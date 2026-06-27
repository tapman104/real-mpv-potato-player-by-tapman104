package com.tapman104.mpvplayer.player.gesture

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.media.AudioManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Speed
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.pow

// ── Seek direction communicated to the overlay ──────────────────────────────
enum class SeekDirection { Forward, Backward, None }

@Composable
fun GestureOverlay(
    onSeekForward: () -> Unit,
    onSeekBackward: () -> Unit,
    onSpeedOverride: (Float) -> Unit,
    onSpeedRestore: () -> Unit,
    onToggleControls: () -> Unit,
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
    // ── Double-tap seek state ────────────────────────────────────────────────
    var seekLabel by remember { mutableStateOf("") }
    var seekDirection by remember { mutableStateOf(SeekDirection.None) }
    var labelTrigger by remember { mutableIntStateOf(0) }
    var showSeekIndicator by remember { mutableStateOf(false) }

    // ── Long-press 2× speed state ────────────────────────────────────────────
    var isLongPressing by remember { mutableStateOf(false) }

    // ── Scrub-seek label (kept in local sub-composable to avoid full recompose)
    var seekScrubLabel by remember { mutableStateOf("") }
    var seekScrubClearTrigger by remember { mutableIntStateOf(0) }

    // ── Zoom label state ─────────────────────────────────────────────────────
    var showZoomLabel by remember { mutableStateOf(false) }
    var zoomLabelTrigger by remember { mutableIntStateOf(0) }

    // ── Vertical swipe gate ──────────────────────────────────────────────────
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

    // Auto-hide seek indicator after 700 ms
    LaunchedEffect(labelTrigger) {
        if (labelTrigger > 0) {
            showSeekIndicator = true
            delay(700)
            showSeekIndicator = false
            delay(300) // wait for fade-out before clearing text
            seekLabel = ""
            seekDirection = SeekDirection.None
        }
    }

    LaunchedEffect(seekScrubClearTrigger) {
        if (seekScrubClearTrigger > 0) {
            delay(800)
            seekScrubLabel = ""
        }
    }

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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .tapGesture(
                    onToggleControls = onToggleControls,
                    onSeekForward = onSeekForward,
                    onSeekBackward = onSeekBackward,
                    onSpeedOverride = onSpeedOverride,
                    onSpeedRestore = onSpeedRestore,
                    onSeekLabel = { label ->
                        seekLabel = label
                        seekDirection = if (label.startsWith("+")) SeekDirection.Forward else SeekDirection.Backward
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

            // ── Double-tap seek indicator (left / right) ─────────────────────
            AnimatedVisibility(
                visible = showSeekIndicator && seekDirection == SeekDirection.Backward,
                enter = fadeIn(tween(120, easing = FastOutSlowInEasing)) +
                        scaleIn(tween(150, easing = FastOutSlowInEasing), initialScale = 0.75f),
                exit = fadeOut(tween(250)),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 32.dp)
            ) {
                SeekIndicator(label = seekLabel, isForward = false)
            }

            AnimatedVisibility(
                visible = showSeekIndicator && seekDirection == SeekDirection.Forward,
                enter = fadeIn(tween(120, easing = FastOutSlowInEasing)) +
                        scaleIn(tween(150, easing = FastOutSlowInEasing), initialScale = 0.75f),
                exit = fadeOut(tween(250)),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 32.dp)
            ) {
                SeekIndicator(label = seekLabel, isForward = true)
            }

            // ── Long-press 2× speed indicator ───────────────────────────────
            AnimatedVisibility(
                visible = isLongPressing,
                enter = fadeIn(tween(150)) +
                        scaleIn(tween(200, easing = FastOutSlowInEasing), initialScale = 0.7f),
                exit = fadeOut(tween(200)) +
                        scaleOut(tween(200), targetScale = 0.85f),
                modifier = Modifier.align(Alignment.Center)
            ) {
                SpeedIndicator()
            }

            // ── Volume indicator ─────────────────────────────────────────────
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

            // ── Brightness indicator ─────────────────────────────────────────
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

            // ── Scrub-seek label (only text; composable is scoped narrowly) ──
            SeekScrubLabel(
                label = seekScrubLabel,
                modifier = Modifier.align(Alignment.Center)
            )

            // ── Zoom label ───────────────────────────────────────────────────
            if (showZoomLabel) {
                val zoomScale = 2f.pow(currentZoom)
                Text(
                    text = "Zoom: ${"%.1f".format(zoomScale)}×",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                )
            }
        }
    }
}

// ── Seek indicator (double-tap) ──────────────────────────────────────────────
@Composable
private fun SeekIndicator(label: String, isForward: Boolean) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(96.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.22f),
                        Color.White.copy(alpha = 0.06f),
                    )
                )
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isForward) Icons.Rounded.FastForward else Icons.Rounded.FastRewind,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// ── Long-press 2× speed indicator ───────────────────────────────────────────
@Composable
private fun SpeedIndicator() {
    // Pulsing scale animation while held
    val infiniteTransition = rememberInfiniteTransition(label = "speed_pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .scale(pulse)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFF6B35).copy(alpha = 0.85f),
                        Color(0xFFFF3D71).copy(alpha = 0.85f),
                    )
                )
            )
            .padding(horizontal = 24.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Speed,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
            Text(
                text = "2×  Speed",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// ── Scrub-seek label — isolated composable to minimise recompose scope ───────
@Composable
private fun SeekScrubLabel(label: String, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = label,
        transitionSpec = {
            fadeIn(tween(80)) togetherWith fadeOut(tween(80))
        },
        modifier = modifier,
        label = "scrub_label"
    ) { text ->
        if (text.isNotEmpty()) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.60f))
                    .padding(horizontal = 18.dp, vertical = 9.dp)
            )
        } else {
            // Empty box keeps the composable in the tree so transitions are smooth
            Box(modifier = Modifier)
        }
    }
}

// ── Volume / Brightness indicator card ──────────────────────────────────────
@Composable
private fun IndicatorCard(isVolume: Boolean, percent: Int) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.60f))
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
