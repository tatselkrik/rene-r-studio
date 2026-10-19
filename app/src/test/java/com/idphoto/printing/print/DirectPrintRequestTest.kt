package com.idphoto.printing.print

import com.hp.jipp.encoding.Tag
import com.hp.jipp.model.Orientation
import com.hp.jipp.model.Types
import java.net.URI
import org.junit.Assert.*
import org.junit.Test

class DirectPrintRequestTest {
    private val profile = DirectPrinterProfile("printer.test", certificateSha256 = "pin",
        glossyMediaType = "photographic-glossy", borderlessMediaKeyword = "na_5x7_5x7in-borderless")

    @Test fun `ID print keeps portrait matte and printer margins`() {
        val request = buildDirectPrintRequest(profile, "ID", PaperType.MATTE, false)
        val media = requireNotNull(request.getValue(Tag.jobAttributes, Types.mediaCol))
        assertEquals("photographic-matte", media.mediaType.toString())
        assertEquals(profile.mediaKeyword, media.mediaSizeName.toString())
        assertNull(media.mediaTopMargin)
        assertEquals(Orientation.portrait, request.getValue(Tag.jobAttributes, Types.orientationRequested))
        assertEquals(false, request.getValue(Tag.operationAttributes, Types.ippAttributeFidelity))
    }

    @Test fun `landscape job requests glossy zero margins and strict fidelity`() {
        val request = buildDirectPrintRequest(profile, "Photo", PaperType.GLOSSY, true)
        val media = requireNotNull(request.getValue(Tag.jobAttributes, Types.mediaCol))
        assertEquals("photographic-glossy", media.mediaType.toString())
        assertEquals(profile.borderlessMediaKeyword, media.mediaSizeName.toString())
        assertEquals(listOf(0, 0, 0, 0), listOf(media.mediaTopMargin, media.mediaBottomMargin, media.mediaLeftMargin, media.mediaRightMargin))
        assertEquals(Orientation.landscape, request.getValue(Tag.jobAttributes, Types.orientationRequested))
        assertEquals(true, request.getValue(Tag.operationAttributes, Types.ippAttributeFidelity))
    }

    @Test fun `standard media still receives zero margins when no special keyword is advertised`() {
        val request = buildDirectPrintRequest(profile.copy(borderlessMediaKeyword = null), "Photo", PaperType.MATTE, true)
        val media = requireNotNull(request.getValue(Tag.jobAttributes, Types.mediaCol))
        assertEquals(profile.mediaKeyword, media.mediaSizeName.toString())
        assertEquals(0, media.mediaLeftMargin)
    }

    @Test(expected = IllegalArgumentException::class) fun `missing glossy support cannot silently print matte`() {
        buildDirectPrintRequest(profile.copy(glossyMediaType = null), "Photo", PaperType.GLOSSY, true)
    }

    @Test fun `glossy selection prefers full gloss over semigloss`() {
        assertEquals("photographic-glossy", selectGlossyPhotoMediaType(listOf("photographic-semi-glossy", "photographic-glossy")))
        assertNull(selectGlossyPhotoMediaType(listOf("photographic-matte", "photographic-semi-glossy")))
        assertEquals(PaperType.MATTE, PaperType.fromSaved(null))
        assertEquals(PaperType.GLOSSY, PaperType.fromSaved("GLOSSY"))
    }

    @Test fun `IPv6 hosts from discovery and manual URI parsing produce the same printer URI`() {
        for (host in listOf("2001:db8::1", "[2001:db8::1]")) {
            val request = buildDirectPrintRequest(profile.copy(host = host), "ID", PaperType.MATTE, false)
            assertEquals(URI("ipps://[2001:db8::1]:631/ipp/print"),
                request.getValue(Tag.operationAttributes, Types.printerUri))
        }
    }

    @Test fun `printer resource paths are escaped as paths rather than parsed as URI syntax`() {
        val request = buildDirectPrintRequest(profile.copy(resourcePath = "/ipp/photo queue"), "ID", PaperType.MATTE, false)
        assertEquals(URI("ipps://printer.test:631/ipp/photo%20queue"),
            request.getValue(Tag.operationAttributes, Types.printerUri))
    }
}
