package com.idphoto.printing.core

import kotlin.math.min
import kotlin.math.floor
import kotlin.math.roundToInt

/** Coordinates refer to the upright source, never the downsampled display bitmap. */
object ImportCrop {
    fun square(image: ImageSize, centerX: Float, centerY: Float, requestedSide: Float): FloatRect {
        require(image.width > 0 && image.height > 0)
        require(centerX.isFinite() && centerY.isFinite() && requestedSide.isFinite())
        val side = requestedSide.coerceIn(1f, min(image.width, image.height).toFloat())
        val left = (centerX - side / 2).coerceIn(0f, image.width - side)
        val top = (centerY - side / 2).coerceIn(0f, image.height - side)
        return FloatRect(left, top, left + side, top + side)
    }

    fun aroundFace(image: ImageSize, face: FloatRect): FloatRect {
        val side = maxOf(face.height * 2.5f, face.width * 3f)
        // A starting suggestion only: the operator adjusts hair, shoulders and headroom.
        return square(image, face.centerX, face.top + side * 0.27f, side)
    }

    fun pixelSquare(image: ImageSize, crop: FloatRect): FloatRect {
        val bounded = square(image, crop.centerX, crop.centerY, crop.width)
        val side = floor(bounded.width).toInt()
        val left = bounded.left.roundToInt().coerceIn(0, image.width - side).toFloat()
        val top = bounded.top.roundToInt().coerceIn(0, image.height - side).toFloat()
        return FloatRect(left, top, left + side, top + side)
    }

    fun uprightSize(encoded: ImageSize, orientation: Int): ImageSize =
        if (orientation in 5..8) ImageSize(encoded.height, encoded.width) else encoded

    /** EXIF maps edges (not pixel centers), including all mirrored orientations. */
    fun toEncoded(rect: FloatRect, encoded: ImageSize, orientation: Int): FloatRect {
        fun point(x: Float, y: Float): Pair<Float, Float> = when (orientation) {
            2 -> encoded.width - x to y
            3 -> encoded.width - x to encoded.height - y
            4 -> x to encoded.height - y
            5 -> y to x
            6 -> y to encoded.height - x
            7 -> encoded.width - y to encoded.height - x
            8 -> encoded.width - y to x
            else -> x to y
        }
        val points = listOf(point(rect.left, rect.top), point(rect.right, rect.top),
            point(rect.left, rect.bottom), point(rect.right, rect.bottom))
        return FloatRect(points.minOf { it.first }, points.minOf { it.second },
            points.maxOf { it.first }, points.maxOf { it.second })
    }
}
