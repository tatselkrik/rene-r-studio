package com.idphoto.printing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.idphoto.printing.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PrintModeScreen(onIdPhoto: () -> Unit, onLandscape: () -> Unit, onPrinterSetup: () -> Unit) {
    Column(Modifier.fillMaxSize().background(CanvasCream).safeDrawingPadding().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(28.dp))
        Button(onIdPhoto, Modifier.fillMaxWidth().height(60.dp)) { Text("ID Photo") }
        Spacer(Modifier.height(12.dp))
        Button(onLandscape, Modifier.fillMaxWidth().height(60.dp)) { Text("Landscape Photo") }
        Spacer(Modifier.height(28.dp))
        OutlinedButton(onPrinterSetup, Modifier.fillMaxWidth().height(50.dp)) { Text("Printer Setup") }
    }
}
