package com.tapman104.mpvplayer.player.controls

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapman104.mpvplayer.player.model.AspectRatioMode
import kotlinx.coroutines.delay

@Composable
fun AspectRatioCycleButton(
    currentMode: AspectRatioMode,
    onCycle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLabel by remember { mutableStateOf(false) }

    LaunchedEffect(currentMode) {
        if (showLabel) {
            // reset timer on rapid taps
            showLabel = false
            delay(50)
        }
        showLabel = true
        delay(1500)
        showLabel = false
    }

    Box(modifier = modifier) {
        IconButton(onClick = onCycle) {
            Icon(
                imageVector = Icons.Outlined.AspectRatio,
                contentDescription = "Aspect ratio",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        AnimatedVisibility(
            visible = showLabel,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text(
                    text = when (currentMode) {
                        AspectRatioMode.DEFAULT -> "Default"
                        AspectRatioMode.FIT     -> "Fit"
                        AspectRatioMode.CROP    -> "Crop"
                        AspectRatioMode.STRETCH -> "Stretch"
                    },
                    color = Color.White,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
