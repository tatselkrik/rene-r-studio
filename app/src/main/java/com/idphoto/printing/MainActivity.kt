package com.idphoto.printing

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.idphoto.printing.print.PrinterConnection
import com.idphoto.printing.print.watchWifi
import com.idphoto.printing.analysis.CheckStatus
import com.idphoto.printing.analysis.BlurDetector
import com.idphoto.printing.analysis.PhotoAnalyzer
import com.idphoto.printing.analysis.PhotoReview
import com.idphoto.printing.core.ImageSize
import com.idphoto.printing.core.FloatRect
import com.idphoto.printing.core.ImportCrop
import com.idphoto.printing.core.SheetCombination
import com.idphoto.printing.image.BackgroundWhitener
import com.idphoto.printing.image.BitmapLoader
import com.idphoto.printing.image.ImportedPhoto
import com.idphoto.printing.image.PhotoImporter
import com.idphoto.printing.ui.ImportCropScreen
import com.idphoto.printing.ui.SaveCaptureScreen
import com.idphoto.printing.image.PhotoAdjustments
import com.idphoto.printing.image.PhotoAdjustmentProcessor
import com.idphoto.printing.ui.CapturedPhoto
import com.idphoto.printing.ui.CaptureScreen
import com.idphoto.printing.ui.CombinationScreen
import com.idphoto.printing.ui.IdPhotoTheme
import com.idphoto.printing.ui.PrinterSetupScreen
import com.idphoto.printing.ui.ReviewScreen
import com.idphoto.printing.ui.SheetPreviewScreen
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IdPhotoTheme {
                IdPhotoWorkflow()
            }
        }
    }
}

private enum class WorkflowScreen {
    CAPTURE,
    SAVE_CAPTURE,
    IMPORT,
    REVIEW,
    COMBINATION,
    PREVIEW,
    PRINTER_SETUP,
}

@Composable
private fun IdPhotoWorkflow() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val printerConnection = remember { PrinterConnection.create(context) }
    val printerState by printerConnection.state.collectAsStateWithLifecycle()
    LaunchedEffect(lifecycleOwner, printerConnection) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            printerConnection.watchWifi(context)
        }
    }
    val analyzer = remember { PhotoAnalyzer(context) }
    val backgroundWhitener = remember { BackgroundWhitener() }
    var screen by remember { mutableStateOf(WorkflowScreen.CAPTURE) }
    var capturedPhoto by remember { mutableStateOf<CapturedPhoto?>(null) }
    var pendingCapture by remember { mutableStateOf<CapturedPhoto?>(null) }
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var whiteBackgroundBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var whiteBackgroundEnabled by remember { mutableStateOf(true) }
    var review by remember { mutableStateOf<PhotoReview?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var selectedCombination by remember { mutableStateOf(SheetCombination.DEFAULT) }
    var adjustments by remember { mutableStateOf(PhotoAdjustments.NEUTRAL) }
    var autoAdjust by remember { mutableStateOf(false) }
    var automaticAdjustments by remember { mutableStateOf(PhotoAdjustments.NEUTRAL) }
    var automaticSource by remember { mutableStateOf<Bitmap?>(null) }
    var adjustedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var appliedSource by remember { mutableStateOf<Bitmap?>(null) }
    var appliedSettings by remember { mutableStateOf<PhotoAdjustments?>(null) }
    var adjustmentError by remember { mutableStateOf<String?>(null) }
    val importScope = rememberCoroutineScope()
    var importJob by remember { mutableStateOf<Job?>(null) }
    var importGeneration by remember { mutableIntStateOf(0) }
    var importedPhoto by remember { mutableStateOf<ImportedPhoto?>(null) }
    var importedFaces by remember { mutableStateOf<List<FloatRect>>(emptyList()) }
    var importCrop by remember { mutableStateOf<FloatRect?>(null) }
    var importBusy by remember { mutableStateOf(false) }
    var detectingFaces by remember { mutableStateOf(false) }
    var importError by remember { mutableStateOf<String?>(null) }
    fun openImportedPhoto(uri: Uri) {
            val generation = ++importGeneration
            importJob?.cancel()
            screen = WorkflowScreen.IMPORT
            importBusy = true
            importError = null
            importJob = importScope.launch {
                var opened = false
                try {
                    val photo = PhotoImporter.open(context, uri)
                    importedPhoto?.file?.delete()
                    importedPhoto = photo
                    opened = true
                    importedFaces = emptyList()
                    importCrop = ImportCrop.square(photo.size, photo.size.width / 2f, photo.size.height / 2f,
                        minOf(photo.size.width, photo.size.height).toFloat())
                    importBusy = false
                    detectingFaces = true
                    // Suggestions use the small overview. The chosen crop is decoded from the original file.
                    val found = analyzer.findFaces(photo.preview)
                    val sx = photo.size.width.toFloat() / photo.preview.width
                    val sy = photo.size.height.toFloat() / photo.preview.height
                    importedFaces = found.map { FloatRect(it.left * sx, it.top * sy, it.right * sx, it.bottom * sy) }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    importError = if (opened) "Face suggestions unavailable. You can position the square manually."
                        else error.message ?: "This photo could not be opened. Choose another file."
                } finally {
                    if (generation == importGeneration) {
                        importBusy = false
                        detectingFaces = false
                    }
                }
            }
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) openImportedPhoto(uri)
    }

    val sourceBitmap = if (whiteBackgroundEnabled) {
        whiteBackgroundBitmap ?: originalBitmap
    } else {
        originalBitmap
    }
    val effectiveAdjustments = if (autoAdjust) automaticAdjustments else adjustments
    val adjustmentsReady = (!autoAdjust || automaticSource === sourceBitmap) &&
        (effectiveAdjustments == PhotoAdjustments.NEUTRAL ||
            (appliedSource === sourceBitmap && appliedSettings == effectiveAdjustments))
    val activeBitmap = if (effectiveAdjustments == PhotoAdjustments.NEUTRAL) sourceBitmap
        else if (appliedSource === sourceBitmap) adjustedBitmap ?: sourceBitmap else sourceBitmap

    LaunchedEffect(sourceBitmap, review?.cropPlan) {
        automaticAdjustments = PhotoAdjustments.NEUTRAL
        automaticSource = null
        val source = sourceBitmap ?: return@LaunchedEffect
        val currentReview = review ?: return@LaunchedEffect
        val crop = currentReview.cropPlan?.square ?: return@LaunchedEffect
        automaticAdjustments = withContext(Dispatchers.Default) {
            PhotoAdjustmentProcessor.automatic(source, crop, currentReview.imageSize)
        }
        automaticSource = source
    }
    LaunchedEffect(sourceBitmap, effectiveAdjustments) {
        adjustmentError = null
        val source = sourceBitmap ?: return@LaunchedEffect
        if (effectiveAdjustments == PhotoAdjustments.NEUTRAL) {
            adjustedBitmap = null
            appliedSource = null
            appliedSettings = null
            return@LaunchedEffect
        }
        delay(80) // Coalesce rapid slider updates; only a complete result becomes exportable.
        try {
            val result = withContext(Dispatchers.Default) {
                PhotoAdjustmentProcessor.apply(source, effectiveAdjustments)
            }
            adjustedBitmap = result
            appliedSource = source
            appliedSettings = effectiveAdjustments
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            adjustmentError = "Adjustments could not be applied. Switch Auto on and off, or choose the photo again."
        }
    }

    DisposableEffect(analyzer, backgroundWhitener) {
        onDispose {
            analyzer.close()
            backgroundWhitener.close()
        }
    }

    // Let Android reclaim displayed bitmaps once the renderer and cancellable workers
    // release them. Explicit recycling here races background adjustment processing.

    LaunchedEffect(capturedPhoto) {
        val capture = capturedPhoto ?: return@LaunchedEffect
        val file = capture.file
        isAnalyzing = true
        review = null
        originalBitmap = null
        whiteBackgroundBitmap = null
        whiteBackgroundEnabled = true
        adjustments = PhotoAdjustments.NEUTRAL
        autoAdjust = false
        adjustedBitmap = null
        try {
            val square = requireNotNull(capture.cameraSquare) {
                capture.framingError ?: "The square could not be matched. The original was saved; use Open Photo to reframe it."
            }
            val analyzed = analyzer.analyze(Uri.fromFile(file), square, capture.allowLowResolution)
            val loaded = withContext(Dispatchers.IO) { BitmapLoader.load(file) }
            originalBitmap = loaded
            val checkedReview = analyzed.withDecodedBitmapChecks(loaded)
            review = checkedReview
            if (checkedReview.cropPlan != null) {
                whiteBackgroundBitmap = backgroundWhitener.whiten(loaded)
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            if (originalBitmap == null || review == null) {
                review = PhotoReview(
                    imageSize = ImageSize(0, 0),
                    cropPlan = null,
                    checks = emptyList(),
                    readyToPrint = false,
                    failureMessage = error.message ?: "The captured photo could not be prepared.",
                )
            }
        } finally {
            isAnalyzing = false
        }
    }

    fun retake() {
        originalBitmap = null
        whiteBackgroundBitmap = null
        adjustedBitmap = null
        appliedSource = null
        appliedSettings = null
        adjustments = PhotoAdjustments.NEUTRAL
        autoAdjust = false
        whiteBackgroundEnabled = true
        review = null
        capturedPhoto?.file?.delete()
        capturedPhoto = null
        screen = if (importedPhoto != null) WorkflowScreen.IMPORT else WorkflowScreen.CAPTURE
    }

    BackHandler(enabled = screen != WorkflowScreen.CAPTURE) {
        when (screen) {
            WorkflowScreen.CAPTURE -> Unit
            WorkflowScreen.SAVE_CAPTURE -> Unit
            WorkflowScreen.IMPORT -> {
                ++importGeneration
                importJob?.cancel()
                importBusy = false
                detectingFaces = false
                importedPhoto?.file?.delete()
                importedPhoto = null
                importedFaces = emptyList()
                importCrop = null
                screen = WorkflowScreen.CAPTURE
            }
            WorkflowScreen.REVIEW -> retake()
            WorkflowScreen.COMBINATION -> screen = WorkflowScreen.REVIEW
            WorkflowScreen.PREVIEW -> screen = WorkflowScreen.COMBINATION
            WorkflowScreen.PRINTER_SETUP -> screen = WorkflowScreen.CAPTURE
        }
    }

    when (screen) {
        WorkflowScreen.CAPTURE -> CaptureScreen(
            printerState = printerState,
            onCaptured = { capture ->
                pendingCapture = capture
                screen = WorkflowScreen.SAVE_CAPTURE
            },
            onPrinterSetup = {
                screen = WorkflowScreen.PRINTER_SETUP
            },
            onChoosePhoto = { picker.launch(arrayOf("image/*")) },
        )
        WorkflowScreen.SAVE_CAPTURE -> {
            val capture = requireNotNull(pendingCapture)
            SaveCaptureScreen(capture) { savedUri ->
                pendingCapture = null
                if (capture.isGroup) {
                    openImportedPhoto(savedUri)
                    capture.file.delete()
                } else {
                    capturedPhoto = capture
                    screen = WorkflowScreen.REVIEW
                }
            }
        }
        WorkflowScreen.IMPORT -> ImportCropScreen(
            photo = importedPhoto, faces = importedFaces, crop = importCrop,
            detecting = detectingFaces, busy = importBusy, error = importError,
            onCropChange = { importCrop = it; importError = null },
            onChooseFile = { picker.launch(arrayOf("image/*")) },
            onBack = {
                ++importGeneration
                importJob?.cancel()
                importBusy = false
                detectingFaces = false
                importedPhoto?.file?.delete()
                importedPhoto = null
                importedFaces = emptyList()
                importCrop = null
                screen = WorkflowScreen.CAPTURE
            },
            onUseCrop = {
                val photo = importedPhoto
                val square = importCrop
                if (photo != null && square != null) {
                    val generation = ++importGeneration
                    importJob?.cancel()
                    detectingFaces = false
                    importBusy = true
                    importError = null
                    importJob = importScope.launch {
                        try {
                            val file = PhotoImporter.crop(context, photo, square)
                            val size = withContext(Dispatchers.IO) { BitmapLoader.uprightSize(file) }
                            val side = minOf(size.width, size.height).toFloat()
                            capturedPhoto = CapturedPhoto(file, FloatRect(0f, 0f, side, side), allowLowResolution = true)
                            screen = WorkflowScreen.REVIEW
                        } catch (cancelled: CancellationException) {
                            throw cancelled
                        } catch (error: Exception) {
                            importError = error.message ?: "The crop could not be prepared. Try another photo."
                        } finally {
                            if (generation == importGeneration) importBusy = false
                        }
                    }
                }
            },
        )
        WorkflowScreen.REVIEW -> ReviewScreen(
            bitmap = activeBitmap,
            review = review,
            isLoading = isAnalyzing,
            whiteBackgroundEnabled = whiteBackgroundEnabled,
            whiteBackgroundAvailable = whiteBackgroundBitmap != null,
            onWhiteBackgroundChange = { whiteBackgroundEnabled = it },
            adjustments = effectiveAdjustments,
            autoAdjust = autoAdjust,
            onAdjustmentsChange = { adjustments = it },
            onAutoChange = { adjustments = PhotoAdjustments.NEUTRAL; autoAdjust = it },
            adjustmentsReady = adjustmentsReady,
            adjustmentError = adjustmentError,
            onRetake = ::retake,
            retakeLabel = if (importedPhoto != null) "Reframe" else "Retake",
            onContinue = { screen = WorkflowScreen.COMBINATION },
        )
        WorkflowScreen.COMBINATION -> CombinationScreen(
            selectedCombination = selectedCombination,
            onCombinationSelected = { selectedCombination = it },
            onBack = { screen = WorkflowScreen.REVIEW },
            onPreview = { screen = WorkflowScreen.PREVIEW },
        )
        WorkflowScreen.PREVIEW -> SheetPreviewScreen(
            printerConnection = printerConnection,
            printerState = printerState,
            bitmap = requireNotNull(activeBitmap),
            review = requireNotNull(review),
            combination = selectedCombination,
            onBack = { screen = WorkflowScreen.COMBINATION },
        )
        WorkflowScreen.PRINTER_SETUP -> PrinterSetupScreen(
            printerConnection = printerConnection,
            onBack = { screen = WorkflowScreen.CAPTURE },
        )
    }
}

private fun PhotoReview.withDecodedBitmapChecks(bitmap: Bitmap): PhotoReview {
    val plan = cropPlan ?: return this
    val scaleX = bitmap.width.toFloat() / imageSize.width.coerceAtLeast(1)
    val scaleY = bitmap.height.toFloat() / imageSize.height.coerceAtLeast(1)
    val decodedResolutionSufficient =
        plan.square.width * scaleX >= 600f &&
            plan.square.height * scaleY >= 600f &&
            plan.passport.width * scaleX >= 414f &&
            plan.passport.height * scaleY >= 532f
    val blur = BlurDetector.analyze(bitmap, plan.square, imageSize)

    return copy(
        checks = checks.map { check ->
            when (check.label) {
                "Print resolution" -> if (decodedResolutionSufficient) {
                    check
                } else {
                    check.copy(
                        status = if (allowLowResolution) CheckStatus.WARNING else CheckStatus.FAIL,
                        detail = if (allowLowResolution) "Low resolution: prints may look softer. You can still use this crop."
                        else "The working crop is below 300 dpi. Use a higher-resolution photo or move closer and retake.",
                    )
                }
                "Blur" -> check.copy(
                    status = if (blur.isSharp) CheckStatus.PASS else CheckStatus.FAIL,
                    detail = if (blur.isSharp) {
                        "Sharpness check passed (score ${blur.score.roundToInt()})."
                    } else {
                        "The photo looks soft (score ${blur.score.roundToInt()}; minimum 45). Hold the phone steady, improve the light, and retake."
                    },
                )
                else -> check
            }
        },
        readyToPrint = readyToPrint && (decodedResolutionSufficient || allowLowResolution) && blur.isSharp,
    )
}
