package com.tapman104.mpvplayer.ui.overlay

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapman104.mpvplayer.state.PlayerState
import com.tapman104.mpvplayer.state.PlaylistState
import com.tapman104.mpvplayer.ui.components.PlayerTopBar
import com.tapman104.mpvplayer.ui.components.PlayerBottomBar
import com.tapman104.mpvplayer.ui.dialogs.SubtitleAppearanceDialog
import kotlinx.coroutines.delay

@Composable
fun PlayerOverlay(
    playerState: PlayerState,
    playlistState: PlaylistState,
    onTogglePlay: () -> Unit,
    onSeek: (Long) -> Unit,
    onOpenFile: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onSelectAudioTrack: (Int) -> Unit,
    onSelectSubtitleTrack: (Int) -> Unit,
    onSubtitleAppearance: (size: Float, position: Float) -> Unit,
    onSubtitleReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    var controlsVisible by remember { mutableStateOf(true) }
    var showSubtitleAppearance by remember { mutableStateOf(false) }

    LaunchedEffect(controlsVisible) {
        if (controlsVisible) {
            delay(3000L)
            controlsVisible = false
        }
    }

    val fileName = playlistState.currentUri
        ?.let { Uri.parse(it).lastPathSegment ?: it }
        ?: "No file loaded"

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { controlsVisible = true }
    ) {
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            PlayerTopBar(
                fileName = fileName,
                speed = playerState.speed,
                onBack = {},
                onOpenFile = onOpenFile,
                onSpeedChange = onSpeedChange,
                audioTracks = playerState.audioTracks,
                selectedAudioTrackId = playerState.selectedAudioTrackId,
                subtitleTracks = playerState.subtitleTracks,
                selectedSubtitleTrackId = playerState.selectedSubtitleTrackId,
                onSelectAudioTrack = onSelectAudioTrack,
                onSelectSubtitleTrack = onSelectSubtitleTrack,
            )
        }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            PlayerBottomBar(
                currentPositionMs = playerState.currentPositionMs,
                durationMs = playerState.durationMs,
                demuxerCacheTimeMs = playerState.demuxerCacheTimeMs,
                isPlaying = playerState.isPlaying,
                onSeek = onSeek,
                onTogglePlay = onTogglePlay
            )
        }

        if (playerState.isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        playerState.error?.let { errorMessage ->
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
        }

        if (showSubtitleAppearance) {
            SubtitleAppearanceDialog(
                initialSize = playerState.subtitleSize,
                initialPosition = playerState.subtitlePosition,
                onApply = { size, position ->
                    onSubtitleAppearance(size, position)
                    showSubtitleAppearance = false
                },
                onReset = {
                    onSubtitleReset()
                    showSubtitleAppearance = false
                },
                onDismiss = { showSubtitleAppearance = false },
            )
        }
    }
}
