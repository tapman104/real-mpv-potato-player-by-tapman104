package com.tapman104.mpvplayer.ui.screens

import android.view.SurfaceView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.tapman104.mpvplayer.state.PlayerState
import com.tapman104.mpvplayer.state.PlaylistState
import com.tapman104.mpvplayer.ui.overlay.PlayerOverlay

@Composable
fun PlayerScreen(
    playerState: PlayerState,
    playlistState: PlaylistState,
    surfaceView: SurfaceView,
    onTogglePlay: () -> Unit,
    onSeek: (Long) -> Unit,
    onOpenFile: () -> Unit,
    onSelectAudioTrack: (Int) -> Unit,
    onSelectSubtitleTrack: (Int) -> Unit,
    onSubtitleAppearance: (size: Float, position: Float) -> Unit = { _, _ -> },
    onSubtitleReset: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .then(modifier)
    ) {
        AndroidView(
            factory = { surfaceView },
            // update is called on every recomposition — keeping the SurfaceView
            // reference alive prevents Compose from recycling/destroying the
            // SurfaceHolder that mpv renders into (which causes a black screen).
            update = { /* intentional no-op: reuse existing SurfaceView */ },
            modifier = Modifier.fillMaxSize()
        )

        PlayerOverlay(
            playerState = playerState,
            playlistState = playlistState,
            onTogglePlay = onTogglePlay,
            onSeek = onSeek,
            onOpenFile = onOpenFile,
            onSelectAudioTrack = onSelectAudioTrack,
            onSelectSubtitleTrack = onSelectSubtitleTrack,
            onSubtitleAppearance = onSubtitleAppearance,
            onSubtitleReset = onSubtitleReset,
            modifier = Modifier.fillMaxSize()
        )
    }
}
