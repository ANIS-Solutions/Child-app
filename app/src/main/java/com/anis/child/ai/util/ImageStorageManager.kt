package com.anis.child.ai.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ImageStorageManager {

    private const val MAX_IMAGE_WIDTH = 1280
    private const val JPEG_QUALITY = 80
    private const val IMAGES_DIR = "session_images"

    suspend fun saveImage(
        context: Context,
        bitmap: Bitmap,
        sessionId: Long,
        timestamp: Long
    ): String? = withContext(Dispatchers.IO) {
        try {
            val scaledBitmap = scaleBitmap(bitmap, MAX_IMAGE_WIDTH)

            val imagesDir = File(context.filesDir, IMAGES_DIR)
            val sessionDir = File(imagesDir, sessionId.toString())
            if (!sessionDir.exists()) {
                sessionDir.mkdirs()
            }

            val fileName = "$timestamp.jpg"
            val imageFile = File(sessionDir, fileName)

            FileOutputStream(imageFile).use { outputStream ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
            }

            scaledBitmap.recycle()

            imageFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun loadBitmapFromPath(path: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (file.exists()) {
                BitmapFactory.decodeFile(path)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteSessionImages(context: Context, sessionId: Long) {
        val sessionDir = File(context.filesDir, "$IMAGES_DIR/$sessionId")
        if (sessionDir.exists()) {
            sessionDir.deleteRecursively()
        }
    }

    fun scaleBitmap(bitmap: Bitmap, maxWidth: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth) {
            return bitmap.copy(bitmap.config ?: Bitmap.Config.ARGB_8888, false)
        }

        val scale = maxWidth.toFloat() / width
        val newHeight = (height * scale).toInt()

        return Bitmap.createScaledBitmap(bitmap, maxWidth, newHeight, true)
    }
}
