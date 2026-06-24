package com.tapman104.mpvplayer.player.ui.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
        title = {
            Text(
                text = "Subtitle Appearance",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Size", 
                    color = Color.White.copy(alpha = 0.7f), 
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Slider(
                        value = size,
                        onValueChange = { size = it },
                        valueRange = 0.5f..2.0f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFFB300),
                            activeTrackColor = Color(0xFFFFB300),
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f),
                        ),
                    )
                    Text(
                        text = "${"%.1f".format(size)}×",
                        color = Color.White,
                        modifier = Modifier
                            .width(48.dp)
                            .padding(start = 12.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = "Position (higher = further from bottom)",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Slider(
                        value = position,
                        onValueChange = { position = it },
                        valueRange = 0f..0.5f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFFB300),
                            activeTrackColor = Color(0xFFFFB300),
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f),
                        ),
                    )
                    Text(
                        text = "${"%.2f".format(position)}",
                        color = Color.White,
                        modifier = Modifier
                            .width(48.dp)
                            .padding(start = 12.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
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
                    Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                }
                Button(
                    onClick = {
                        onApply(size, position)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Apply", color = Color(0xFF1E1E1E), fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {},
        containerColor = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 8.dp
    )
}
