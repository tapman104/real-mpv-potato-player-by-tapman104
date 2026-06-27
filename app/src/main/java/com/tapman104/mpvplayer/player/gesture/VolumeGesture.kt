package com.tapman104.mpvplayer.player.gesture

import android.media.AudioManager
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import kotlin.math.roundToInt

fun Modifier.volumeGesture(
    audioManager: AudioManager,
    onVolumeChange: (percent: Int) -> Unit,
    onSwipeStart: () -> Unit,
    onVolumeSwipeEnd: () -> Unit,
): Modifier = composed {
    val currentAudioManager by rememberUpdatedState(audioManager)
    val currentOnVolumeChange by rememberUpdatedState(onVolumeChange)
    val currentOnSwipeStart by rememberUpdatedState(onSwipeStart)
    val currentOnVolumeSwipeEnd by rememberUpdatedState(onVolumeSwipeEnd)

    pointerInput(Unit) {
        awaitEachGesture {
            val firstDown = awaitFirstDown(requireUnconsumed = false)
            if (firstDown.isConsumed) return@awaitEachGesture

            var isDragging = false
            var isConsumed = false
            try {
                withTimeout(500L) {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        val change = event.changes.firstOrNull { it.id == firstDown.id }
                        if (change == null || !change.pressed) {
                            break
                        }
                        if (change.isConsumed) {
                            val dy = change.position.y - firstDown.position.y
                            val dx = change.position.x - firstDown.position.x
                            if (kotlin.math.abs(dy) > kotlin.math.abs(dx)) {
                                isConsumed = true
                            }
                            break
                        }
                        val dy = change.position.y - firstDown.position.y
                        val dx = change.position.x - firstDown.position.x
                        if (kotlin.math.abs(dy) > 20f && kotlin.math.abs(dy) > kotlin.math.abs(dx)) {
                            isDragging = true
                            break
                        } else if (kotlin.math.abs(dx) > 20f) {
                            break
                        }
                    }
                }
            } catch (e: PointerEventTimeoutCancellationException) {
                // Long press, not dragging
            }

            if (isConsumed) return@awaitEachGesture

            if (isDragging) {
                firstDown.consume()

                var currentVolume = currentAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
                val maxVol = currentAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()

                currentOnVolumeChange(((currentVolume / maxVol) * 100).toInt())
                currentOnSwipeStart()

                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Main)
                    // Cancel if a second finger lands — let pinch take over
                    if (event.changes.count { it.pressed } > 1) {
                        currentOnVolumeSwipeEnd()
                        break
                    }
                    val change = event.changes.firstOrNull { it.id == firstDown.id }
                    if (change == null || !change.pressed) break
                    if (change.isConsumed) break

                    val dy = change.positionChange().y
                    val fraction = -dy / size.height

                    currentVolume += fraction * maxVol * 1.5f
                    currentVolume = currentVolume.coerceIn(0f, maxVol)
                    currentAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume.roundToInt(), 0)
                    currentOnVolumeChange(((currentVolume / maxVol) * 100).toInt())
                    
                    change.consume()
                }
                currentOnVolumeSwipeEnd()
            }
        }
    }
}
