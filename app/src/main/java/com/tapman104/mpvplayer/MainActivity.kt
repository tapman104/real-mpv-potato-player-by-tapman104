package com.tapman104.mpvplayer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import com.tapman104.mpvplayer.ui.screens.HomeScreen
import com.tapman104.mpvplayer.ui.theme.MpvPlayerTheme

class MainActivity : ComponentActivity() {
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val intent = Intent(this, PlayerActivity::class.java).apply {
                data = it
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MpvPlayerTheme {
                HomeScreen(onOpenFile = { filePickerLauncher.launch(arrayOf("video/*")) })
            }
        }
    }
}
