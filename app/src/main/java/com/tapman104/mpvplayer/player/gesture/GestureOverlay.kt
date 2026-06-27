package com.tapman104.mpvplayer.player.gesture

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.media.AudioManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material.icons.rounded.VolumeDown
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.pow

@Composable
fun GestureOverlay(
    onSeekForward: () -> Unit,
    onSeekBackward: () -> Unit,
    onSpeedOverride: (Float) -> Unit,
    onSpeedRestore: () -> Unit,
    onToggleControls: () -> Unit,
    // New seek/zoom/pan parameters (all have defaults so existing call sites compile)
    currentPositionMs: Long = 0L,
    durationMs: Long = 0L,
    currentZoom: Float = 0f,
    currentPanX: Float = 0f,
    currentPanY: Float = 0f,
    onSeekTo: (Long) -> Unit = {},
    onPauseForSeek: () -> Unit = {},
    onResumeAfterSeek: () -> Unit = {},
    onZoomChange: (Float) -> Unit = {},
    onPanChange: (Float, Float) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var seekLabel by remember { mutableStateOf("") }
    var labelTrigger by remember { mutableIntStateOf(0) }
    var isLongPressing by remember { mutableStateOf(false) }

    // Labels for the new gestures
    var seekScrubLabel by remember { mutableStateOf("") }
    var seekScrubClearTrigger by remember { mutableIntStateOf(0) }
    var showZoomLabel by remember { mutableStateOf(false) }
    var zoomLabelTrigger by remember { mutableIntStateOf(0) }

    // Whether the existing vertical swipe gesture is active — used to gate new gestures
    var isVerticalGestureActiveLocal by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = remember(context) {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) break
            ctx = ctx.baseContext
        }
        ctx as? Activity
    }
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    var showBrightness by remember { mutableStateOf(false) }
    var brightnessPercent by remember { mutableIntStateOf(0) }
    var brightnessHideTrigger by remember { mutableIntStateOf(0) }

    var showVolume by remember { mutableStateOf(false) }
    var volumePercent by remember { mutableIntStateOf(0) }
    var volumeHideTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(labelTrigger) {
        if (labelTrigger > 0) {
            delay(800)
            seekLabel = ""
        }
    }

    LaunchedEffect(seekScrubClearTrigger) {
        if (seekScrubClearTrigger > 0) {
            delay(800)
            seekScrubLabel = ""
        }
    }

    // Auto-hide zoom label after 1500ms
    LaunchedEffect(zoomLabelTrigger) {
        if (zoomLabelTrigger > 0) {
            delay(1500)
            showZoomLabel = false
        }
    }

    LaunchedEffect(volumeHideTrigger) {
        if (volumeHideTrigger > 0) {
            delay(800)
            showVolume = false
        }
    }

    LaunchedEffect(brightnessHideTrigger) {
        if (brightnessHideTrigger > 0) {
            delay(800)
            showBrightness = false
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val displayWidthPx = with(density) { maxWidth.toPx() }
        val displayHeightPx = with(density) { maxHeight.toPx() }

        // ── All gestures stacked in a single Box ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .tapGesture(
                    onToggleControls = onToggleControls,
                    onSeekForward = onSeekForward,
                    onSeekBackward = onSeekBackward,
                    onSpeedOverride = onSpeedOverride,
                    onSpeedRestore = onSpeedRestore,
                    onSeekLabel = {
                        seekLabel = it
                        labelTrigger++
                    },
                    onLongPressStart = { isLongPressing = true },
                    onLongPressEnd = { isLongPressing = false }
                )
                .horizontalSeekGesture(
                    isEnabled = true,
                    isVerticalGestureActive = isVerticalGestureActiveLocal,
                    currentPositionMs = currentPositionMs,
                    durationMs = durationMs,
                    onSeekStart = onPauseForSeek,
                    onSeekEnd = onResumeAfterSeek,
                    onSeekTo = onSeekTo,
                    onSeekLabel = { current, delta ->
                        seekScrubLabel = "$current  ($delta)"
                    },
                    onSeekLabelClear = { seekScrubClearTrigger++ },
                )
                .pinchToZoomGesture(
                    isEnabled = true,
                    isVerticalGestureActive = isVerticalGestureActiveLocal,
                    currentZoom = currentZoom,
                    onZoomChange = onZoomChange,
                    onZoomLabel = {
                        showZoomLabel = true
                        zoomLabelTrigger++
                    },
                    onZoomLabelClear = { showZoomLabel = false },
                )
                .panGesture(
                    isEnabled = true,
                    isVerticalGestureActive = isVerticalGestureActiveLocal,
                    currentZoom = currentZoom,
                    currentPanX = currentPanX,
                    currentPanY = currentPanY,
                    videoDisplayWidth = displayWidthPx,
                    videoDisplayHeight = displayHeightPx,
                    onPanChange = onPanChange,
                )
        ) {
            // The side-detection logic (firstDown.position.x > size.width / 2) stays in GestureOverlay.kt
            // — it decides which modifier to apply structurally by splitting the screen.
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .brightnessGesture(
                            activity = activity,
                            onBrightnessChange = {
                                showBrightness = true
                                brightnessPercent = it
                            },
                            onSwipeStart = { isVerticalGestureActiveLocal = true },
                            onBrightnessSwipeEnd = {
                                brightnessHideTrigger++
                                isVerticalGestureActiveLocal = false
                            }
                        )
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .volumeGesture(
                            audioManager = audioManager,
                            onVolumeChange = {
                                showVolume = true
                                volumePercent = it
                            },
                            onSwipeStart = { isVerticalGestureActiveLocal = true },
                            onVolumeSwipeEnd = {
                                volumeHideTrigger++
                                isVerticalGestureActiveLocal = false
                            }
                        )
                )
            }
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

            AnimatedVisibility(
                visible = showVolume,
                enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 },
                exit = fadeOut(tween(300)) + slideOutVertically(tween(300)) { it / 2 },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(horizontal = 48.dp)
            ) {
                IndicatorCard(isVolume = true, percent = volumePercent)
            }

            AnimatedVisibility(
                visible = showBrightness,
                enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 },
                exit = fadeOut(tween(300)) + slideOutVertically(tween(300)) { it / 2 },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(horizontal = 48.dp)
            ) {
                IndicatorCard(isVolume = false, percent = brightnessPercent)
            }

            // Seek scrub label
            if (seekScrubLabel.isNotEmpty()) {
                Text(
                    text = seekScrubLabel,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Zoom label
            if (showZoomLabel) {
                val zoomScale = 2f.pow(currentZoom)
                Text(
                    text = "Zoom: ${"%.1f".format(zoomScale)}×",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun IndicatorCard(isVolume: Boolean, percent: Int) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val animatedFraction by animateFloatAsState(
                    targetValue = percent / 100f,
                    animationSpec = tween(150),
                    label = "fraction"
                )
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(120.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.DarkGray.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(fraction = animatedFraction)
                            .background(Color.White)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val icon = if (isVolume) {
                        when {
                            percent == 0 -> Icons.Rounded.VolumeOff
                            percent < 50 -> Icons.Rounded.VolumeDown
                            else -> Icons.Rounded.VolumeUp
                        }
                    } else {
                        Icons.Rounded.Brightness6
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Text(
                        text = "$percent%",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
