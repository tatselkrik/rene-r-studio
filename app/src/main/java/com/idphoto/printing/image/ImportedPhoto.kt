package com.idphoto.printing.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Matrix
import android.graphics.Rect
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.idphoto.printing.core.FloatRect
import com.idphoto.printing.core.ImageSize
import com.idphoto.printing.core.ImportCrop
import java.io.File
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

data class ImportedPhoto(
    val file: File,
    val encodedSize: ImageSize,
    val orientation: Int,
    val preview: Bitmap,
) {
    val size: ImageSize get() = ImportCrop.uprightSize(encodedSize, orientation)
}

object PhotoImporter {
    suspend fun open(context: Context, uri: Uri): ImportedPhoto = withContext(Dispatchers.IO) {
        val directory = File(context.cacheDir, "imports").apply { mkdirs() }
        val file = File.createTempFile("source-", ".image", directory)
        try {
            requireNotNull(context.contentResolver.openInputStream(uri)) {
                "This photo is unavailable. Choose another file."
            }.use { input ->
                file.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var total = 0L
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val count = input.read(buffer)
                        if (count < 0) break
                        total += count
                        require(total <= 100L * 1024 * 1024) { "Choose a photo smaller than 100 MB." }
                        output.write(buffer, 0, count)
                    }
                }
            }
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.path, bounds)
            require(bounds.outWidth > 0 && bounds.outHeight > 0) {
                "This file cannot be read as a photo. Try a JPEG or PNG."
            }
            val encoded = ImageSize(bounds.outWidth, bounds.outHeight)
            val orientation = runCatching {
                ExifInterface(file).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
            }.getOrDefault(1)
            var sample = 1
            while (max(encoded.width, encoded.height) / sample > 1600) sample *= 2
            val decoded = requireNotNull(BitmapFactory.decodeFile(file.path,
                BitmapFactory.Options().apply {
                    inSampleSize = sample
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                })) { "This photo could not be opened." }
            val preview = upright(decoded, orientation)
            currentCoroutineContext().ensureActive()
            ImportedPhoto(file, encoded, orientation, preview)
        } catch (error: Throwable) {
            file.delete()
            throw error
        }
    }

    /** Decode only the selected source region, keeping small faces at source resolution. */
    @Suppress("DEPRECATION")
    suspend fun crop(context: Context, photo: ImportedPhoto, square: FloatRect, squareOnly: Boolean = true): File =
        withContext(Dispatchers.IO) {
            require(square.width >= 1f && square.height >= 1f) { "Select a valid crop." }
            require(square.left >= 0 && square.top >= 0 && square.right <= photo.size.width + 0.01f && square.bottom <= photo.size.height + 0.01f) { "The crop must stay inside the photo." }
            val uprightCrop = if (squareOnly) ImportCrop.pixelSquare(photo.size, square) else square
            val raw = ImportCrop.toEncoded(uprightCrop, photo.encodedSize, photo.orientation)
            val region = Rect(floor(raw.left).toInt().coerceAtLeast(0),
                floor(raw.top).toInt().coerceAtLeast(0),
                ceil(raw.right).toInt().coerceAtMost(photo.encodedSize.width),
                ceil(raw.bottom).toInt().coerceAtMost(photo.encodedSize.height))
            val decoder = try {
                requireNotNull(BitmapRegionDecoder.newInstance(photo.file.path, false))
            } catch (error: Exception) {
                throw IllegalArgumentException("This photo format cannot be cropped on this phone. Try a JPEG or PNG.", error)
            }
            val bitmap = try {
                var sample = 1
                while (max(region.width(), region.height()) / sample > 2400) sample *= 2
                val decoded = requireNotNull(decoder.decodeRegion(region, BitmapFactory.Options().apply {
                    inSampleSize = sample
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                })) { "The selected crop could not be read." }
                upright(decoded, photo.orientation)
            } finally {
                decoder.recycle()
            }
            val directory = File(context.cacheDir, "captures").apply { mkdirs() }
            val output = File.createTempFile("selected-", ".png", directory)
            try {
                currentCoroutineContext().ensureActive()
                output.outputStream().use {
                    check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) { "The selected crop could not be saved." }
                }
                currentCoroutineContext().ensureActive()
                output
            } catch (error: Throwable) {
                output.delete()
                throw error
            } finally {
                bitmap.recycle()
            }
        }

    private fun upright(source: Bitmap, orientation: Int): Bitmap {
        if (orientation !in 2..8) return source
        // Android matrices map encoded coordinates into the upright image.
        val values = when (orientation) {
            2 -> floatArrayOf(-1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f)
            3 -> floatArrayOf(-1f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, 1f)
            4 -> floatArrayOf(1f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, 1f)
            5 -> floatArrayOf(0f, 1f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)
            6 -> floatArrayOf(0f, -1f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)
            7 -> floatArrayOf(0f, -1f, 0f, -1f, 0f, 0f, 0f, 0f, 1f)
            else -> floatArrayOf(0f, 1f, 0f, -1f, 0f, 0f, 0f, 0f, 1f)
        }
        val result = Bitmap.createBitmap(source, 0, 0, source.width, source.height,
            Matrix().apply { setValues(values) }, true)
        if (result !== source) source.recycle()
        return result
    }
}
