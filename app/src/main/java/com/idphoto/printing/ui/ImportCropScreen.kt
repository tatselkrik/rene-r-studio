package com.idphoto.printing.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.idphoto.printing.core.FloatRect
import com.idphoto.printing.core.ImportCrop
import com.idphoto.printing.core.CropPlanner
import com.idphoto.printing.image.ImportedPhoto
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun ImportCropScreen(
    photo: ImportedPhoto?,
    crop: FloatRect?,
    busy: Boolean,
    error: String?,
    onCropChange: (FloatRect) -> Unit,
    onChooseFile: () -> Unit,
    onBack: () -> Unit,
    onUseCrop: () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(CanvasCream).safeDrawingPadding().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Adjust framing", style = MaterialTheme.typography.titleLarge)
        Text("Drag the square or tap to center it. Leave space above the hair.",
            style = MaterialTheme.typography.bodySmall)
        Box(Modifier.weight(1f).fillMaxWidth().padding(vertical = 12.dp),
            contentAlignment = Alignment.Center) {
            if (photo != null && crop != null) {
                var canvasSize by remember { mutableStateOf(IntSize.Zero) }
                val scale = min(canvasSize.width.toFloat() / photo.size.width,
                    canvasSize.height.toFloat() / photo.size.height)
                val origin = Offset((canvasSize.width - photo.size.width * scale) / 2,
                    (canvasSize.height - photo.size.height * scale) / 2)
                val currentCrop by rememberUpdatedState(crop)
                val image = remember(photo) { photo.preview.asImageBitmap() }
                Canvas(Modifier.fillMaxSize().onSizeChanged { canvasSize = it }
                    .semantics { contentDescription = "Photo with adjustable square and passport side guides" }
                    .pointerInput(photo, scale, origin, busy) {
                        detectTapGestures { point ->
                            if (busy || scale <= 0) return@detectTapGestures
                            val x = (point.x - origin.x) / scale
                            val y = (point.y - origin.y) / scale
                            onCropChange(ImportCrop.square(photo.size, x, y, currentCrop.width))
                        }
                    }.pointerInput(photo, scale, origin, busy) {
                        detectDragGestures { change, amount ->
                            if (!busy && scale > 0) {
                                change.consume()
                                onCropChange(ImportCrop.square(photo.size,
                                    currentCrop.centerX + amount.x / scale,
                                    currentCrop.centerY + amount.y / scale, currentCrop.width))
                            }
                        }
                    }) {
                    if (scale <= 0) return@Canvas
                    val display = Size(photo.size.width * scale, photo.size.height * scale)
                    drawImage(image, dstOffset = IntOffset(origin.x.roundToInt(), origin.y.roundToInt()),
                        dstSize = IntSize(display.width.roundToInt(), display.height.roundToInt()))
                    val left = origin.x + crop.left * scale
                    val top = origin.y + crop.top * scale
                    val side = crop.width * scale
                    val shade = Color.Black.copy(alpha = 0.5f)
                    drawRect(shade, origin, Size(display.width, (top - origin.y).coerceAtLeast(0f)))
                    drawRect(shade, Offset(origin.x, top + side), Size(display.width,
                        (origin.y + display.height - top - side).coerceAtLeast(0f)))
                    drawRect(shade, Offset(origin.x, top), Size((left - origin.x).coerceAtLeast(0f), side))
                    drawRect(shade, Offset(left + side, top), Size(
                        (origin.x + display.width - left - side).coerceAtLeast(0f), side))
                    drawRect(Color.White, Offset(left, top), Size(side, side), style = Stroke(3.dp.toPx()))
                    val inset = side * (1f - 35f / 45f) / 2
                    for (x in listOf(left + inset, left + side - inset)) {
                        drawLine(Color.White.copy(alpha = 0.7f), Offset(x, top), Offset(x, top + side), 1.dp.toPx())
                    }
                }
            }
            if (busy) CircularProgressIndicator()
        }
        if (photo != null && crop != null) {
            val plan = CropPlanner.plan(photo.size, crop)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PhotoCrop(photo.preview, plan.square, photo.size, Modifier.size(88.dp))
                PhotoCrop(photo.preview, plan.passport, photo.size, Modifier.height(88.dp).aspectRatio(35f / 45f))
            }
            Text("Crop size", style = MaterialTheme.typography.bodyMedium)
            val maxSide = min(photo.size.width, photo.size.height).toFloat()
            val minSide = min(100f, maxSide)
            if (maxSide > minSide) Slider(value = crop.width.coerceIn(minSide, maxSide),
                onValueChange = { onCropChange(ImportCrop.square(photo.size, crop.centerX, crop.centerY, it)) },
                valueRange = minSide..maxSide, enabled = !busy,
                modifier = Modifier.semantics { contentDescription = "Crop size" })
            Text(if (crop.width < 600f) "Low resolution: prints may look softer."
                else "Inner lines show the passport sides.", style = MaterialTheme.typography.bodySmall,
                color = if (crop.width < 600f) Color(0xFF9A6700) else Muted)
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onChooseFile, enabled = !busy, modifier = Modifier.fillMaxWidth().height(48.dp)) { Text("Choose another photo") }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onBack, enabled = !busy, modifier = Modifier.weight(1f).height(48.dp)) { Text("Camera") }
            Button(onClick = onUseCrop, enabled = !busy && photo != null && crop != null,
                modifier = Modifier.weight(1f).height(48.dp)) { Text("Use crop") }
        }
    }
}
