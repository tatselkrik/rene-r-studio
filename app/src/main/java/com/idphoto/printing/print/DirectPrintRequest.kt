package com.idphoto.printing.print

import com.hp.jipp.encoding.IppPacket
import com.hp.jipp.encoding.KeywordOrName
import com.hp.jipp.model.MediaCol
import com.hp.jipp.model.Orientation
import com.hp.jipp.model.PrintQuality
import com.hp.jipp.model.Types
import java.net.URI

internal fun selectGlossyPhotoMediaType(types: List<String>): String? =
    types.firstOrNull { it.equals("photographic-glossy", true) }
        ?: types.firstOrNull { "glossy" in it.lowercase() && "semi" !in it.lowercase() }

enum class DirectPrintMode(val landscape: Boolean, val borderless: Boolean, val scaling: String) {
    SHEET(false, false, "fill"),
    LANDSCAPE_BORDERLESS(true, true, "fill"),
    LANDSCAPE_BORDERED(true, false, "fit"),
}

// The L15150's standard single-sheet margin, in millimetres.
const val LANDSCAPE_BORDER_MM = 3f

/** Kept separate so tests inspect the actual job attributes sent over IPPS. */
internal fun buildDirectPrintRequest(profile: DirectPrinterProfile, jobName: String,
                                    paper: PaperType, mode: DirectPrintMode = DirectPrintMode.SHEET): IppPacket {
    require(mode != DirectPrintMode.LANDSCAPE_BORDERED || !profile.mediaKeyword.contains("borderless", true)) {
        "The printer did not report regular 5 × 7 paper. Reconnect in Printer Setup before choosing Bordered."
    }
    val media = MediaCol().apply {
        mediaSizeName = KeywordOrName(if (mode.borderless) profile.borderlessMediaKeyword ?: profile.mediaKeyword else profile.mediaKeyword)
        mediaSource = KeywordOrName(profile.mediaSource)
        mediaType = KeywordOrName(when (paper) {
            PaperType.MATTE -> profile.mediaType
            PaperType.GLOSSY -> requireNotNull(profile.glossyMediaType) {
                "The printer did not report glossy paper support. Reconnect in Printer Setup to refresh its paper options."
            }
        })
        if (mode.landscape) {
            // IPP margin units are hundredths of a millimetre. Explicit positive margins
            // keep Bordered from inheriting a printer's previous borderless defaults.
            val margin = if (mode.borderless) 0 else (LANDSCAPE_BORDER_MM * 100).toInt()
            mediaTopMargin = margin
            mediaBottomMargin = margin
            mediaLeftMargin = margin
            mediaRightMargin = margin
        }
    }
    val printerUri = URI("ipps", null, profile.host, profile.port, profile.resourcePath, null, null)
    return IppPacket.printJob(printerUri)
        .putOperationAttributes(
            Types.documentFormat.of(DirectPrinterProfile.JPEG_MIME_TYPE),
            Types.requestingUserName.of("ID Photo Print"),
            Types.jobName.of(jobName),
            // Landscape orientation, scaling and selected margins must not be silently substituted.
            Types.ippAttributeFidelity.of(mode.landscape),
        ).putJobAttributes(
            Types.mediaCol.of(media),
            Types.orientationRequested.of(if (mode.landscape) Orientation.landscape else Orientation.portrait),
            Types.printQuality.of(PrintQuality.high),
            Types.printColorMode.of("color"),
            Types.sides.of("one-sided"),
            Types.printScaling.of(mode.scaling),
            Types.copies.of(1),
        ).build()
}
