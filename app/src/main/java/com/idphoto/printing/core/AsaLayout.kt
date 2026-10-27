package com.idphoto.printing.core

import kotlin.math.min

enum class AsaMode(val photoCount: Int) {
    TOGETHER(1),
    SEPARATE(2),
}

enum class AsaSide {
    LEFT, RIGHT;

    val opposite: AsaSide get() = if (this == LEFT) RIGHT else LEFT
}

/** One horizontal 3 x 2 photo, repeated four times on the existing portrait 5 x 7 sheet. */
object AsaLayout {
    const val PHOTO_WIDTH_MM = 76.2f
    const val PHOTO_HEIGHT_MM = 50.8f
    const val PHOTO_ASPECT = 3f / 2f
    const val PERSON_ASPECT = 3f / 4f

    // Rotate the photos on the paper, keeping the proven portrait printer path/calibration.
    val cells: List<RectMm> = List(4) { index ->
        RectMm(
            left = 11.2f + (index % 2) * (PHOTO_HEIGHT_MM + SheetLayout.COLUMN_GAP_MM),
            top = 11.7f + (index / 2) * (PHOTO_WIDTH_MM + SheetLayout.CUT_GAP_MM),
            width = PHOTO_HEIGHT_MM,
            height = PHOTO_WIDTH_MM,
        )
    }

    /** Indices stay attached to their original photos/crops when the positions change. */
    fun sourceOrder(firstSide: AsaSide): List<Int> =
        if (firstSide == AsaSide.LEFT) listOf(0, 1) else listOf(1, 0)
}

/** A bounded, movable crop; ASA has no face detector or single-person validation. */
object AsaCrop {
    fun fit(
        image: ImageSize,
        aspect: Float = AsaLayout.PHOTO_ASPECT,
        centerX: Float = image.width / 2f,
        centerY: Float = image.height / 2f,
        zoom: Float = 1f,
    ): FloatRect {
        require(image.width > 0 && image.height > 0)
        require(aspect.isFinite() && aspect > 0f)
        require(centerX.isFinite() && centerY.isFinite() && zoom.isFinite())
        val width = min(image.width.toFloat(), image.height * aspect) / zoom.coerceIn(1f, 8f)
        val height = width / aspect
        val left = (centerX - width / 2f).coerceIn(0f, (image.width - width).coerceAtLeast(0f))
        val top = (centerY - height / 2f).coerceIn(0f, (image.height - height).coerceAtLeast(0f))
        return FloatRect(left, top, left + width, top + height)
    }

    fun zoom(image: ImageSize, crop: FloatRect, aspect: Float): Float =
        (fit(image, aspect).width / crop.width).coerceIn(1f, 8f)
}
