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

    var showVolume by remember { mutableStateOf(false) }
    var volumePercent by remember { mutableIntStateOf(0) }

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

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val displayWidthPx = with(density) { maxWidth.toPx() }
        val displayHeightPx = with(density) { maxHeight.toPx() }

        // ── Layer 1: existing tap / double-tap / long-press / volume / brightness ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalSwipeGesture(
                    activity = activity,
                    audioManager = audioManager,
                    onVolumeChange = {
                        showVolume = true
                        volumePercent = it
                    },
                    onBrightnessChange = {
                        showBrightness = true
                        brightnessPercent = it
                    },
                    onSwipeStart = { isVerticalGestureActiveLocal = true },
                    onSwipeEnd = {
                        showVolume = false
                        showBrightness = false
                        isVerticalGestureActiveLocal = false
                    }
                )
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

            if (showVolume || showBrightness) {
                val align = if (showVolume) Alignment.CenterStart else Alignment.CenterEnd
                val text = if (showVolume) "🔊 $volumePercent%" else "☀️ $brightnessPercent%"

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 48.dp),
                    contentAlignment = align
                ) {
                    Text(
                        text = text,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.45f))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // ── Layer 2: horizontal seek + pinch zoom + pan ──
        Box(
            modifier = Modifier
                .fillMaxSize()
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
