package com.idphoto.printing.print

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.RectF
import androidx.exifinterface.media.ExifInterface
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withSave
import com.idphoto.printing.core.AsaLayout
import com.idphoto.printing.core.PrintScale
import com.idphoto.printing.core.SheetLayout
import java.io.File

internal object AsaSheetRenderer {
    fun draw(canvas: Canvas, width: Float, height: Float, photo: Bitmap,
             scale: PrintScale = PrintScale.IDENTITY, tone: PhotoColorTone = PhotoColorTone.NEUTRAL) {
        val mmX = width / SheetLayout.PAPER_WIDTH_MM
        val mmY = height / SheetLayout.PAPER_HEIGHT_MM
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG).apply {
            if (tone != PhotoColorTone.NEUTRAL) colorFilter = ColorMatrixColorFilter(ColorMatrix(tone.colorMatrixValues()))
        }
        val guides = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = SheetLayout.CUT_GUIDE_STROKE_MM * mmX
            strokeCap = Paint.Cap.SQUARE
        }
        // Unlike drawColor, this respects the page transform/extent in the Compose preview.
        canvas.drawRect(0f, 0f, width, height, Paint().apply { color = Color.WHITE })
        canvas.withSave {
            canvas.scale(scale.x, scale.y, width / 2f, height / 2f)
            AsaLayout.cells.forEach { cell ->
                val bounds = RectF(cell.left * mmX, cell.top * mmY, cell.right * mmX, cell.bottom * mmY)
                canvas.withSave {
                    canvas.translate(bounds.right, bounds.top)
                    canvas.rotate(90f)
                    canvas.drawBitmap(photo, null, RectF(0f, 0f, bounds.height(), bounds.width()), paint)
                }
                SheetCanvasRenderer.drawCornerCutGuides(canvas, bounds, SheetLayout.CUT_GUIDE_LENGTH_MM * mmX, guides)
            }
        }
    }
}

object AsaSheetJpegGenerator {
    fun generate(output: File, photo: Bitmap, scale: PrintScale = PrintScale.IDENTITY,
                 tone: PhotoColorTone = PhotoColorTone.NEUTRAL): File {
        output.parentFile?.mkdirs()
        val sheet = createBitmap(SheetJpegGenerator.WIDTH_PIXELS, SheetJpegGenerator.HEIGHT_PIXELS)
        try {
            AsaSheetRenderer.draw(Canvas(sheet), sheet.width.toFloat(), sheet.height.toFloat(), photo, scale, tone)
            output.outputStream().buffered().use {
                check(sheet.compress(Bitmap.CompressFormat.JPEG, 100, it)) { "The ASA sheet could not be created." }
            }
            ExifInterface(output).apply {
                setAttribute(ExifInterface.TAG_X_RESOLUTION, "600/1")
                setAttribute(ExifInterface.TAG_Y_RESOLUTION, "600/1")
                setAttribute(ExifInterface.TAG_RESOLUTION_UNIT, "2")
                saveAttributes()
            }
            return output
        } catch (error: Throwable) {
            output.delete()
            throw error
        } finally { sheet.recycle() }
    }
}
