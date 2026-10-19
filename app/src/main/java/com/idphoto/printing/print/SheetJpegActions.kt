package com.idphoto.printing.print

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/** A granted URI must keep referring to the same photo when another customer is processed. */
internal fun createShareJpeg(directory: File, prefix: String): File {
    check(directory.isDirectory || directory.mkdirs()) { "The share folder could not be created." }
    return File.createTempFile(prefix, ".jpg", directory)
}

object SheetJpegActions {
    fun share(context: Context, jpeg: File, title: String = "Share 5 x 7 ID photo sheet") {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            jpeg,
        )
        val send = Intent(Intent.ACTION_SEND).apply {
            type = DirectPrinterProfile.JPEG_MIME_TYPE
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri(jpeg.name, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, title))
    }
}
