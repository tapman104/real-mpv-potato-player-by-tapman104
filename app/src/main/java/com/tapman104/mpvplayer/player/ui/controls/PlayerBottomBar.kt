package com.tapman104.mpvplayer.player.ui.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PlayerBottomBar(
    currentPositionMs: Long,
    durationMs: Long,
    demuxerCacheTimeMs: Long,
    isPlaying: Boolean,
    onSeek: (Long) -> Unit,
    onTogglePlay: () -> Unit,
    onDraggingChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                )
            )
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        PlayerSeekBar(
            currentPositionMs = currentPositionMs,
            durationMs = durationMs,
            demuxerCacheTimeMs = demuxerCacheTimeMs,
            onSeek = onSeek,
            onDraggingChange = onDraggingChange,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        PlayPauseButton(
            isPlaying = isPlaying,
            onToggle = onTogglePlay,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
