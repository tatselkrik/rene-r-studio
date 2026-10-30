package com.idphoto.printing.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.idphoto.printing.core.AsaCrop
import com.idphoto.printing.core.FloatRect
import com.idphoto.printing.print.AsaSheetJpegGenerator
import com.idphoto.printing.print.LandscapeJpegGenerator
import java.io.File
import kotlin.math.abs
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CropPipelineInstrumentedTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val directory = File(context.cacheDir, "crop-regression-${System.nanoTime()}").apply { mkdirs() }
    private val opened = mutableListOf<ImportedPhoto>()

    @After fun clean() {
        opened.forEach { it.preview.recycle(); it.file.delete() }
        directory.deleteRecursively()
    }

    private suspend fun photo(orientation: Int, width: Int = 360, height: Int = 240): ImportedPhoto {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        val colors = listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)
        colors.forEachIndexed { index, color ->
            paint.color = color
            val x = (index % 2) * width / 2f
            val y = (index / 2) * height / 2f
            canvas.drawRect(x, y, x + width / 2f, y + height / 2f, paint)
        }
        val source = File(directory, "source-$orientation-$width.jpg")
        try { source.outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)) } }
        finally { bitmap.recycle() }
        ExifInterface(source).apply {
            setAttribute(ExifInterface.TAG_ORIENTATION, orientation.toString())
            saveAttributes()
        }
        return PhotoImporter.open(context, Uri.fromFile(source)).also { opened += it }
    }

    @Test fun fullCropsKeepAllEightExifOrientations() = runBlocking {
        val colors = intArrayOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)
        val orders = arrayOf(intArrayOf(0,1,2,3), intArrayOf(1,0,3,2), intArrayOf(3,2,1,0),
            intArrayOf(2,3,0,1), intArrayOf(0,2,1,3), intArrayOf(2,0,3,1),
            intArrayOf(3,1,2,0), intArrayOf(1,3,0,2))
        for (orientation in 1..8) {
            val source = photo(orientation)
            val crop = FloatRect(0f, 0f, source.size.width.toFloat(), source.size.height.toFloat())
            val result = PhotoImporter.cropBitmap(source, crop, squareOnly = false)
            try {
                assertEquals(source.size.width, result.width)
                assertEquals(source.size.height, result.height)
                for (index in 0..3) {
                    val pixel = result.getPixel(result.width * (if (index % 2 == 0) 1 else 3) / 4,
                        result.height * (if (index < 2) 1 else 3) / 4)
                    val expected = colors[orders[orientation - 1][index]]
                    for (channel in listOf(Color::red, Color::green, Color::blue)) {
                        assertTrue("Orientation $orientation quadrant $index", abs(channel(pixel) - channel(expected)) <= 10)
                    }
                }
            } finally { result.recycle() }
        }
    }

    @Test fun rectangularPreviewPixelsMatchThePngRoundTripWithoutCreatingCropFiles() = runBlocking {
        for (orientation in 1..8) {
            val source = photo(orientation, 3080, 2060)
            for (aspect in listOf(1.5f, 7f / 5f, 0.75f)) {
                val selection = AsaCrop.fit(source.size, aspect)
                val before = File(context.cacheDir, "captures").list()?.toSet().orEmpty()
                val direct = PhotoImporter.cropBitmap(source, selection, squareOnly = false)
                try {
                    assertEquals(before, File(context.cacheDir, "captures").list()?.toSet().orEmpty())
                    val png = PhotoImporter.crop(context, source, selection, squareOnly = false)
                    try {
                        val decoded = BitmapLoader.load(png)
                        try {
                            assertTrue("Pixels changed for orientation $orientation, aspect $aspect", direct.sameAs(decoded))
                            assertTrue(direct.width <= 2400 && direct.height <= 2400)
                        } finally { decoded.recycle() }
                    } finally { png.delete() }
                } finally { direct.recycle() }
            }
        }
    }

    @Test fun directCropsStillExportAsaAndLandscapeAtTheEstablishedSizeAndDpi() = runBlocking {
        val source = photo(1, 1800, 1200)
        val crop = PhotoImporter.cropBitmap(source, AsaCrop.fit(source.size, 1.5f), squareOnly = false)
        try {
            val asa = AsaSheetJpegGenerator.generate(File(directory, "asa.jpg"), crop)
            val landscape = LandscapeJpegGenerator.generate(File(directory, "landscape.jpg"), crop)
            for ((file, size) in listOf(asa to (3000 to 4200), landscape to (4200 to 3000))) {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(file.path, bounds)
                assertEquals(size.first, bounds.outWidth)
                assertEquals(size.second, bounds.outHeight)
                val exif = ExifInterface(file)
                assertEquals(600.0, exif.getAttributeDouble(ExifInterface.TAG_X_RESOLUTION, 0.0), 0.01)
                assertEquals(600.0, exif.getAttributeDouble(ExifInterface.TAG_Y_RESOLUTION, 0.0), 0.01)
            }
        } finally { crop.recycle() }
    }
}
