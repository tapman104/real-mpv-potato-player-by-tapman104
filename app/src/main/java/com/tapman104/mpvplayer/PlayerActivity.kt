package com.tapman104.mpvplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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

    /**
     * Pauses playback when the screen turns off (power button).
     * Using a BroadcastReceiver rather than onPause so that dialogs,
     * notifications, and picture-in-picture transitions do NOT pause playback.
     */
    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF) {
                viewModel.pausePlayback()
            }
        }
    }

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.loadAndPlay(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen on during playback (power button still fires ACTION_SCREEN_OFF)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Edge-to-edge layout
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Immersive sticky — hide system bars, show transiently on swipe
        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }

        // Register screen-off receiver to pause audio on lock
        registerReceiver(screenOffReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))

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
                    onOpenFile = { filePickerLauncher.launch(arrayOf("video/*")) },
                    onSpeedChange = { viewModel.setSpeed(it) },
                    onSelectAudioTrack = { viewModel.setAudioTrack(it) },
                    onSelectSubtitleTrack = { viewModel.setSubtitleTrack(it) },
                    onSubtitleAppearance = { size, position -> viewModel.setSubtitleAppearance(size, position) },
                    onSubtitleReset = { viewModel.resetSubtitleAppearance() }
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

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenOffReceiver)
    }
}
