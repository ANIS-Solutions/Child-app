package com.anis.child.ai.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object ImageStorageManager {

    private const val MAX_IMAGE_WIDTH = 1280
    private const val JPEG_QUALITY = 80
    private const val IMAGES_DIR = "session_images"
    private const val CACHE_DIR = "session_cache"

    private fun encryptedFile(context: Context, file: File): EncryptedFile {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedFile.Builder(
            context,
            file,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
    }

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

            encryptedFile(context, imageFile).openFileOutput().use { outputStream ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
            }

            scaledBitmap.recycle()

            imageFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun saveImageToCache(
        context: Context,
        bitmap: Bitmap,
        sessionId: Long,
        timestamp: Long
    ): String? = withContext(Dispatchers.IO) {
        try {
            val scaledBitmap = scaleBitmap(bitmap, MAX_IMAGE_WIDTH)

            val cacheDir = File(context.cacheDir, CACHE_DIR)
            val sessionDir = File(cacheDir, sessionId.toString())
            if (!sessionDir.exists()) {
                sessionDir.mkdirs()
            }

            val fileName = "$timestamp.jpg"
            val imageFile = File(sessionDir, fileName)

            encryptedFile(context, imageFile).openFileOutput().use { outputStream ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
            }

            scaledBitmap.recycle()

            imageFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getPermanentImagePath(context: Context, sessionId: Long, timestamp: Long): String {
        return File(context.filesDir, "$IMAGES_DIR/$sessionId/$timestamp.jpg").absolutePath
    }

    fun getCacheImagePath(context: Context, sessionId: Long, timestamp: Long): String {
        return File(context.cacheDir, "$CACHE_DIR/$sessionId/$timestamp.jpg").absolutePath
    }

    suspend fun moveImageToPermanent(context: Context, sessionId: Long, timestamp: Long): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val src = File(context.cacheDir, "$CACHE_DIR/$sessionId/$timestamp.jpg")
                if (!src.exists()) return@withContext false

                val destDir = File(context.filesDir, "$IMAGES_DIR/$sessionId")
                if (!destDir.exists()) destDir.mkdirs()

                val dest = File(destDir, "$timestamp.jpg")

                val srcEf = encryptedFile(context, src)
                val destEf = encryptedFile(context, dest)

                srcEf.openFileInput().use { input ->
                    destEf.openFileOutput().use { output ->
                        input.copyTo(output)
                    }
                }

                src.delete()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    fun clearSessionCache(context: Context, sessionId: Long) {
        val sessionDir = File(context.cacheDir, "$CACHE_DIR/$sessionId")
        if (sessionDir.exists()) {
            sessionDir.deleteRecursively()
        }
    }

    suspend fun loadBitmapFromPath(context: Context, path: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (file.exists()) {
                encryptedFile(context, file).openFileInput().use { input ->
                    BitmapFactory.decodeStream(input)
                }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun readImageBytes(context: Context, path: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (file.exists()) {
                encryptedFile(context, file).openFileInput().use { input ->
                    input.readBytes()
                }
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
