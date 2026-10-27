package com.idphoto.printing.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.idphoto.printing.core.AsaCrop
import com.idphoto.printing.core.AsaLayout
import com.idphoto.printing.core.AsaMode
import com.idphoto.printing.core.AsaSide
import com.idphoto.printing.core.FloatRect
import com.idphoto.printing.core.ImageSize
import com.idphoto.printing.core.PrintScale
import com.idphoto.printing.image.AsaPhotoComposer
import com.idphoto.printing.image.BackgroundWhitener
import com.idphoto.printing.image.BitmapLoader
import com.idphoto.printing.image.ImportedPhoto
import com.idphoto.printing.image.PhotoAdjustmentProcessor
import com.idphoto.printing.image.PhotoAdjustments
import com.idphoto.printing.image.PhotoImporter
import com.idphoto.printing.print.AsaSheetJpegGenerator
import com.idphoto.printing.print.AsaSheetRenderer
import com.idphoto.printing.print.DirectIppPrinter
import com.idphoto.printing.print.PhotoColorTone
import com.idphoto.printing.print.PrinterConnection
import com.idphoto.printing.print.SheetJpegActions
import com.idphoto.printing.print.createShareJpeg
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class AsaStage { OPTIONS, EDIT, FRAMING, ADJUST, PREVIEW }
private data class AsaSource(val photo: ImportedPhoto, val crop: FloatRect)

@Composable
fun AsaPhotoScreen(connection: PrinterConnection, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val printer by connection.state.collectAsStateWithLifecycle()
    var stage by remember { mutableStateOf(AsaStage.OPTIONS) }
    var mode by remember { mutableStateOf(AsaMode.TOGETHER) }
    var photos by remember { mutableStateOf<List<AsaSource>>(emptyList()) }
    var firstSide by remember { mutableStateOf(AsaSide.LEFT) }
    var cameraOpen by remember { mutableStateOf(false) }
    var captureIndex by remember { mutableIntStateOf(0) }
    var framingIndex by remember { mutableIntStateOf(0) }
    var pendingCapture by remember { mutableStateOf<CapturedPhoto?>(null) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var frame by remember { mutableStateOf<Bitmap?>(null) }
    val backgroundWhitener = remember { BackgroundWhitener() }
    var whiteBackgroundEnabled by remember { mutableStateOf(true) }
    var whiteFrame by remember { mutableStateOf<Bitmap?>(null) }
    var whiteSource by remember { mutableStateOf<Bitmap?>(null) }
    var backgroundBusy by remember { mutableStateOf(false) }
    var backgroundError by remember { mutableStateOf<String?>(null) }
    var adjustments by remember { mutableStateOf(PhotoAdjustments.NEUTRAL) }
    var auto by remember { mutableStateOf(false) }
    var edited by remember { mutableStateOf<Bitmap?>(null) }
    var appliedSource by remember { mutableStateOf<Bitmap?>(null) }
    var appliedSettings by remember { mutableStateOf<PhotoAdjustments?>(null) }
    var appliedAuto by remember { mutableStateOf(false) }
    var adjustmentError by remember { mutableStateOf<String?>(null) }
    val sourceBitmap = if (whiteBackgroundEnabled && whiteSource === frame) whiteFrame ?: frame else frame
    val activeBitmap = if (appliedSource === sourceBitmap) edited ?: sourceBitmap else sourceBitmap
    val backgroundReady = !whiteBackgroundEnabled || (!backgroundBusy && whiteSource === frame && whiteFrame != null)
    val adjustmentsReady = sourceBitmap != null && backgroundReady && appliedSource === sourceBitmap && appliedSettings == adjustments && appliedAuto == auto
    val complete = photos.size == mode.photoCount
    val aspect = if (mode == AsaMode.TOGETHER) AsaLayout.PHOTO_ASPECT else AsaLayout.PERSON_ASPECT
    val workingFiles = remember { mutableListOf<File>() }
    DisposableEffect(Unit) { onDispose { workingFiles.forEach { it.delete() } } }
    DisposableEffect(backgroundWhitener) { onDispose { backgroundWhitener.close() } }

    LaunchedEffect(frame) {
        whiteFrame = null
        whiteSource = null
        backgroundError = null
        val source = frame ?: return@LaunchedEffect
        backgroundBusy = true
        try {
            // The same on-device segmentation used by Single supports multiple people;
            // keep the complete ASA image rather than selecting or identifying a person.
            val result = backgroundWhitener.whiten(source)
            whiteFrame = result
            whiteSource = source
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { backgroundError = "White background could not be prepared. Go back and retry, or turn Background off." }
        finally { backgroundBusy = false }
    }

    LaunchedEffect(sourceBitmap, adjustments, auto) {
        val source = sourceBitmap ?: return@LaunchedEffect
        adjustmentError = null
        val manual = adjustments
        val automatic = auto
        try {
            if (automatic || manual != PhotoAdjustments.NEUTRAL) delay(80)
            val result = withContext(Dispatchers.Default) {
                val settings = if (automatic) PhotoAdjustmentProcessor.automatic(source,
                    AsaCrop.fit(ImageSize(source.width, source.height)), ImageSize(source.width, source.height)) else manual
                if (settings == PhotoAdjustments.NEUTRAL) source else PhotoAdjustmentProcessor.apply(source, settings)
            }
            edited = result
            appliedSource = source
            appliedSettings = manual
            appliedAuto = automatic
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { adjustmentError = "Adjustments could not be applied. Switch Auto on and off to reset." }
    }

    fun work(action: suspend () -> Unit) {
        if (busy) return
        busy = true
        error = null
        message = null
        scope.launch {
            try { action() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (failure: Exception) { error = failure.message ?: "The ASA photo could not be prepared." }
            finally { busy = false }
        }
    }

    fun reset() {
        photos = emptyList()
        frame = null
        whiteFrame = null
        whiteSource = null
        edited = null
        appliedSource = null
        firstSide = AsaSide.LEFT
        workingFiles.forEach { it.delete() }
        workingFiles.clear()
    }

    fun openPhoto(uri: Uri, index: Int) = work {
        val opened = PhotoImporter.open(context, uri)
        workingFiles += opened.file
        val source = AsaSource(opened, AsaCrop.fit(opened.size, aspect))
        val updated = photos.toMutableList()
        if (index < updated.size) {
            updated[index].photo.file.delete()
            updated[index] = source
        } else {
            check(index == updated.size) { "Take the first photo before the second." }
            updated += source
        }
        photos = updated
        frame = null
        stage = AsaStage.EDIT
        if (mode == AsaMode.SEPARATE && updated.size == 1) {
            captureIndex = 1
            cameraOpen = true
        }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            cameraOpen = false
            openPhoto(uri, captureIndex)
        }
    }

    pendingCapture?.let { capture ->
        SaveCaptureScreen(capture) { savedUri ->
            pendingCapture = null
            openPhoto(savedUri, captureIndex)
            capture.file.delete()
        }
        return
    }

    fun closeCamera() {
        cameraOpen = false
        if (photos.isEmpty()) stage = AsaStage.OPTIONS
    }
    if (cameraOpen) {
        BackHandler { if (!busy) closeCamera() }
        CaptureScreen(printer, onCaptured = { capture ->
            workingFiles += capture.file
            cameraOpen = false
            pendingCapture = capture
        }, onPrinterSetup = ::closeCamera, onChoosePhoto = { picker.launch(arrayOf("image/*")) },
            asa = true, asaPerson = if (mode == AsaMode.SEPARATE) captureIndex + 1 else null)
        return
    }

    fun back() {
        error = null
        message = null
        when (stage) {
            AsaStage.OPTIONS -> onBack()
            AsaStage.EDIT -> { reset(); stage = AsaStage.OPTIONS }
            AsaStage.FRAMING -> stage = AsaStage.EDIT
            AsaStage.ADJUST -> stage = AsaStage.EDIT
            AsaStage.PREVIEW -> stage = AsaStage.ADJUST
        }
    }
    BackHandler { if (!busy) back() }

    fun updateCrop(index: Int, crop: FloatRect) {
        photos = photos.mapIndexed { i, source -> if (i == index) source.copy(crop = crop) else source }
    }

    fun prepareFrame() = work {
        val decoded = mutableListOf<Bitmap>()
        try {
            check(complete)
            for (source in photos) {
                val cropFile = PhotoImporter.crop(context, source.photo, source.crop, squareOnly = false)
                try {
                    decoded += withContext(Dispatchers.IO) { BitmapLoader.load(cropFile) }
                } finally { cropFile.delete() }
            }
            val composed = withContext(Dispatchers.Default) { AsaPhotoComposer.compose(decoded, firstSide) }
            currentCoroutineContext().ensureActive()
            frame = composed
            whiteBackgroundEnabled = true
            adjustments = PhotoAdjustments.NEUTRAL
            auto = false
            stage = AsaStage.ADJUST
        } finally { decoded.forEach { it.recycle() } }
    }

    Column(Modifier.fillMaxSize().background(CanvasCream).safeDrawingPadding().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(if (stage == AsaStage.FRAMING) "Adjust framing" else if (stage == AsaStage.ADJUST) "Adjust Photo"
            else if (stage == AsaStage.PREVIEW) "ASA Print Preview" else "ASA", style = MaterialTheme.typography.headlineMedium)
        Text("Four 3 × 2 photos · 5 × 7 paper", style = MaterialTheme.typography.bodyMedium, color = Muted)
        Spacer(Modifier.height(12.dp))

        when (stage) {
            AsaStage.OPTIONS -> {
                Column(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Button({ reset(); mode = AsaMode.TOGETHER; captureIndex = 0; stage = AsaStage.EDIT; cameraOpen = true },
                        Modifier.fillMaxWidth().height(60.dp)) { Text("Together") }
                    Text("Take one photo of both people.", Modifier.padding(8.dp), color = Muted)
                    Spacer(Modifier.height(16.dp))
                    Button({ reset(); mode = AsaMode.SEPARATE; captureIndex = 0; stage = AsaStage.EDIT; cameraOpen = true },
                        Modifier.fillMaxWidth().height(60.dp)) { Text("Separately") }
                    Text("Take two photos, then choose left and right.", Modifier.padding(8.dp), color = Muted)
                }
            }
            AsaStage.EDIT, AsaStage.FRAMING -> {
                Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (stage == AsaStage.FRAMING && mode == AsaMode.SEPARATE) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("First photo", "Second photo").forEachIndexed { index, label ->
                                FilterChip(framingIndex == index, { framingIndex = index }, { Text(label) }, enabled = !busy)
                            }
                        }
                    }
                    AsaFramePreview(photos, mode, firstSide, if (stage == AsaStage.FRAMING) framingIndex else null,
                        !busy, ::updateCrop, Modifier.fillMaxWidth().aspectRatio(AsaLayout.PHOTO_ASPECT))
                    if (stage == AsaStage.FRAMING) {
                        val selected = photos[framingIndex]
                        Text("Drag and zoom to adjust the framing.", style = MaterialTheme.typography.bodySmall, color = Muted)
                        Slider(AsaCrop.zoom(selected.photo.size, selected.crop, aspect), onValueChange = {
                            updateCrop(framingIndex, AsaCrop.fit(selected.photo.size, aspect, selected.crop.centerX, selected.crop.centerY, it))
                        }, valueRange = 1f..8f, enabled = !busy,
                            modifier = Modifier.semantics { contentDescription = "ASA framing zoom" })
                        OutlinedButton({ updateCrop(framingIndex, AsaCrop.fit(selected.photo.size, aspect)) },
                            Modifier.fillMaxWidth(), enabled = !busy) { Text("Auto fit") }
                    } else {
                        if (mode == AsaMode.SEPARATE && complete) AsaPositionControls(firstSide, !busy) { firstSide = it }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            repeat(mode.photoCount) { index ->
                                OutlinedButton({ captureIndex = index; cameraOpen = true }, Modifier.weight(1f),
                                    enabled = !busy && index <= photos.size) {
                                    Text(if (mode == AsaMode.TOGETHER) if (complete) "Retake" else "Take Photo"
                                        else "${if (index < photos.size) "Retake" else "Take"} ${index + 1}")
                                }
                            }
                        }
                        OutlinedButton({ framingIndex = 0; stage = AsaStage.FRAMING }, Modifier.fillMaxWidth(),
                            enabled = !busy && complete) { Text("Adjust framing") }
                        if (photos.any { it.crop.width < (if (mode == AsaMode.TOGETHER) 900 else 450) || it.crop.height < 600 })
                            Text("Low resolution: prints may look softer.", color = Color(0xFF9A6700), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            AsaStage.ADJUST -> {
                val bitmap = activeBitmap
                if (bitmap != null) PhotoCrop(bitmap, AsaCrop.fit(ImageSize(bitmap.width, bitmap.height)),
                    ImageSize(bitmap.width, bitmap.height), Modifier.height(170.dp).aspectRatio(AsaLayout.PHOTO_ASPECT))
                Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Background")
                            Switch(whiteBackgroundEnabled, { whiteBackgroundEnabled = it }, enabled = !busy,
                                modifier = Modifier.semantics { contentDescription = "Background" })
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Auto")
                            Switch(auto, { adjustments = PhotoAdjustments.NEUTRAL; auto = it }, enabled = !busy,
                                modifier = Modifier.semantics { contentDescription = "Auto" })
                        }
                    }
                    if (backgroundBusy && whiteBackgroundEnabled) Text("Preparing white background…", style = MaterialTheme.typography.bodySmall, color = Muted)
                    if (whiteBackgroundEnabled) backgroundError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    PhotoAdjustmentControls(adjustments, auto || busy) { adjustments = it }
                    adjustmentError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            }
            AsaStage.PREVIEW -> {
                val bitmap = activeBitmap
                if (bitmap != null) AsaSheetPreview(bitmap, Modifier.weight(1f).fillMaxWidth())
            }
        }

        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth().padding(vertical = 6.dp))
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        message?.let { Text(it, color = Pine, style = MaterialTheme.typography.bodySmall) }
        Spacer(Modifier.height(8.dp))
        if (stage == AsaStage.PREVIEW) {
            Text(printer.message, style = MaterialTheme.typography.bodySmall, color = Muted)
            OutlinedButton({ work {
                val jpeg = withContext(Dispatchers.IO) {
                    AsaSheetJpegGenerator.generate(createShareJpeg(File(context.cacheDir, "print"), "asa-share-"), requireNotNull(activeBitmap))
                }
                SheetJpegActions.share(context, jpeg, "Share ASA sheet")
            } }, Modifier.fillMaxWidth().height(48.dp), enabled = !busy && adjustmentsReady) { Text("Share") }
            PaperTypeChoice(printer.paperType, !busy && !printer.checking) { paper ->
                scope.launch { connection.selectPaper(paper) }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(::back, Modifier.weight(1f).height(48.dp), enabled = !busy) { Text("Back") }
            when (stage) {
                AsaStage.OPTIONS -> Unit
                AsaStage.EDIT -> Button(::prepareFrame, Modifier.weight(1f).height(48.dp), enabled = !busy && complete) { Text("Preview") }
                AsaStage.FRAMING -> Button({ stage = AsaStage.EDIT }, Modifier.weight(1f).height(48.dp), enabled = !busy) { Text("Done") }
                AsaStage.ADJUST -> Button({ stage = AsaStage.PREVIEW }, Modifier.weight(1f).height(48.dp),
                    enabled = !busy && adjustmentsReady) { Text("Preview") }
                AsaStage.PREVIEW -> Button({ work {
                    if (connection.state.value.profile == null) connection.refresh()
                    val profile = connection.state.value.profile ?: error(connection.state.value.message)
                    val paper = connection.state.value.paperType
                    message = withContext(Dispatchers.IO) {
                        val jpeg = AsaSheetJpegGenerator.generate(File(context.cacheDir, "print/asa-direct.jpg"),
                            requireNotNull(activeBitmap), PrintScale.L15150_RC_WOVEN_MATTE, PhotoColorTone.DIRECT_PRINT_WARM)
                        DirectIppPrinter.printJpeg(profile, jpeg, "ASA four 3x2 photos", paper).message
                    }
                } }, Modifier.weight(1f).height(48.dp), enabled = !busy && !printer.checking && adjustmentsReady) { Text("Print") }
            }
        }
    }
}

@Composable
private fun AsaPositionControls(firstSide: AsaSide, enabled: Boolean, onChange: (AsaSide) -> Unit) {
    Column {
        listOf("First photo" to firstSide, "Second photo" to firstSide.opposite).forEachIndexed { index, (label, current) ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(label, Modifier.weight(1f))
                AsaSide.entries.forEach { side ->
                    FilterChip(current == side, { onChange(if (index == 0) side else side.opposite) },
                        { Text(if (side == AsaSide.LEFT) "Left" else "Right") }, enabled = enabled,
                        modifier = Modifier.semantics { contentDescription = "$label ${side.name.lowercase()}" })
                }
            }
        }
    }
}

@Composable
private fun AsaFramePreview(photos: List<AsaSource>, mode: AsaMode, firstSide: AsaSide, editing: Int?,
                            enabled: Boolean, onCrop: (Int, FloatRect) -> Unit, modifier: Modifier) {
    Row(modifier.background(Color.White)) {
        val order = if (mode == AsaMode.TOGETHER) listOf(0) else AsaLayout.sourceOrder(firstSide)
        order.forEach { index ->
            val source = photos.getOrNull(index)
            val cell = Modifier.weight(1f).fillMaxHeight()
            if (source == null) Box(cell, contentAlignment = Alignment.Center) { Text("Photo ${index + 1}", color = Muted) }
            else key(source.photo.file) {
                var size by remember { mutableStateOf(IntSize.Zero) }
                val current by rememberUpdatedState(source)
                val onCropUpdated by rememberUpdatedState(onCrop)
                val aspect = if (mode == AsaMode.TOGETHER) AsaLayout.PHOTO_ASPECT else AsaLayout.PERSON_ASPECT
                PhotoCrop(source.photo.preview, source.crop, source.photo.size,
                    cell.onSizeChanged { size = it }
                        .semantics { contentDescription = if (mode == AsaMode.TOGETHER) "ASA together photo" else "ASA photo ${index + 1}" }
                        .border(if (editing == index) 2.dp else 0.dp, if (editing == index) Pine else Color.Transparent)
                        .pointerInput(source.photo.file, size, editing, enabled) {
                            if (editing == index && enabled) detectTransformGestures { _, pan, zoom, _ ->
                                if (size.width > 0) {
                                    val crop = current.crop
                                    val scale = size.width / crop.width
                                    onCropUpdated(index, AsaCrop.fit(current.photo.size, aspect,
                                        crop.centerX - pan.x / scale, crop.centerY - pan.y / scale,
                                        AsaCrop.zoom(current.photo.size, crop, aspect) * zoom))
                                }
                            }
                        })
            }
        }
    }
}

@Composable
private fun AsaSheetPreview(bitmap: Bitmap, modifier: Modifier) {
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val width = minOf(maxWidth, maxHeight * (7f / 5f))
        Canvas(Modifier.width(width).aspectRatio(7f / 5f).semantics { contentDescription = "Four ASA photos on 5 by 7 paper" }) {
            // Display the paper sideways so each photo is upright, while printing with the existing portrait path.
            drawIntoCanvas { composeCanvas ->
                val canvas = composeCanvas.nativeCanvas
                canvas.save()
                try {
                    canvas.translate(0f, size.height)
                    canvas.rotate(-90f)
                    AsaSheetRenderer.draw(canvas, size.height, size.width, bitmap)
                } finally { canvas.restore() }
            }
        }
    }
}
