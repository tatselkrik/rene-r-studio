package com.idphoto.printing.image

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

/** Save the untouched camera JPEG, including its orientation, before editing it. */
object GalleryPhotoStore {
    const val ALBUM = "ReneR ID Print"

    suspend fun save(context: Context, source: File): Uri =
        withContext(Dispatchers.IO + NonCancellable) {
            require(source.isFile && source.length() > 0) { "The camera photo is unavailable." }
            if (Build.VERSION.SDK_INT >= 29) {
                val resolver = context.contentResolver
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, source.name)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$ALBUM")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val uri = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)) {
                    "The photo album could not be opened."
                }
                try {
                    requireNotNull(resolver.openOutputStream(uri, "w")).use { output ->
                        source.inputStream().use { it.copyTo(output) }
                    }
                    check(resolver.update(uri, ContentValues().apply {
                        put(MediaStore.Images.Media.IS_PENDING, 0)
                    }, null, null) == 1) { "The saved photo could not be made visible in the gallery." }
                    uri
                } catch (error: Exception) {
                    runCatching { resolver.delete(uri, null, null) }
                    throw error
                }
            } else {
                @Suppress("DEPRECATION")
                val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), ALBUM)
                check(directory.isDirectory || directory.mkdirs()) { "The photo album could not be created." }
                val destination = File(directory, source.name)
                // A unique camera filename prevents replacing an earlier original.
                check(destination.createNewFile()) { "A photo with this name already exists." }
                try {
                    source.inputStream().use { input -> destination.outputStream().use { input.copyTo(it) } }
                    MediaScannerConnection.scanFile(context, arrayOf(destination.path), arrayOf("image/jpeg"), null)
                    Uri.fromFile(destination)
                } catch (error: Exception) {
                    destination.delete()
                    throw error
                }
            }
        }
}
