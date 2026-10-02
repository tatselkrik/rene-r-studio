package com.idphoto.printing.core

import org.junit.Assert.*
import org.junit.Test

class ImportCropTest {
    @Test fun dragAndResizeStaySquareInsideImage() {
        val image = ImageSize(4000, 3000)
        for (x in listOf(-500f, 2000f, 5000f)) for (y in listOf(-500f, 1500f, 4000f)) {
            for (side in listOf(600f, 1800f, 9000f)) {
                val crop = ImportCrop.square(image, x, y, side)
                assertEquals(crop.width, crop.height, 0.01f)
                assertTrue(crop.left >= 0 && crop.top >= 0 && crop.right <= 4000 && crop.bottom <= 3000)
                assertEquals(minOf(side, 3000f), crop.width, 0.01f)
            }
        }
    }

    @Test fun faceSuggestionLeavesRoomAboveFaceAndForShoulders() {
        val face = FloatRect(1400f, 900f, 1800f, 1450f)
        val crop = ImportCrop.aroundFace(ImageSize(4000, 3000), face)
        assertEquals(face.centerX, crop.centerX, 0.01f)
        assertTrue(crop.top < face.top - face.height * 0.5f)
        assertTrue(crop.bottom > face.bottom + 100)
    }

    @Test fun pixelRoundingKeepsSquareAtImageEdges() {
        val image = ImageSize(4033, 3025)
        val crop = ImportCrop.pixelSquare(image, FloatRect(3032.3f, 2024.3f, 4033f, 3025f))
        assertEquals(1000f, crop.width, 0f)
        assertEquals(crop.width, crop.height, 0f)
        assertTrue(crop.right <= image.width && crop.bottom <= image.height)
    }

    @Test fun everyExifOrientationMapsKnownUprightRegionToEncodedEdges() {
        val image = ImageSize(4000, 3000)
        val crop = FloatRect(100f, 200f, 700f, 800f)
        val expected = listOf(
            FloatRect(100f, 200f, 700f, 800f), FloatRect(3300f, 200f, 3900f, 800f),
            FloatRect(3300f, 2200f, 3900f, 2800f), FloatRect(100f, 2200f, 700f, 2800f),
            FloatRect(200f, 100f, 800f, 700f), FloatRect(200f, 2300f, 800f, 2900f),
            FloatRect(3200f, 2300f, 3800f, 2900f), FloatRect(3200f, 100f, 3800f, 700f),
        )
        for (orientation in 1..8) assertEquals(expected[orientation - 1], ImportCrop.toEncoded(crop, image, orientation))
    }

    @Test fun wholeUprightImageMapsBackToWholeEncodedImage() {
        val encoded = ImageSize(4000, 3000)
        for (orientation in 1..8) {
            val size = ImportCrop.uprightSize(encoded, orientation)
            val result = ImportCrop.toEncoded(FloatRect(0f, 0f, size.width.toFloat(), size.height.toFloat()), encoded, orientation)
            assertEquals(FloatRect(0f, 0f, 4000f, 3000f), result)
        }
    }

    @Test fun smallGroupFaceRemainsBelowPrintResolutionInsteadOfBeingUpscaled() {
        val crop = ImportCrop.aroundFace(ImageSize(6000, 4000), FloatRect(2500f, 1000f, 2600f, 1140f))
        assertFalse(CropPlanner.plan(ImageSize(6000, 4000), crop).resolutionSufficient)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidCoordinatesAreRejected() {
        ImportCrop.square(ImageSize(4000, 3000), Float.NaN, 0f, 1000f)
    }
}
