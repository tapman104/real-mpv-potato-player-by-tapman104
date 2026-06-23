package com.tapman104.mpvplayer.ui.screens

import android.view.SurfaceView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.tapman104.mpvplayer.model.PlayerState
import com.tapman104.mpvplayer.model.PlaylistState
import com.tapman104.mpvplayer.ui.components.GestureOverlay
import com.tapman104.mpvplayer.ui.overlay.PlayerOverlay

@Composable
fun PlayerScreen(
    playerState: PlayerState,
    playlistState: PlaylistState,
    surfaceView: SurfaceView,
    onTogglePlay: () -> Unit,
    onSeek: (Long) -> Unit,
    onSeekRelative: (Long) -> Unit = {},
    onOpenFile: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onSpeedOverride: (Float) -> Unit = {},
    onSpeedRestore: () -> Unit = {},
    onSelectAudioTrack: (Int) -> Unit,
    onSelectSubtitleTrack: (Int) -> Unit,
    onSubtitleAppearance: (size: Float, position: Float) -> Unit = { _, _ -> },
    onSubtitleReset: () -> Unit = {},
    // Resume dialog
    showResumeDialog: Boolean = false,
    resumePositionMs: Long = 0L,
    onResume: () -> Unit = {},
    onStartOver: () -> Unit = {},
    onDismissResume: () -> Unit = {},
    // Auto-subtitle
    onAutoSelectSubtitle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var controlsVisible by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .then(modifier)
    ) {
        AndroidView(
            factory = { 
                surfaceView.apply {
                    setOnTouchListener { _, _ -> false } // don't consume — let Compose handle it
                }
            },
            // update is called on every recomposition — keeping the SurfaceView
            // reference alive prevents Compose from recycling/destroying the
            // SurfaceHolder that mpv renders into (which causes a black screen).
            update = { view -> 
                view.setOnTouchListener { _, _ -> false }
            },
            modifier = Modifier.fillMaxSize()
        )

        GestureOverlay(
            onSeekForward = { onSeekRelative(10_000L) },
            onSeekBackward = { onSeekRelative(-10_000L) },
            onSpeedOverride = onSpeedOverride,
            onSpeedRestore = onSpeedRestore,
            onToggleControls = { controlsVisible = !controlsVisible },
            modifier = Modifier.fillMaxSize()
        )

        PlayerOverlay(
            playerState = playerState,
            playlistState = playlistState,
            controlsVisible = controlsVisible,
            onControlsVisibilityChange = { controlsVisible = it },
            onTogglePlay = onTogglePlay,
            onSeek = onSeek,
            onOpenFile = onOpenFile,
            onSpeedChange = onSpeedChange,
            onSelectAudioTrack = onSelectAudioTrack,
            onSelectSubtitleTrack = onSelectSubtitleTrack,
            onSubtitleAppearance = onSubtitleAppearance,
            onSubtitleReset = onSubtitleReset,
            showResumeDialog = showResumeDialog,
            resumePositionMs = resumePositionMs,
            onResume = onResume,
            onStartOver = onStartOver,
            onDismissResume = onDismissResume,
            onAutoSelectSubtitle = onAutoSelectSubtitle,
            modifier = Modifier.fillMaxSize()
        )
    }
}

