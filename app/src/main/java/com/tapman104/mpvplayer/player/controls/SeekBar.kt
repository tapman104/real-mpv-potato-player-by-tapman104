package com.tapman104.mpvplayer.player.controls

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
    onDraggingChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragPositionMs by remember { mutableStateOf(0L) }

    val displayPositionMs = if (isDragging) dragPositionMs else currentPositionMs
    val fraction = if (durationMs > 0L) (displayPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    // TODO: cache bar — demuxerCacheTimeMs is kept but not rendered visually yet

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatMs(displayPositionMs),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            fontSize = 12.sp,
            modifier = Modifier.widthIn(min = 48.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Start
        )
        Spacer(Modifier.width(8.dp))
        Slider(
            value = fraction,
            onValueChange = {
                if (!isDragging) onDraggingChange(true)
                isDragging = true
                dragPositionMs = (it * durationMs).toLong()
            },
            onValueChangeFinished = {
                onSeek(dragPositionMs)
                isDragging = false
                onDraggingChange(false)
            },
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.onPrimary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
            ),
            modifier = Modifier.weight(1f).height(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = formatMs(durationMs),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            fontSize = 12.sp,
            modifier = Modifier.widthIn(min = 48.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
