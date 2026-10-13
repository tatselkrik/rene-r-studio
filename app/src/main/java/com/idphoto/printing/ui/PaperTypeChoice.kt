package com.idphoto.printing.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.idphoto.printing.print.PaperType

@Composable
fun PaperTypeChoice(paper: PaperType, enabled: Boolean, onChange: (PaperType) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text("Paper")
        PaperType.entries.forEach { choice ->
            FilterChip(selected = paper == choice, onClick = { onChange(choice) },
                enabled = enabled, modifier = Modifier.weight(1f), label = { Text(choice.label) })
        }
    }
}
