package com.liferise.app.photos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

data class CompressedPhotoResult(
    val file: File,
    val sizeBytes: Long,
    val mimeType: String,
    val width: Int,
    val height: Int
)

object PhotoCompressor {

    private const val MAX_DIMENSION = 1600
    private const val MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024 // 5 MB limit
    private const val TARGET_COMPRESSION_QUALITY = 82

    suspend fun compressImage(
        context: Context,
        imageUri: Uri
    ): Result<CompressedPhotoResult> = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(imageUri) ?: "image/jpeg"

            // Validate MIME type
            if (!mimeType.startsWith("image/")) {
                return@withContext Result.failure(IllegalArgumentException("Unsupported file type: $mimeType. Only images are permitted."))
            }

            // Decode dimensions first (inJustDecodeBounds)
            var inputStream: InputStream? = contentResolver.openInputStream(imageUri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // Calculate inSampleSize
            var sampleSize = 1
            while (options.outWidth / sampleSize > MAX_DIMENSION || options.outHeight / sampleSize > MAX_DIMENSION) {
                sampleSize *= 2
            }

            // Decode actual bitmap
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            inputStream = contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            inputStream?.close()

            if (bitmap == null) {
                return@withContext Result.failure(IllegalStateException("Failed to decode image from selected URI."))
            }

            // Compress to target size
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, TARGET_COMPRESSION_QUALITY, outputStream)
            val bytes = outputStream.toByteArray()

            if (bytes.size > MAX_FILE_SIZE_BYTES) {
                return@withContext Result.failure(IllegalArgumentException("Image exceeds the maximum allowed size of 5 MB even after compression."))
            }

            // Save to cache directory
            val cacheFile = File(context.cacheDir, "compressed_post_${System.currentTimeMillis()}.jpg")
            FileOutputStream(cacheFile).use { fos ->
                fos.write(bytes)
                fos.flush()
            }

            val result = CompressedPhotoResult(
                file = cacheFile,
                sizeBytes = cacheFile.length(),
                mimeType = "image/jpeg",
                width = bitmap.width,
                height = bitmap.height
            )
            bitmap.recycle()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
