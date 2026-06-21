package com.tapman104.mpvplayer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PlayerBottomBar(
    currentPositionMs: Long,
    durationMs: Long,
    demuxerCacheTimeMs: Long,
    isPlaying: Boolean,
    onSeek: (Long) -> Unit,
    onTogglePlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        PlayerSeekBar(
            currentPositionMs = currentPositionMs,
            durationMs = durationMs,
            demuxerCacheTimeMs = demuxerCacheTimeMs,
            onSeek = onSeek,
            modifier = Modifier.fillMaxWidth()
        )
        PlayPauseButton(
            isPlaying = isPlaying,
            onToggle = onTogglePlay,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
