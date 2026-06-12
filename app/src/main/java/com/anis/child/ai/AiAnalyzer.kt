package com.anis.child.ai

import ai.onnxruntime.OnnxJavaType
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Half
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.nio.ShortBuffer
import kotlin.math.exp
import kotlin.math.max

class AiAnalyzer(context: Context) {

    private val bannedWords = listOf(
        "fuck", "shit", "ass", "bitch", "bastard", "damn", "dick", "piss", "slut", "whore",
        "kill", "murder", "death", "die", "suicide", "rape", "torture", "slaughter", "massacre", "terror",
        "porn", "nude", "sex", "sexy", "erotic", "xxx", "horny", "naked", "stripper", "orgasm",
        "blood", "gore", "corpse", "carcass", "skeleton", "skull", "wound", "injury", "trauma", "surgery",
        "drug", "cocaine", "heroin", "meth", "weed", "marijuana", "cannabis", "opium", "lsd", "ecstasy",
        "alcohol", "beer", "whiskey", "vodka", "cigarette", "cigar", "smoke", "vape", "nicotine", "tobacco",
        "weapon", "gun", "rifle", "pistol", "knife", "sword", "bomb", "explosive", "missile", "bullet",
        "hate", "racist", "nazi", "terrorist", "abuse", "bully", "threat", "assault", "attack", "violent",
        "gambling", "casino", "poker", "bet", "lottery", "striptease", "lingerie", "bikini", "swimsuit", "underwear"
    )
    private val env: OrtEnvironment = OrtEnvironment.getEnvironment()

    private var visionSession: OrtSession? = null
    private val CLIP_MEAN = floatArrayOf(0.48145466f, 0.4578275f, 0.40821073f)

    private val CLIP_STD = floatArrayOf(0.26862954f, 0.26130258f, 0.27577711f)
    private var baselineEmbed: FloatArray? = null

    private val threatsEmbeds = mutableMapOf<String, FloatArray>()
    var lastEmbedding: FloatArray? = null
    private var modelLoaded = false
    private var loadError: String? = null

    init {
        try {
            val modelFile = File(context.cacheDir, "vision_model_fp16.onnx")
            if (!modelFile.exists()) {
                context.assets.open("vision_model_fp16.onnx").use { inputStream ->
                    FileOutputStream(modelFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }

            visionSession = env.createSession(modelFile.absolutePath, OrtSession.SessionOptions())

            val jsonString = context.assets.open("saved_embeddings.json").bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)

            val baseArray = jsonObject.getJSONArray("Baseline")
            baselineEmbed = FloatArray(baseArray.length()) { i -> baseArray.getDouble(i).toFloat() }

            val iter = jsonObject.keys()
            while (iter.hasNext()) {
                val key = iter.next()
                if (key != "Baseline") {
                    val threatArray = jsonObject.getJSONArray(key)
                    val floatArray = FloatArray(threatArray.length()) { i -> threatArray.getDouble(i).toFloat() }
                    threatsEmbeds[key] = floatArray
                }
            }
            modelLoaded = true
        } catch (e: Exception) {
            android.util.Log.e("AiAnalyzer", "Failed to load model", e)
            loadError = e.message
            modelLoaded = false
        }
    }

    suspend fun analyzeImage(bitmap: Bitmap): String {
        if (!modelLoaded) {
            return "Scan Results:\n\nModel not loaded: ${loadError ?: "Unknown error"}\n\nFinal Decision: SAFE (No Model)"
        }
        val totalStart = System.nanoTime()

        var ocrTimeMs = 0.0
        var preprocessTimeMs = 0.0
        var inferenceTimeMs = 0.0

        val ocrStart = System.nanoTime()
        val ocrText = extractTextFromImage(bitmap).lowercase()
        ocrTimeMs = (System.nanoTime() - ocrStart) / 1_000_000.0

        android.util.Log.d("AiAnalyzer", "OCR completed: ${String.format("%.2f", ocrTimeMs)} ms")

        val foundBannedWord = bannedWords.find { word -> ocrText.contains(word) }

        if (foundBannedWord != null) {
            return "Scan Results:\n\nDetected banned word: $foundBannedWord\nOCR Time: ${String.format("%.2f", ocrTimeMs)} ms\n\nFinal Decision: BLOCKED (Early Exit - OCR)"
        }

        val preprocessStart = System.nanoTime()
        val shortBuffer = preprocessBitmapToFp16(bitmap)
        preprocessTimeMs = (System.nanoTime() - preprocessStart) / 1_000_000.0
        android.util.Log.d("AiAnalyzer", "Preprocess completed: ${String.format("%.2f", preprocessTimeMs)} ms")

        val inputShape = longArrayOf(1, 3, 224, 224)

        val inferenceStart = System.nanoTime()
        val inputTensor = OnnxTensor.createTensor(env, shortBuffer, inputShape, OnnxJavaType.FLOAT16)

        val inputs = mapOf("pixel_values" to inputTensor)
        val result = visionSession!!.run(inputs)
        inferenceTimeMs = (System.nanoTime() - inferenceStart) / 1_000_000.0

        android.util.Log.d("AiAnalyzer", "ONNX inference completed: ${String.format("%.2f", inferenceTimeMs)} ms")

        @Suppress("UNCHECKED_CAST")
        val rawOutput = (result[0].value as Array<FloatArray>)[0]
        val imgEmbed = rawOutput

        inputTensor.close()
        result.close()

        var norm = 0.0f
        for (v in imgEmbed) norm += v * v
        norm = Math.sqrt(norm.toDouble()).toFloat()
        for (i in imgEmbed.indices) imgEmbed[i] /= norm
        lastEmbedding = imgEmbed.copyOf()

        val LOGIT_SCALE = exp(4.60517f)
        val safeLogit = dotProduct(imgEmbed, baselineEmbed!!) * LOGIT_SCALE

        val report = StringBuilder("Scan Results (ONNX Model):\n\n")
        var isBlocked = false

        for ((threatName, threatEmbed) in threatsEmbeds) {
            val threatLogit = dotProduct(imgEmbed, threatEmbed) * LOGIT_SCALE
            val maxLog = max(threatLogit, safeLogit)
            val expT = exp(threatLogit - maxLog)
            val expS = exp(safeLogit - maxLog)
            val prob = expT / (expT + expS)

            val threshold = when(threatName) {
                "Adult" -> 0.35f
                "Violence" -> 0.40f
                else -> 0.40f
            }

            if (prob >= threshold) isBlocked = true
            report.append("- $threatName: ${(prob * 100).toInt()}%\n")
        }

        val totalTimeMs = (System.nanoTime() - totalStart) / 1_000_000.0
        report.append("\nOCR Time: ${String.format("%.2f", ocrTimeMs)} ms\n")
        report.append("Preprocess Time: ${String.format("%.2f", preprocessTimeMs)} ms\n")
        report.append("ONNX Time: ${String.format("%.2f", inferenceTimeMs)} ms\n")
        report.append("\nTotal Analysis: ${String.format("%.2f", totalTimeMs)} ms\n")
        report.append("\nFinal Decision: ${if (isBlocked) "BLOCKED" else "SAFE"}")

        android.util.Log.d("AiAnalyzer", "TOTAL: OCR=${String.format("%.2f", ocrTimeMs)}ms, preprocess=${String.format("%.2f", preprocessTimeMs)}ms, ONNX=${String.format("%.2f", inferenceTimeMs)}ms, total=${String.format("%.2f", totalTimeMs)}ms")

        return report.toString()
    }

    private suspend fun extractTextFromImage(bitmap: Bitmap): String {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val visionText = recognizer.process(image).await()
            visionText.text
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private fun preprocessBitmapToFp16(bitmap: Bitmap): ShortBuffer {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val buffer = ShortBuffer.allocate(3 * 224 * 224)

        for (y in 0 until 224) {
            for (x in 0 until 224) {
                val pixel = resizedBitmap.getPixel(x, y)
                val r = Color.red(pixel) / 255.0f
                val g = Color.green(pixel) / 255.0f
                val b = Color.blue(pixel) / 255.0f

                val normR = (r - CLIP_MEAN[0]) / CLIP_STD[0]
                val normG = (g - CLIP_MEAN[1]) / CLIP_STD[1]
                val normB = (b - CLIP_MEAN[2]) / CLIP_STD[2]

                val indexR = y * 224 + x
                val indexG = 224 * 224 + indexR
                val indexB = 2 * 224 * 224 + indexR

                buffer.put(indexR, Half.toHalf(normR))
                buffer.put(indexG, Half.toHalf(normG))
                buffer.put(indexB, Half.toHalf(normB))
            }
        }
        return buffer
    }

    private fun dotProduct(a: FloatArray, b: FloatArray): Float {
        var sum = 0.0f
        for (i in a.indices) sum += a[i] * b[i]
        return sum
    }
}
