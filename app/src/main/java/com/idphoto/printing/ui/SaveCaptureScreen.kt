package com.idphoto.printing.ui

import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import android.provider.Settings
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.idphoto.printing.image.GalleryPhotoStore
import kotlinx.coroutines.CancellationException

@Composable
fun SaveCaptureScreen(capture: CapturedPhoto, onSaved: (Uri) -> Unit) {
    val context = LocalContext.current
    var attempt by remember(capture) { mutableIntStateOf(0) }
    var error by remember(capture) { mutableStateOf<String?>(null) }
    var busy by remember(capture) { mutableStateOf(true) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) attempt++ else {
            busy = false
            error = "Allow storage access to save this photo. The original is kept while you retry."
        }
    }
    // Keep this captured original available until saving succeeds; never pretend it was saved.
    BackHandler { }
    LaunchedEffect(capture, attempt) {
        busy = true
        error = null
        if (Build.VERSION.SDK_INT <= 28 && ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return@LaunchedEffect
        }
        try {
            onSaved(GalleryPhotoStore.save(context, capture.file))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            error = "Could not save the original. Free some phone storage and retry. ${failure.message.orEmpty()}"
        } finally {
            busy = false
        }
    }
    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        if (busy) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text("Saving photo…")
        } else {
            Text(error ?: "The photo has not been saved.", color = MaterialTheme.colorScheme.error)
            Button(onClick = { attempt++ }) { Text("Retry saving") }
            if (Build.VERSION.SDK_INT <= 28) TextButton(onClick = {
                context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:${context.packageName}")))
            }) { Text("App settings") }
        }
        Text("Pictures / ${GalleryPhotoStore.ALBUM}", style = MaterialTheme.typography.bodySmall)
    }
}
