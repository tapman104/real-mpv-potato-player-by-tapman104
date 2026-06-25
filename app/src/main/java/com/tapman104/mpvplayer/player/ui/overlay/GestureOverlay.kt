package com.tapman104.mpvplayer.player.ui.overlay

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.media.AudioManager
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
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
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.pow
import kotlin.math.roundToInt

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
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val firstDown = awaitFirstDown(requireUnconsumed = false)
                        if (firstDown.isConsumed) return@awaitEachGesture
                        firstDown.consume()

                        var isDragging = false
                        var isLongPressingLocal = false
                        var isConsumed = false
                        var firstUp: androidx.compose.ui.input.pointer.PointerInputChange? = null

                        try {
                            withTimeout(500L) {
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Main)
                                    val change = event.changes.firstOrNull { it.id == firstDown.id }
                                    if (change == null || !change.pressed) {
                                        firstUp = change
                                        break
                                    }
                                    if (change.isConsumed) {
                                        isConsumed = true
                                        break
                                    }
                                    val dy = change.position.y - firstDown.position.y
                                    val dx = change.position.x - firstDown.position.x
                                    if (kotlin.math.abs(dy) > 20f && kotlin.math.abs(dy) > kotlin.math.abs(dx)) {
                                        isDragging = true
                                        break
                                    } else if (kotlin.math.abs(dx) > 20f) {
                                        firstUp = change
                                        break
                                    }
                                }
                            }
                        } catch (e: PointerEventTimeoutCancellationException) {
                            isLongPressingLocal = true
                        }

                        if (isConsumed) return@awaitEachGesture

                        if (isDragging) {
                            val isRightSide = firstDown.position.x > size.width / 2
                            var currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
                            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()

                            var currentBrightness = activity?.window?.attributes?.screenBrightness ?: -1f
                            if (currentBrightness < 0f) {
                                currentBrightness = try {
                                    Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS) / 255f
                                } catch (e: Exception) {
                                    0.5f
                                }
                            }

                            if (isRightSide) {
                                showVolume = true
                                volumePercent = ((currentVolume / maxVol) * 100).toInt()
                            } else {
                                showBrightness = true
                                brightnessPercent = (currentBrightness * 100).toInt()
                            }
                            isVerticalGestureActiveLocal = true

                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Main)
                                val change = event.changes.firstOrNull { it.id == firstDown.id }
                                if (change == null || !change.pressed) break
                                if (change.isConsumed) break

                                val dy = change.positionChange().y
                                val fraction = -dy / size.height

                                if (isRightSide) {
                                    currentVolume += fraction * maxVol * 1.5f
                                    currentVolume = currentVolume.coerceIn(0f, maxVol)
                                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume.roundToInt(), 0)
                                    volumePercent = ((currentVolume / maxVol) * 100).toInt()
                                } else {
                                    currentBrightness += fraction * 1.5f
                                    currentBrightness = currentBrightness.coerceIn(0.01f, 1f)
                                    activity?.window?.attributes = activity?.window?.attributes?.apply {
                                        screenBrightness = currentBrightness
                                    }
                                    brightnessPercent = (currentBrightness * 100).toInt()
                                }
                                change.consume()
                            }
                            showVolume = false
                            showBrightness = false
                            isVerticalGestureActiveLocal = false
                        } else if (isLongPressingLocal) {
                            isLongPressing = true
                            onSpeedOverride(2.0f)

                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Main)
                                val change = event.changes.firstOrNull { it.id == firstDown.id }
                                if (change == null || !change.pressed) {
                                    change?.consume()
                                    break
                                }
                                change.consume()
                            }

                            isLongPressing = false
                            onSpeedRestore()
                        } else {
                            firstUp?.consume()

                            val secondDown = withTimeoutOrNull(300L) {
                                awaitFirstDown(requireUnconsumed = false)
                            }

                            if (secondDown == null) {
                                onToggleControls()
                            } else {
                                if (secondDown.isConsumed) return@awaitEachGesture
                                secondDown.consume()

                                val secondUp = withTimeoutOrNull(500L) {
                                    waitForUpOrCancellation()
                                }

                                if (secondUp != null) {
                                    secondUp.consume()
                                    if (secondUp.position.x < size.width / 2) {
                                        onSeekBackward()
                                        seekLabel = "-10s"
                                    } else {
                                        onSeekForward()
                                        seekLabel = "+10s"
                                    }
                                    labelTrigger++
                                } else {
                                    isLongPressing = true
                                    onSpeedOverride(2.0f)

                                    while (true) {
                                        val event = awaitPointerEvent(PointerEventPass.Main)
                                        val change = event.changes.firstOrNull { it.id == secondDown.id }
                                        if (change == null || !change.pressed) {
                                            change?.consume()
                                            break
                                        }
                                        change.consume()
                                    }

                                    isLongPressing = false
                                    onSpeedRestore()
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
                    onSeekLabelClear = { seekScrubLabel = "" },
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
