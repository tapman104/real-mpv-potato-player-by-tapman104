package com.tapman104.mpvplayer.ui.screens

import android.net.Uri
import android.view.SurfaceView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.tapman104.mpvplayer.state.PlayerState
import com.tapman104.mpvplayer.state.PlaylistState
import com.tapman104.mpvplayer.ui.components.PlayPauseButton
import com.tapman104.mpvplayer.ui.components.PlayerSeekBar

@Composable
fun PlayerScreen(
    playerState: PlayerState,
    playlistState: PlaylistState,
    surfaceView: SurfaceView,
    onTogglePlay: () -> Unit,
    onSeek: (Long) -> Unit,
    onOpenFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    var controlsVisible by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .then(modifier)
    ) {
        // ── Video surface ──────────────────────────────────────────────────────
        AndroidView(
            factory = { surfaceView },
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { controlsVisible = !controlsVisible }
        )

        // ── Top bar ────────────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = controlsVisible,
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            val fileName = playlistState.currentUri
                ?.let { Uri.parse(it).lastPathSegment ?: it }
                ?: "No file loaded"

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = fileName,
                    color = Color.White,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onOpenFile) {
                    Icon(
                        imageVector = Icons.Filled.FolderOpen,
                        contentDescription = "Open file",
                        tint = Color.White
                    )
                }
            }
        }

        // ── Bottom bar ─────────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = controlsVisible,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                PlayerSeekBar(
                    currentPositionMs = playerState.currentPositionMs,
                    durationMs = playerState.durationMs,
                    demuxerCacheTimeMs = playerState.demuxerCacheTimeMs,
                    onSeek = onSeek,
                    modifier = Modifier.fillMaxWidth()
                )
                PlayPauseButton(
                    isPlaying = playerState.isPlaying,
                    onToggle = onTogglePlay,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        // ── Loading spinner ────────────────────────────────────────────────────
        if (playerState.isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // ── Error text ─────────────────────────────────────────────────────────
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
    }
}
