package com.idphoto.printing.image

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.core.graphics.createBitmap
import com.idphoto.printing.core.AsaCrop
import com.idphoto.printing.core.AsaLayout
import com.idphoto.printing.core.AsaSide
import com.idphoto.printing.core.ImageSize
import kotlin.math.roundToInt

object AsaPhotoComposer {
    const val WIDTH_PIXELS = 1800
    const val HEIGHT_PIXELS = 1200

    /** Sources are already cropped from their original files; no overview is enlarged for export. */
    fun compose(photos: List<Bitmap>, firstSide: AsaSide): Bitmap {
        require(photos.size in 1..2) { "ASA needs one shared photo or two separate photos." }
        val result = createBitmap(WIDTH_PIXELS, HEIGHT_PIXELS)
        try {
            val canvas = Canvas(result)
            canvas.drawColor(Color.WHITE)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)
            val order = if (photos.size == 1) listOf(0) else AsaLayout.sourceOrder(firstSide)
            order.forEachIndexed { position, index ->
                val bitmap = photos[index]
                val aspect = if (photos.size == 1) AsaLayout.PHOTO_ASPECT else AsaLayout.PERSON_ASPECT
                val crop = AsaCrop.fit(ImageSize(bitmap.width, bitmap.height), aspect)
                val cellWidth = WIDTH_PIXELS.toFloat() / photos.size
                canvas.drawBitmap(bitmap,
                    Rect(crop.left.roundToInt(), crop.top.roundToInt(), crop.right.roundToInt(), crop.bottom.roundToInt()),
                    RectF(position * cellWidth, 0f, (position + 1) * cellWidth, HEIGHT_PIXELS.toFloat()), paint)
            }
            return result
        } catch (error: Throwable) {
            result.recycle()
            throw error
        }
    }
}
