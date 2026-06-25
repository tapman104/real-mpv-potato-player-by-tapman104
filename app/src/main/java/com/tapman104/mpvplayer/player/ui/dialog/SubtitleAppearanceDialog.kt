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
        containerColor = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 0.dp,
        title = {
            Text(
                text = "Subtitle Appearance",
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 17.sp,
            )
        },
        text = {
            Column {
                Text(
                    "Size",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Size", color = Color.White.copy(alpha = 0.45f), modifier = Modifier.width(52.dp), fontSize = 13.sp)
                    Slider(
                        value = size,
                        onValueChange = { size = it },
                        valueRange = 0.5f..2.0f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.White.copy(alpha = 0.12f),
                        ),
                    )
                    Text(
                        text = "${"%.1f".format(size)}×",
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier
                            .width(40.dp)
                            .padding(start = 8.dp),
                        fontSize = 13.sp,
                    )
                }

                Column(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) {
                    Text("Position", fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f))
                    Text("↑ higher = further from bottom", fontSize = 11.sp, color = Color.White.copy(alpha = 0.25f))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Position", color = Color.White.copy(alpha = 0.45f), modifier = Modifier.width(52.dp), fontSize = 13.sp)
                    Slider(
                        value = position,
                        onValueChange = { position = it },
                        valueRange = 0f..0.5f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.White.copy(alpha = 0.12f),
                        ),
                    )
                    Text(
                        text = "${"%.2f".format(position)}",
                        color = Color.White.copy(alpha = 0.6f),
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
                    Text("Reset", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp)
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.55f), fontSize = 13.sp)
                }
                Button(
                    onClick = { onApply(size, position) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Apply", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        },
        dismissButton = {},
    )
}
