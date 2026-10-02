package com.idphoto.printing.print

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object SheetJpegActions {
    fun share(context: Context, jpeg: File) {
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
        context.startActivity(Intent.createChooser(send, "Share 5 x 7 ID photo sheet"))
    }
}
