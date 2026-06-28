package com.tapman104.mpvplayer.player.playback

import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapman104.mpvplayer.player.state.PlayerState
import com.tapman104.mpvplayer.player.state.PlaylistState
import com.tapman104.mpvplayer.player.controls.PlayerTopBar
import com.tapman104.mpvplayer.player.controls.PlayerBottomBar
import com.tapman104.mpvplayer.player.dialog.SubtitleAppearanceDialog
import com.tapman104.mpvplayer.player.model.AspectRatioMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PlayerOverlay(
    playerState: PlayerState,
    playlistState: PlaylistState,
    controlsVisible: Boolean,
    onControlsVisibilityChange: (Boolean) -> Unit,
    onTogglePlay: () -> Unit,
    onSeek: (Long) -> Unit,
    onOpenFile: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onSelectAudioTrack: (Int) -> Unit,
    onSelectSubtitleTrack: (Int) -> Unit,
    onSubtitleAppearance: (size: Float, position: Float) -> Unit,
    onSubtitleReset: () -> Unit,
    onDecodeModeChange: (com.tapman104.mpvplayer.player.model.DecodeMode) -> Unit,
    onAspectRatioChange: (com.tapman104.mpvplayer.player.model.AspectRatioMode) -> Unit,
    // Auto-subtitle
    onAutoSelectSubtitle: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showSubtitleAppearance by remember { mutableStateOf(false) }
    var showAspectRatioDialog by remember { mutableStateOf(false) }
    var isDraggingSeekbar by remember { mutableStateOf(false) }
    var isTopBarDialogOpen by remember { mutableStateOf(false) }
    var aspectRatioFlashLabel by remember { mutableStateOf("") }
    var aspectRatioFlashTrigger by remember { mutableIntStateOf(0) }

    val isAnyDialogOpen = isTopBarDialogOpen || showSubtitleAppearance || showAspectRatioDialog

    LaunchedEffect(aspectRatioFlashTrigger) {
        if (aspectRatioFlashTrigger > 0) {
            delay(150)
            aspectRatioFlashLabel = ""
        }
    }

    LaunchedEffect(controlsVisible, isDraggingSeekbar, isAnyDialogOpen) {
        if (controlsVisible && !isDraggingSeekbar && !isAnyDialogOpen) {
            delay(3000L)
            onControlsVisibilityChange(false)
        }
    }

    // Auto-select subtitle once per file load (fires when non-empty tracks arrive).
    val subtitleTracks = playerState.subtitleTracks
    LaunchedEffect(subtitleTracks) {
        if (subtitleTracks.isNotEmpty()) {
            onAutoSelectSubtitle()
        }
    }

    val context = LocalContext.current
    var fileName by remember(playlistState.currentUri) {
        mutableStateOf(
            playlistState.currentUri?.let { Uri.parse(it).lastPathSegment ?: it } ?: "No file loaded"
        )
    }

    LaunchedEffect(playlistState.currentUri) {
        val uriStr = playlistState.currentUri
        if (uriStr != null) {
            val uri = Uri.parse(uriStr)
            if (uri.scheme == "content") {
                withContext(Dispatchers.IO) {
                    try {
                        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                            if (cursor.moveToFirst()) {
                                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                if (index != -1) {
                                    cursor.getString(index)?.let { fileName = it }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            PlayerTopBar(
                fileName = fileName,
                onBack = {},
                onOpenFile = onOpenFile,
                onSpeedChange = onSpeedChange,
                audioTracks = playerState.audioTracks,
                selectedAudioTrackId = playerState.selectedAudioTrackId,
                subtitleTracks = playerState.subtitleTracks,
                selectedSubtitleTrackId = playerState.selectedSubtitleTrackId,
                onSelectAudioTrack = onSelectAudioTrack,
                onSelectSubtitleTrack = onSelectSubtitleTrack,
                onSubtitleAppearanceClick = { showSubtitleAppearance = true },
                currentDecodeMode = playerState.decodeMode,
                onDecodeModeChange = onDecodeModeChange,
                currentAspectRatio = playerState.aspectRatio,
                onAspectRatioClick = { showAspectRatioDialog = true },
                onDialogOpenChange = { isTopBarDialogOpen = it },
                onSettingsClick = onSettingsClick
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
                onTogglePlay = onTogglePlay,
                onDraggingChange = { isDraggingSeekbar = it },
                currentAspectRatio = playerState.aspectRatio,
                onAspectRatioCycle = {
                    val modes = com.tapman104.mpvplayer.player.model.AspectRatioMode.entries
                    val next = modes[(modes.indexOf(playerState.aspectRatio) + 1) % modes.size]
                    onAspectRatioChange(next)
                    aspectRatioFlashLabel = next.displayName
                    aspectRatioFlashTrigger++
                }
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

        if (aspectRatioFlashLabel.isNotEmpty()) {
            Text(
                text = aspectRatioFlashLabel,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
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

        if (showAspectRatioDialog) {
            com.tapman104.mpvplayer.player.dialog.AspectRatioDialog(
                currentMode = playerState.aspectRatio,
                onSelectMode = { 
                    onAspectRatioChange(it)
                    showAspectRatioDialog = false 
                },
                onDismiss = { showAspectRatioDialog = false }
            )
        }

    }
}


