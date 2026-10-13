package com.idphoto.printing.print

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.exifinterface.media.ExifInterface
import java.io.File
import com.idphoto.printing.core.ImageSize
import com.idphoto.printing.core.LandscapeCrop
import kotlin.math.roundToInt

object LandscapeJpegGenerator {
    const val WIDTH_PIXELS = 4200
    const val HEIGHT_PIXELS = 3000

    fun generate(output: File, bitmap: Bitmap, tone: PhotoColorTone = PhotoColorTone.NEUTRAL): File {
        output.parentFile?.mkdirs()
        val sheet = Bitmap.createBitmap(WIDTH_PIXELS, HEIGHT_PIXELS, Bitmap.Config.ARGB_8888)
        try {
            val canvas = Canvas(sheet)
            canvas.drawColor(Color.WHITE)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                colorFilter = ColorMatrixColorFilter(ColorMatrix(tone.colorMatrixValues()))
            }
            // Fill the entire page. No cutting guides, ID cells or margins.
            val crop = LandscapeCrop.fit(ImageSize(bitmap.width, bitmap.height))
            canvas.drawBitmap(bitmap, Rect(crop.left.roundToInt(), crop.top.roundToInt(), crop.right.roundToInt(), crop.bottom.roundToInt()),
                RectF(0f, 0f, WIDTH_PIXELS.toFloat(), HEIGHT_PIXELS.toFloat()), paint)
            output.outputStream().buffered().use {
                check(sheet.compress(Bitmap.CompressFormat.JPEG, 100, it)) { "The landscape JPEG could not be created." }
            }
            ExifInterface(output).apply {
                setAttribute(ExifInterface.TAG_X_RESOLUTION, "600/1")
                setAttribute(ExifInterface.TAG_Y_RESOLUTION, "600/1")
                setAttribute(ExifInterface.TAG_RESOLUTION_UNIT, "2")
                saveAttributes()
            }
            return output
        } catch (error: Exception) {
            output.delete()
            throw error
        } finally {
            sheet.recycle()
        }
    }
}
