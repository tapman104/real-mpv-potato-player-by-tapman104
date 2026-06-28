package com.tapman104.mpvplayer.player.gesture

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs

private fun formatMs(ms: Long): String {
    val s = ms / 1000
    return "%d:%02d".format(s / 60, s % 60)
}

/**
 * Recognises a single-finger horizontal drag as a seek gesture.
 * Activation requires: |dx| > 30px AND |dx| > |dy|*2 AND held > 100ms AND not long-pressing.
 * Cancels on multi-finger contact.
 */
fun Modifier.horizontalSeekGesture(
    isEnabled: Boolean,
    isVerticalGestureActive: Boolean,
    isLongPressing: Boolean = false,
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
    pointerInput(isEnabled, isVerticalGestureActive, isLongPressing) {
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
                    // Activation condition — also blocked while a long-press is active
                    if (abs(deltaX) > 30f && abs(deltaX) > abs(deltaY) * 2f && elapsed > 100 && !isLongPressing) {
                        seekActive = true
                        initialPositionMs = currentPositionMsState.value
                        onSeekStart()
                    }
                }

                if (seekActive) {
                    change.consume()
                    val msPerPx = if (sensitivityPx > 0f) sensitivityPx else 300f
                    val dur = durationMsState.value
                    val targetMs = (initialPositionMs + (deltaX * msPerPx).toLong())
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
