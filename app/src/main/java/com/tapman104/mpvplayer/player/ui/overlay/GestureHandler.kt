package com.tapman104.mpvplayer.player.ui.overlay

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

// ---------------------------------------------------------------------------
// Time formatter helper
// ---------------------------------------------------------------------------

private fun formatMs(ms: Long): String {
    val s = ms / 1000
    return "%d:%02d".format(s / 60, s % 60)
}

// ---------------------------------------------------------------------------
// 4a — Horizontal seek gesture
// ---------------------------------------------------------------------------

/**
 * Recognises a single-finger horizontal drag as a seek gesture.
 * Activation requires: |dx| > 30px AND |dx| > |dy|*2 AND held > 100ms.
 * Cancels on multi-finger contact.
 */
fun Modifier.horizontalSeekGesture(
    isEnabled: Boolean,
    isVerticalGestureActive: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    onSeekStart: () -> Unit,
    onSeekEnd: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSeekLabel: (current: String, delta: String) -> Unit,
    onSeekLabelClear: () -> Unit,
    sensitivityPx: Float = -1f,
): Modifier = composed {
    val currentPositionMsState = rememberUpdatedState(currentPositionMs)
    val durationMsState = rememberUpdatedState(durationMs)
    pointerInput(isEnabled, isVerticalGestureActive) {
        if (!isEnabled || isVerticalGestureActive) return@pointerInput
        awaitEachGesture {
            val firstDown = awaitFirstDown(requireUnconsumed = false)
            val downTime = System.currentTimeMillis()
            val startX = firstDown.position.x
            val startY = firstDown.position.y

            var seekActive = false
            var initialPositionMs = currentPositionMsState.value

            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Main)

                // Cancel on multi-touch
                if (event.changes.count { it.pressed } > 1) break

                val change = event.changes.firstOrNull { it.id == firstDown.id } ?: break
                if (!change.pressed) {
                    if (seekActive) {
                        change.consume()
                        onSeekEnd()
                        onSeekLabelClear() // timed hide is handled by LaunchedEffect in the composable
                    }
                    break
                }

                val deltaX = change.position.x - startX
                val deltaY = change.position.y - startY
                val elapsed = System.currentTimeMillis() - downTime

                if (!seekActive) {
                    // Activation condition
                    if (abs(deltaX) > 30f && abs(deltaX) > abs(deltaY) * 2f && elapsed > 100) {
                        seekActive = true
                        initialPositionMs = currentPositionMsState.value
                        onSeekStart()
                    }
                }

                if (seekActive) {
                    change.consume()
                    val effectiveSensitivity = if (sensitivityPx > 0f) sensitivityPx else size.width.toFloat()
                    val dur = durationMsState.value
                    val factor = if (dur > 0) dur.toFloat() / effectiveSensitivity else 1f
                    val targetMs = (initialPositionMs + (deltaX * factor).toLong())
                        .coerceIn(0L, dur)
                    onSeekTo(targetMs)
                    val deltaMs = targetMs - initialPositionMs
                    val sign = if (deltaMs >= 0) "+" else ""
                    onSeekLabel(formatMs(targetMs), "$sign${formatMs(abs(deltaMs))}")
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 4b — Pinch-to-zoom gesture
// ---------------------------------------------------------------------------

/**
 * Recognises a two-finger pinch as a zoom gesture.
 * Uses ln(dist/prevDist)*1.2 increments, clamped to [-1f, 3f].
 */
fun Modifier.pinchToZoomGesture(
    isEnabled: Boolean,
    isVerticalGestureActive: Boolean,
    currentZoom: Float,
    onZoomChange: (Float) -> Unit,
    onZoomLabel: () -> Unit,
    onZoomLabelClear: () -> Unit,
): Modifier = composed {
    val currentZoomState = rememberUpdatedState(currentZoom)
    pointerInput(isEnabled, isVerticalGestureActive) {
        if (!isEnabled || isVerticalGestureActive) return@pointerInput
        awaitEachGesture {
            // Wait for first finger
            awaitFirstDown(requireUnconsumed = false)

            var zoom = currentZoomState.value
            var prevDist = -1f
            var zoomActive = false

            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Main)
                val pressed = event.changes.filter { it.pressed }

                if (pressed.size < 2) {
                    if (zoomActive) onZoomLabelClear()
                    break
                }

                val p0 = pressed[0].position
                val p1 = pressed[1].position
                val dx = p1.x - p0.x
                val dy = p1.y - p0.y
                val dist = sqrt(dx * dx + dy * dy)

                if (prevDist < 0f) {
                    prevDist = dist
                    continue
                }

                if (abs(dist - prevDist) > 5f) {
                    if (!zoomActive) {
                        zoomActive = true
                        onZoomLabel()
                    }
                    val zoomDelta = ln(dist / prevDist) * 1.2f
                    zoom = (zoom + zoomDelta).coerceIn(-1f, 3f)
                    onZoomChange(zoom)
                    pressed.forEach { it.consume() }
                }
                prevDist = dist
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 4c — Pan gesture (single-finger, only when zoomed in)
// ---------------------------------------------------------------------------

/**
 * Recognises a single-finger drag as a pan gesture, but only when
 * [currentZoom] > 0 (i.e. the video is zoomed in). Clamps pan to the visible
 * bounds derived from the current scale.
 */
fun Modifier.panGesture(
    isEnabled: Boolean,
    isVerticalGestureActive: Boolean,
    currentZoom: Float,
    currentPanX: Float,
    currentPanY: Float,
    videoDisplayWidth: Float,
    videoDisplayHeight: Float,
    onPanChange: (Float, Float) -> Unit,
): Modifier = composed {
    val currentZoomState = rememberUpdatedState(currentZoom)
    val currentPanXState = rememberUpdatedState(currentPanX)
    val currentPanYState = rememberUpdatedState(currentPanY)
    pointerInput(isEnabled, isVerticalGestureActive) {
        if (!isEnabled || isVerticalGestureActive) return@pointerInput
        awaitEachGesture {
            val firstDown = awaitFirstDown(requireUnconsumed = false)
            if (currentZoomState.value <= 0f) return@awaitEachGesture
            val startX = firstDown.position.x
            val startY = firstDown.position.y

            val initialPanX = currentPanXState.value
            val initialPanY = currentPanYState.value
            val scale = 2f.pow(currentZoomState.value)
            val maxPan = ((scale - 1f) / (2f * scale)).coerceAtLeast(0f)
            val safeWidth = if (videoDisplayWidth > 0f) videoDisplayWidth else 1f
            val safeHeight = if (videoDisplayHeight > 0f) videoDisplayHeight else 1f

            var panActive = false

            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Main)

                // Bail if multi-touch (let pinch-to-zoom take over)
                if (event.changes.count { it.pressed } > 1) break

                val change = event.changes.firstOrNull { it.id == firstDown.id } ?: break
                if (!change.pressed) break

                val deltaX = change.position.x - startX
                val deltaY = change.position.y - startY

                if (!panActive) {
                    if (sqrt(deltaX * deltaX + deltaY * deltaY) > 20f) {
                        panActive = true
                    }
                }

                if (panActive) {
                    change.consume()
                    val targetX = (initialPanX + (deltaX / (safeWidth * scale))).coerceIn(-maxPan, maxPan)
                    val targetY = (initialPanY + (deltaY / (safeHeight * scale))).coerceIn(-maxPan, maxPan)

                    onPanChange(targetX, targetY)
                }
            }
        }
    }
}
