package com.tapman104.mpvplayer.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SubtitleAppearanceDialog(
    initialSize: Float,
    initialPosition: Float,
    onApply: (size: Float, position: Float) -> Unit,
    onDismiss: () -> Unit,
    onReset: () -> Unit = {},
) {
    var size by remember { mutableStateOf(initialSize) }
    var position by remember { mutableStateOf(initialPosition) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2A2A2A),
        title = {
            Text(
                text = "Subtitle Appearance",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                Text("Size", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Size", color = Color.White, modifier = Modifier.width(72.dp), fontSize = 14.sp)
                    Slider(
                        value = size,
                        onValueChange = { size = it },
                        valueRange = 0.5f..2.0f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f),
                        ),
                    )
                    Text(
                        text = "${"%.1f".format(size)}×",
                        color = Color.White,
                        modifier = Modifier
                            .width(40.dp)
                            .padding(start = 8.dp),
                        fontSize = 13.sp,
                    )
                }

                Text(
                    "Position  ↑ higher = further from bottom",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Position", color = Color.White, modifier = Modifier.width(72.dp), fontSize = 14.sp)
                    Slider(
                        value = position,
                        onValueChange = { position = it },
                        valueRange = 0f..0.5f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f),
                        ),
                    )
                    Text(
                        text = "${"%.2f".format(position)}",
                        color = Color.White,
                        modifier = Modifier
                            .width(40.dp)
                            .padding(start = 8.dp),
                        fontSize = 13.sp,
                    )
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = {
                    onReset()
                    onDismiss()
                }) {
                    Text("Reset", color = Color.White.copy(alpha = 0.7f))
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.White)
                }
                Button(
                    onClick = { onApply(size, position) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                ) {
                    Text("Apply", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {},
    )
}
