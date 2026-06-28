package com.tapman104.mpvplayer.player.gesture

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.sqrt

fun Modifier.tapGesture(
    onToggleControls: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekBackward: () -> Unit,
    onSpeedOverride: (Float) -> Unit,
    onSpeedRestore: () -> Unit,
    onSeekLabel: (String) -> Unit,
    onLongPressStart: () -> Unit,
    onLongPressEnd: () -> Unit,
): Modifier = composed {
    val currentOnToggleControls by rememberUpdatedState(onToggleControls)
    val currentOnSeekForward by rememberUpdatedState(onSeekForward)
    val currentOnSeekBackward by rememberUpdatedState(onSeekBackward)
    val currentOnSpeedOverride by rememberUpdatedState(onSpeedOverride)
    val currentOnSpeedRestore by rememberUpdatedState(onSpeedRestore)
    val currentOnSeekLabel by rememberUpdatedState(onSeekLabel)
    val currentOnLongPressStart by rememberUpdatedState(onLongPressStart)
    val currentOnLongPressEnd by rememberUpdatedState(onLongPressEnd)

    pointerInput(Unit) {
        awaitEachGesture {
            val firstDown = awaitFirstDown(requireUnconsumed = false)
            if (firstDown.isConsumed) return@awaitEachGesture
            firstDown.consume()

            var isDragging = false
            var isLongPressingLocal = false
            var isConsumed = false
            var horizontalExitDetected = false
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
                            // Only bail if a vertical drag or long-press consumed it.
                            // Horizontal consumption comes from Layer 2 seek scrub — we still
                            // want to fall through to the tap handler after the scrub ends.
                            val dy = change.position.y - firstDown.position.y
                            val dx = change.position.x - firstDown.position.x
                            if (kotlin.math.abs(dy) > kotlin.math.abs(dx)) {
                                isConsumed = true
                            }
                            break
                        }
                        val dy = change.position.y - firstDown.position.y
                        val dx = change.position.x - firstDown.position.x
                        val distance = sqrt(dx * dx + dy * dy)
                        if (distance > 10f && kotlin.math.abs(dy) > kotlin.math.abs(dx)) {
                            isDragging = true
                            break
                        } else if (kotlin.math.abs(dx) > 20f) {
                            horizontalExitDetected = true
                            firstUp = change
                            break
                        }
                    }
                }
            } catch (e: PointerEventTimeoutCancellationException) {
                isLongPressingLocal = true
            }

            if (isConsumed) return@awaitEachGesture

            if (isLongPressingLocal) {
                currentOnLongPressStart()
                currentOnSpeedOverride(2.0f)

                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Main)
                    val change = event.changes.firstOrNull { it.id == firstDown.id }
                    if (change == null || !change.pressed) {
                        change?.consume()
                        break
                    }
                    change.consume()
                }

                currentOnLongPressEnd()
                currentOnSpeedRestore()
            } else if (!isDragging) {
                if (horizontalExitDetected) return@awaitEachGesture

                firstUp?.consume()

                val secondDown = withTimeoutOrNull(300L) {
                    awaitFirstDown(requireUnconsumed = false)
                }

                if (secondDown == null) {
                    if (!horizontalExitDetected) {
                        currentOnToggleControls()
                    }
                } else {
                    if (secondDown.isConsumed) return@awaitEachGesture
                    secondDown.consume()

                    val secondUp = withTimeoutOrNull(500L) {
                        waitForUpOrCancellation()
                    }

                    if (secondUp != null) {
                        secondUp.consume()
                        if (secondUp.position.x < size.width / 2) {
                            currentOnSeekBackward()
                        } else {
                            currentOnSeekForward()
                        }
                    } else {
                        currentOnLongPressStart()
                        currentOnSpeedOverride(2.0f)

                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            val change = event.changes.firstOrNull { it.id == secondDown.id }
                            if (change == null || !change.pressed) {
                                change?.consume()
                                break
                            }
                            change.consume()
                        }

                        currentOnLongPressEnd()
                        currentOnSpeedRestore()
                    }
                }
            }
        }
    }
}
