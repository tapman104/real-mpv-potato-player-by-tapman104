package com.tapman104.mpvplayer.player.gesture

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.sqrt

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
