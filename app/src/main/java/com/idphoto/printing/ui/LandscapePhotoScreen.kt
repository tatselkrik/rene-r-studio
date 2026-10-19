package com.idphoto.printing.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.idphoto.printing.core.FloatRect
import com.idphoto.printing.core.ImageSize
import com.idphoto.printing.core.LandscapeCrop
import com.idphoto.printing.image.PhotoAdjustments
import com.idphoto.printing.image.PhotoAdjustmentProcessor
import com.idphoto.printing.image.BitmapLoader
import com.idphoto.printing.image.ImportedPhoto
import com.idphoto.printing.image.PhotoImporter
import com.idphoto.printing.print.*
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LandscapePhotoScreen(connection: PrinterConnection, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by connection.state.collectAsStateWithLifecycle()
    var cameraOpen by remember { mutableStateOf(false) }
    var pendingCapture by remember { mutableStateOf<CapturedPhoto?>(null) }
    var photo by remember { mutableStateOf<ImportedPhoto?>(null) }
    var crop by remember { mutableStateOf<FloatRect?>(null) }
    var preview by remember { mutableStateOf<Bitmap?>(null) }
    var finalPreview by remember { mutableStateOf(false) }
    var adjustments by remember { mutableStateOf(PhotoAdjustments.NEUTRAL) }
    var auto by remember { mutableStateOf(false) }
    var edited by remember { mutableStateOf<Bitmap?>(null) }
    var appliedSource by remember { mutableStateOf<Bitmap?>(null) }
    var appliedManual by remember { mutableStateOf<PhotoAdjustments?>(null) }
    var appliedAuto by remember { mutableStateOf(false) }
    var adjustmentError by remember { mutableStateOf<String?>(null) }
    val adjustmentsReady = preview != null && appliedSource === preview && appliedManual == adjustments && appliedAuto == auto
    val activeBitmap = if (appliedSource === preview) edited ?: preview else preview
    LaunchedEffect(preview, adjustments, auto) {
        val source = preview ?: return@LaunchedEffect
        adjustmentError = null
        val manualSettings = adjustments
        val automatic = auto
        try {
            if (automatic || manualSettings != PhotoAdjustments.NEUTRAL) delay(80)
            val result = withContext(Dispatchers.Default) {
                val settings = if (automatic) PhotoAdjustmentProcessor.automatic(source,
                    LandscapeCrop.fit(ImageSize(source.width, source.height)), ImageSize(source.width, source.height)) else manualSettings
                if (settings == PhotoAdjustments.NEUTRAL) source else PhotoAdjustmentProcessor.apply(source, settings)
            }
            edited = result
            appliedSource = source
            appliedManual = manualSettings
            appliedAuto = automatic
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { adjustmentError = "Adjustments could not be applied. Switch Auto on and off to reset." }
    }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    val workingFiles = remember { mutableListOf<File>() }
    DisposableEffect(Unit) { onDispose { workingFiles.forEach { it.delete() } } }

    fun work(action: suspend () -> Unit) {
        scope.launch {
            busy = true
            error = null
            message = null
            try { action() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (failure: Exception) { error = failure.message ?: "The photo could not be processed." }
            finally { busy = false }
        }
    }
    fun openPhoto(uri: Uri) = work {
        val opened = PhotoImporter.open(context, uri)
        workingFiles += opened.file
        photo?.file?.delete()
        photo = opened
        crop = LandscapeCrop.fit(opened.size)
        preview = null
        finalPreview = false
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) openPhoto(uri)
    }
    pendingCapture?.let { capture ->
        SaveCaptureScreen(capture) { savedUri ->
            pendingCapture = null
            capture.file.delete()
            openPhoto(savedUri)
        }
        return
    }
    if (cameraOpen) {
        BackHandler { cameraOpen = false }
        CaptureScreen(state, onCaptured = {
            workingFiles += it.file
            cameraOpen = false
            pendingCapture = it
        }, onPrinterSetup = { cameraOpen = false }, onChoosePhoto = {
            cameraOpen = false
            picker.launch(arrayOf("image/*"))
        }, landscape = true)
        return
    }
    fun back() {
        if (finalPreview) { finalPreview = false; error = null; message = null }
        else if (preview != null) { preview = null; error = null; message = null } else onBack()
    }
    BackHandler { if (!busy) back() }

    Column(Modifier.fillMaxSize().background(CanvasCream).safeDrawingPadding().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(if (preview == null) "Landscape Photo" else if (finalPreview) "7 × 5 Print Preview" else "Adjust Photo", style = MaterialTheme.typography.titleLarge)
        Text("Borderless • ${state.paperType.label}", style = MaterialTheme.typography.bodyMedium, color = Muted)
        val source = photo
        val selection = crop
        Box((if (preview != null && !finalPreview) Modifier.fillMaxWidth().height(200.dp) else Modifier.weight(1f).fillMaxWidth()), contentAlignment = Alignment.Center) {
            BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val imageModifier = Modifier.width(minOf(maxWidth, maxHeight * LandscapeCrop.ASPECT)).aspectRatio(LandscapeCrop.ASPECT)
            val ready = activeBitmap
            if (ready != null) {
                PhotoCrop(ready, LandscapeCrop.fit(ImageSize(ready.width, ready.height)),
                    ImageSize(ready.width, ready.height), imageModifier)
            } else if (source != null && selection != null) {
                var size by remember { mutableStateOf(IntSize.Zero) }
                val current by rememberUpdatedState(selection)
                PhotoCrop(source.preview, selection, source.size,
                    imageModifier.onSizeChanged { size = it }
                        .semantics { contentDescription = "Landscape crop preview" }
                        .pointerInput(source, size, busy) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                if (!busy && size.width > 0) {
                                    val scale = size.width / current.width
                                    crop = LandscapeCrop.fit(source.size,
                                        current.centerX - pan.x / scale, current.centerY - pan.y / scale,
                                        LandscapeCrop.zoom(source.size, current) * zoom)
                                }
                            }
                        })
            } else Text("Open a photo to print.", color = Muted)
            }
            if (busy) CircularProgressIndicator()
        }
        if (preview == null && source != null && selection != null) {
            Text("Drag and zoom to fill the page.", style = MaterialTheme.typography.bodySmall)
            Slider(LandscapeCrop.zoom(source.size, selection), onValueChange = {
                crop = LandscapeCrop.fit(source.size, selection.centerX, selection.centerY, it)
            }, valueRange = 1f..8f, enabled = !busy,
                modifier = Modifier.semantics { contentDescription = "Landscape zoom" })
            Box(Modifier.fillMaxWidth().height(40.dp), contentAlignment = Alignment.TopCenter) {
                if (selection.width < 2100 || selection.height < 1500)
                    Text("Low resolution: prints may look softer.", color = androidx.compose.ui.graphics.Color(0xFF9A6700),
                        style = MaterialTheme.typography.bodySmall)
            }
        }
        if (preview != null && !finalPreview) {
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    Text("Auto")
                    Switch(auto, onCheckedChange = { adjustments = PhotoAdjustments.NEUTRAL; auto = it },
                        enabled = !busy, modifier = Modifier.semantics { contentDescription = "Auto" })
                }
                PhotoAdjustmentControls(adjustments, auto || busy) { adjustments = it }
                adjustmentError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        message?.let { Text(it, color = Pine, style = MaterialTheme.typography.bodySmall) }
        Spacer(Modifier.height(8.dp))
        if (preview == null) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button({ cameraOpen = true }, Modifier.weight(1f).height(48.dp), enabled = !busy) { Text("Take Photo") }
                OutlinedButton({ picker.launch(arrayOf("image/*")) }, Modifier.weight(1f).height(48.dp), enabled = !busy) {
                    Text("Open Photo")
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(::back, Modifier.weight(1f).height(48.dp), enabled = !busy) { Text("Back") }
                Button({ work {
                    val file = PhotoImporter.crop(context, requireNotNull(photo), requireNotNull(crop), squareOnly = false)
                    workingFiles += file
                    preview = withContext(Dispatchers.IO) { BitmapLoader.load(file) }
                    adjustments = PhotoAdjustments.NEUTRAL
                    auto = false
                    finalPreview = false
                } }, Modifier.weight(1f).height(48.dp), enabled = !busy && source != null) { Text("Preview") }
            }
        } else if (!finalPreview) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(::back, Modifier.weight(1f).height(48.dp), enabled = !busy) { Text("Reframe") }
                Button({ finalPreview = true }, Modifier.weight(1f).height(48.dp), enabled = !busy && adjustmentsReady) { Text("Preview") }
            }
        } else {
            Text(state.message, color = Muted, style = MaterialTheme.typography.bodySmall)
            OutlinedButton({ work {
                val jpeg = withContext(Dispatchers.IO) {
                    LandscapeJpegGenerator.generate(createShareJpeg(File(context.cacheDir, "print"), "landscape-share-"), requireNotNull(activeBitmap))
                }
                SheetJpegActions.share(context, jpeg, "Share landscape photo")
            } }, Modifier.fillMaxWidth().height(48.dp), enabled = !busy && adjustmentsReady) { Text("Share") }
            Spacer(Modifier.height(8.dp))
            PaperTypeChoice(state.paperType, !busy && !state.checking) { paper ->
                scope.launch { connection.selectPaper(paper) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(::back, Modifier.weight(1f).height(48.dp), enabled = !busy) { Text("Back") }
                Button({ work {
                    if (connection.state.value.profile == null) connection.refresh()
                    val printer = connection.state.value.profile ?: error(connection.state.value.message)
                    val paper = connection.state.value.paperType
                    message = withContext(Dispatchers.IO) {
                        val jpeg = LandscapeJpegGenerator.generate(File(context.cacheDir, "print/landscape-direct.jpg"),
                            requireNotNull(activeBitmap), PhotoColorTone.DIRECT_PRINT_WARM)
                        DirectIppPrinter.printJpeg(printer, jpeg, "Landscape photo 7x5", paper, borderlessLandscape = true).message
                    }
                } }, Modifier.weight(1f).height(48.dp), enabled = !busy && !state.checking && adjustmentsReady) {
                    Text(if (busy) "Printing…" else "Print")
                }
            }
        }
    }
}
