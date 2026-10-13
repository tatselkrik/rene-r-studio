package com.idphoto.printing.core

import kotlin.math.min

/** A movable 7:5 crop always inside the upright image, with no letterboxing. */
object LandscapeCrop {
    const val ASPECT = 7f / 5f

    fun fit(image: ImageSize, centerX: Float = image.width / 2f,
            centerY: Float = image.height / 2f, zoom: Float = 1f): FloatRect {
        require(image.width > 0 && image.height > 0)
        require(centerX.isFinite() && centerY.isFinite() && zoom.isFinite())
        val width = min(image.width.toFloat(), image.height * ASPECT) / zoom.coerceIn(1f, 8f)
        val height = width / ASPECT
        val left = (centerX - width / 2).coerceIn(0f, (image.width - width).coerceAtLeast(0f))
        val top = (centerY - height / 2).coerceIn(0f, (image.height - height).coerceAtLeast(0f))
        return FloatRect(left, top, left + width, top + height)
    }

    fun zoom(image: ImageSize, crop: FloatRect): Float = (fit(image).width / crop.width).coerceIn(1f, 8f)
}
