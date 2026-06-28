package com.tapman104.mpvplayer.player.controls

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tapman104.mpvplayer.player.model.AspectRatioMode

@Composable
fun AspectRatioCycleButton(
    currentMode: AspectRatioMode,
    onCycle: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onCycle, modifier = modifier) {
        Icon(
            imageVector = Icons.Outlined.AspectRatio,
            contentDescription = "Aspect ratio",
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
    }
}
