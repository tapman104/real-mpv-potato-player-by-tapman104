package com.tapman104.mpvplayer.player.ui.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapman104.mpvplayer.player.model.DecodeMode

@Composable
fun DecodeModeDialog(
    currentDecodeMode: DecodeMode,
    onSelectMode: (DecodeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Decode Mode",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                DecodeModeOption(
                    mode = DecodeMode.SW,
                    description = "SW — Software decoding, slowest but most compatible",
                    isSelected = currentDecodeMode == DecodeMode.SW,
                    onClick = {
                        onSelectMode(DecodeMode.SW)
                        onDismiss()
                    }
                )
                DecodeModeOption(
                    mode = DecodeMode.HW,
                    description = "HW — Hardware decoding, fast",
                    isSelected = currentDecodeMode == DecodeMode.HW,
                    onClick = {
                        onSelectMode(DecodeMode.HW)
                        onDismiss()
                    }
                )
                DecodeModeOption(
                    mode = DecodeMode.HWPlus,
                    description = "HW+ — Hardware + copy-back, recommended",
                    isSelected = currentDecodeMode == DecodeMode.HWPlus,
                    onClick = {
                        onSelectMode(DecodeMode.HWPlus)
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {},
        containerColor = Color(0xFF1E1E1E)
    )
}

@Composable
private fun DecodeModeOption(
    mode: DecodeMode,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(0xFFFFB300),
                unselectedColor = Color.White.copy(alpha = 0.6f)
            )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = description,
            color = Color.White,
            fontSize = 16.sp
        )
    }
}
