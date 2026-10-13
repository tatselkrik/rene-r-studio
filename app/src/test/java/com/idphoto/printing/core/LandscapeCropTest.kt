package com.idphoto.printing.core

import org.junit.Assert.*
import org.junit.Test

class LandscapeCropTest {
    @Test fun `wide photo trims sides while portrait photo trims top and bottom`() {
        val wide = LandscapeCrop.fit(ImageSize(2000, 1000))
        assertEquals(300f, wide.left, 0.01f)
        assertEquals(1700f, wide.right, 0.01f)
        val portrait = LandscapeCrop.fit(ImageSize(1000, 2000))
        assertEquals(0f, portrait.left, 0.01f)
        assertEquals(1000f, portrait.right, 0.01f)
        assertEquals(7f / 5f, portrait.width / portrait.height, 0.0001f)
    }

    @Test fun `panning and zooming cannot expose empty space`() {
        for (size in listOf(ImageSize(4200, 3000), ImageSize(3000, 4200), ImageSize(12, 7))) {
            for (zoom in listOf(-3f, 1f, 4f, 100f)) {
                for (center in listOf(-10000f, 0f, 10000f)) {
                    val crop = LandscapeCrop.fit(size, center, center, zoom)
                    assertTrue(crop.left >= 0f && crop.top >= 0f)
                    assertTrue(crop.right <= size.width + 0.001f && crop.bottom <= size.height + 0.001f)
                    assertEquals(LandscapeCrop.ASPECT, crop.width / crop.height, 0.001f)
                }
            }
        }
    }

    @Test(expected = IllegalArgumentException::class) fun `invalid crop geometry is rejected`() {
        LandscapeCrop.fit(ImageSize(100, 100), zoom = Float.NaN)
    }
}
