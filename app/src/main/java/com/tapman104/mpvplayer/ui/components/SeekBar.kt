package com.tapman104.mpvplayer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlayerSeekBar(
    currentPositionMs: Long,
    durationMs: Long,
    demuxerCacheTimeMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragPositionMs by remember { mutableStateOf(0L) }

    val displayPositionMs = if (isDragging) dragPositionMs else currentPositionMs
    val fraction = if (durationMs > 0L) (displayPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    // TODO: cache bar — demuxerCacheTimeMs is kept but not rendered visually yet

    Column(modifier = modifier) {
        Slider(
            value = fraction,
            onValueChange = {
                isDragging = true
                dragPositionMs = (it * durationMs).toLong()
            },
            onValueChangeFinished = {
                onSeek(dragPositionMs)
                isDragging = false
            },
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            ),
            modifier = Modifier.padding(horizontal = 0.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatMs(displayPositionMs),
                color = Color.White,
                fontSize = 13.sp
            )
            Text(
                text = formatMs(durationMs),
                color = Color.White,
                fontSize = 13.sp
            )
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
