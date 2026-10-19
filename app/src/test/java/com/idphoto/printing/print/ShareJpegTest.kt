package com.idphoto.printing.print

import java.io.File
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ShareJpegTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test fun `successive ID and landscape shares retain their original photo contents`() {
        val directory = File(temporary.root, "print")
        val exports = (1..20).flatMap { index ->
            listOf("id-photo-5x7-share-", "landscape-share-").map { prefix ->
                val contents = "$prefix customer $index"
                val file = createShareJpeg(directory, prefix).apply { writeText(contents) }
                file to contents
            }
        }
        assertEquals(exports.size, exports.map { it.first.canonicalPath }.distinct().size)
        exports.forEach { (file, originalContents) -> assertEquals(originalContents, file.readText()) }
    }
}
