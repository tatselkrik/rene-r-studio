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

/** Kept separate so tests inspect the actual job attributes sent over IPPS. */
internal fun buildDirectPrintRequest(profile: DirectPrinterProfile, jobName: String,
                                    paper: PaperType, borderless: Boolean): IppPacket {
    val media = MediaCol().apply {
        mediaSizeName = KeywordOrName(if (borderless) profile.borderlessMediaKeyword ?: profile.mediaKeyword else profile.mediaKeyword)
        mediaSource = KeywordOrName(profile.mediaSource)
        mediaType = KeywordOrName(when (paper) {
            PaperType.MATTE -> profile.mediaType
            PaperType.GLOSSY -> requireNotNull(profile.glossyMediaType) {
                "The printer did not report glossy paper support. Reconnect in Printer Setup to refresh its paper options."
            }
        })
        if (borderless) {
            mediaTopMargin = 0
            mediaBottomMargin = 0
            mediaLeftMargin = 0
            mediaRightMargin = 0
        }
    }
    val printerUri = URI("ipps", null, profile.host, profile.port, profile.resourcePath, null, null)
    return IppPacket.printJob(printerUri)
        .putOperationAttributes(
            Types.documentFormat.of(DirectPrinterProfile.JPEG_MIME_TYPE),
            Types.requestingUserName.of("ID Photo Print"),
            Types.jobName.of(jobName),
            // A borderless job must not silently turn into a bordered print.
            Types.ippAttributeFidelity.of(borderless),
        ).putJobAttributes(
            Types.mediaCol.of(media),
            Types.orientationRequested.of(if (borderless) Orientation.landscape else Orientation.portrait),
            Types.printQuality.of(PrintQuality.high),
            Types.printColorMode.of("color"),
            Types.sides.of("one-sided"),
            Types.printScaling.of("fill"),
            Types.copies.of(1),
        ).build()
}
