package com.idphoto.printing.core

import org.junit.Assert.*
import org.junit.Test

class AsaLayoutTest {
    @Test fun `four exact horizontal 3 by 2 photos fit the portrait paper when rotated`() {
        assertEquals(4, AsaLayout.cells.size)
        AsaLayout.cells.forEach { cell ->
            assertEquals(50.8f, cell.width, 0.001f)
            assertEquals(76.2f, cell.height, 0.001f)
            assertTrue(cell.left >= 0 && cell.top >= 0)
            assertTrue(cell.right <= SheetLayout.PAPER_WIDTH_MM)
            assertTrue(cell.bottom <= SheetLayout.PAPER_HEIGHT_MM)
        }
        AsaLayout.cells.forEachIndexed { index, a ->
            AsaLayout.cells.drop(index + 1).forEach { b ->
                assertTrue(a.right <= b.left || b.right <= a.left || a.bottom <= b.top || b.bottom <= a.top)
            }
        }
    }

    @Test fun `existing printer scale keeps all four cells inside the page`() {
        val scale = PrintScale.L15150_RC_WOVEN_MATTE
        val cx = SheetLayout.PAPER_WIDTH_MM / 2f
        val cy = SheetLayout.PAPER_HEIGHT_MM / 2f
        AsaLayout.cells.forEach { cell ->
            assertTrue(cx + (cell.left - cx) * scale.x >= 0)
            assertTrue(cy + (cell.top - cy) * scale.y >= 0)
            assertTrue(cx + (cell.right - cx) * scale.x <= SheetLayout.PAPER_WIDTH_MM)
            assertTrue(cy + (cell.bottom - cy) * scale.y <= SheetLayout.PAPER_HEIGHT_MM)
        }
    }

    @Test fun `left and right controls always use both source photos once`() {
        assertEquals(listOf(0, 1), AsaLayout.sourceOrder(AsaSide.LEFT))
        assertEquals(listOf(1, 0), AsaLayout.sourceOrder(AsaSide.RIGHT))
        AsaSide.entries.forEach { side ->
            assertNotEquals(side, side.opposite)
            assertEquals(side, side.opposite.opposite)
            assertEquals(setOf(0, 1), AsaLayout.sourceOrder(side).toSet())
        }
    }

    @Test fun `together fills a horizontal photo and each separate photo fills one half`() {
        val photo = ImageSize(2400, 3200)
        val together = AsaCrop.fit(photo)
        val individual = AsaCrop.fit(photo, AsaLayout.PERSON_ASPECT)
        assertEquals(2400f, together.width, 0.01f)
        assertEquals(1600f, together.height, 0.01f)
        assertEquals(2400f, individual.width, 0.01f)
        assertEquals(3200f, individual.height, 0.01f)
        assertEquals(AsaLayout.PHOTO_ASPECT, 2 * individual.width / individual.height, 0.001f)
        assertEquals(1, AsaMode.TOGETHER.photoCount)
        assertEquals(2, AsaMode.SEPARATE.photoCount)
    }

    @Test fun `framing stays inside upright images without changing proportions`() {
        for (image in listOf(ImageSize(4000, 3000), ImageSize(3000, 4000), ImageSize(12, 7))) {
            for (aspect in listOf(AsaLayout.PHOTO_ASPECT, AsaLayout.PERSON_ASPECT)) {
                for (zoom in listOf(-1f, 1f, 4f, 100f)) {
                    for (center in listOf(-10000f, 0f, 10000f)) {
                        val crop = AsaCrop.fit(image, aspect, center, center, zoom)
                        assertTrue(crop.left >= 0 && crop.top >= 0)
                        assertTrue(crop.right <= image.width + 0.001f && crop.bottom <= image.height + 0.001f)
                        assertEquals(aspect, crop.width / crop.height, 0.001f)
                        assertEquals(zoom.coerceIn(1f, 8f), AsaCrop.zoom(image, crop, aspect), 0.01f)
                    }
                }
            }
        }
    }

    @Test(expected = IllegalArgumentException::class) fun `nonfinite framing is rejected`() {
        AsaCrop.fit(ImageSize(100, 100), zoom = Float.NaN)
    }

    @Test(expected = IllegalArgumentException::class) fun `invalid source size is rejected`() {
        AsaCrop.fit(ImageSize(0, 100))
    }
}
