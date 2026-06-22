package com.tapman104.mpvplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GestureOverlay(
    onSeekForward: () -> Unit,
    onSeekBackward: () -> Unit,
    onSpeedOverride: (Float) -> Unit,
    onSpeedRestore: () -> Unit,
    modifier: Modifier = Modifier
) {
    var seekLabel by remember { mutableStateOf("") }
    var labelTrigger by remember { mutableIntStateOf(0) }
    var isLongPressing by remember { mutableStateOf(false) }

    LaunchedEffect(labelTrigger) {
        if (labelTrigger > 0) {
            delay(800)
            seekLabel = ""
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                coroutineScope {
                    launch {
                        detectTapGestures(
                            onDoubleTap = { offset ->
                                if (offset.x < size.width / 2) {
                                    onSeekBackward()
                                    seekLabel = "-10s"
                                } else {
                                    onSeekForward()
                                    seekLabel = "+10s"
                                }
                                labelTrigger++
                            },
                            onLongPress = {
                                isLongPressing = true
                                onSpeedOverride(2.0f)
                            }
                        )
                    }
                    launch {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                if (event.type == PointerEventType.Release && isLongPressing) {
                                    isLongPressing = false
                                    onSpeedRestore()
                                }
                            }
                        }
                    }
                }
            }
    ) {
        val displayLabel = if (isLongPressing) "2×" else seekLabel

        if (displayLabel.isNotEmpty()) {
            Text(
                text = displayLabel,
                color = Color.White,
                fontSize = if (isLongPressing) 22.sp else 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}
