package com.tapman104.mpvplayer.player.gesture

import android.app.Activity
import android.media.AudioManager
import android.provider.Settings
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
import androidx.compose.ui.platform.LocalContext
import kotlin.math.roundToInt

fun Modifier.verticalSwipeGesture(
    activity: Activity?,
    audioManager: AudioManager,
    onVolumeChange: (percent: Int) -> Unit,
    onBrightnessChange: (percent: Int) -> Unit,
    onSwipeStart: () -> Unit,
    onSwipeEnd: () -> Unit,
): Modifier = composed {
    val context = LocalContext.current
    val currentActivity by rememberUpdatedState(activity)
    val currentAudioManager by rememberUpdatedState(audioManager)
    val currentOnVolumeChange by rememberUpdatedState(onVolumeChange)
    val currentOnBrightnessChange by rememberUpdatedState(onBrightnessChange)
    val currentOnSwipeStart by rememberUpdatedState(onSwipeStart)
    val currentOnSwipeEnd by rememberUpdatedState(onSwipeEnd)

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
                val isRightSide = firstDown.position.x > size.width / 2
                var currentVolume = currentAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
                val maxVol = currentAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()

                var currentBrightness = currentActivity?.window?.attributes?.screenBrightness ?: -1f
                if (currentBrightness < 0f) {
                    currentBrightness = try {
                        Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS) / 255f
                    } catch (e: Exception) {
                        0.5f
                    }
                }

                if (isRightSide) {
                    currentOnVolumeChange(((currentVolume / maxVol) * 100).toInt())
                } else {
                    currentOnBrightnessChange((currentBrightness * 100).toInt())
                }
                currentOnSwipeStart()

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
                        currentAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume.roundToInt(), 0)
                        currentOnVolumeChange(((currentVolume / maxVol) * 100).toInt())
                    } else {
                        currentBrightness += fraction * 1.5f
                        currentBrightness = currentBrightness.coerceIn(0.01f, 1f)
                        currentActivity?.window?.attributes = currentActivity?.window?.attributes?.apply {
                            screenBrightness = currentBrightness
                        }
                        currentOnBrightnessChange((currentBrightness * 100).toInt())
                    }
                    change.consume()
                }
                currentOnSwipeEnd()
            }
        }
    }
}
