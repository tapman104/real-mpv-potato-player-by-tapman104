package com.tapman104.mpvplayer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.SurfaceView
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.tapman104.mpvplayer.ui.screens.PlayerScreen
import com.tapman104.mpvplayer.ui.theme.MpvPlayerTheme
import com.tapman104.mpvplayer.viewmodel.PlayerViewModel
import com.tapman104.mpvplayer.viewmodel.PlayerViewModelFactory
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class PlayerActivity : ComponentActivity() {

    private val viewModel: PlayerViewModel by viewModels {
        PlayerViewModelFactory(this)
    }

    private lateinit var surfaceView: SurfaceView

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.loadAndPlay(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen on during playback
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Edge-to-edge layout
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Immersive sticky — hide system bars, show transiently on swipe
        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }

        // Create the SurfaceView that mpv will render into
        surfaceView = SurfaceView(this)

        // Register MpvSurface as the holder callback BEFORE setContent
        surfaceView.holder.addCallback(viewModel.controller.surface)

        setContent {
            MpvPlayerTheme {
                PlayerScreen(
                    playerState = viewModel.playerState.collectAsStateWithLifecycle().value,
                    playlistState = viewModel.playlistState.collectAsStateWithLifecycle().value,
                    surfaceView = surfaceView,
                    onTogglePlay = { viewModel.togglePlay() },
                    onSeek = { viewModel.seekTo(it) },
                    onOpenFile = { filePickerLauncher.launch(arrayOf("video/*")) }
                )
            }
        }

        // Handle direct launch from a file manager or intent
        intent.data?.let { viewModel.loadAndPlay(it) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.data?.let { viewModel.loadAndPlay(it) }
    }
}
