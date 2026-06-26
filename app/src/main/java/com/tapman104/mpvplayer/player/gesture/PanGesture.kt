package com.tapman104.mpvplayer.player.gesture

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.pow
import kotlin.math.sqrt

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
