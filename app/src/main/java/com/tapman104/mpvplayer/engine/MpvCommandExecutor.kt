package com.tapman104.mpvplayer.engine

import `is`.xyz.mpv.MPVLib
import java.util.concurrent.Executors

class MpvCommandExecutor {
    private val executor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "mpv-engine-thread")
    }

    fun execute(action: () -> Unit) {
        if (!executor.isShutdown) {
            executor.submit(action)
        }
    }

    fun play() {
        execute {
            MPVLib.setPropertyBoolean(MpvConstants.PROP_PAUSE, false)
        }
    }

    fun pause() {
        execute {
            MPVLib.setPropertyBoolean(MpvConstants.PROP_PAUSE, true)
        }
    }

    fun togglePlay() {
        execute {
            val paused = MPVLib.getPropertyBoolean(MpvConstants.PROP_PAUSE) ?: false
            MPVLib.setPropertyBoolean(MpvConstants.PROP_PAUSE, !paused)
        }
    }

    fun seek(seconds: Double) {
        execute {
            MPVLib.command("seek", seconds.toString(), "absolute")
        }
    }

    fun seekRelative(seconds: Double) {
        execute {
            MPVLib.command("seek", seconds.toString(), "relative")
        }
    }

    fun loadFile(path: String) {
        execute {
            MPVLib.command("loadfile", path)
        }
    }

    fun setAudioTrack(id: Int) {
        execute {
            val value = if (id < 0) "no" else id.toString()
            MPVLib.setPropertyString(MpvConstants.PROP_AUDIO_ID, value)
        }
    }

    fun setSubtitleTrack(id: Int) {
        execute {
            val value = if (id < 0) "no" else id.toString()
            MPVLib.setPropertyString(MpvConstants.PROP_SUBTITLE_ID, value)
        }
    }

    fun setSpeed(speed: Double) {
        execute {
            MPVLib.setPropertyDouble(MpvConstants.PROP_SPEED, speed)
        }
    }

    fun setHwdec(mode: String) {
        execute {
            MPVLib.setPropertyString(MpvConstants.PROP_HWDEC, mode)
        }
    }

    fun setVolume(volume: Int) {
        execute {
            MPVLib.setPropertyDouble(MpvConstants.PROP_VOLUME, volume.toDouble())
        }
    }

    fun setSubtitleAppearance(size: Float, position: Float) {
        execute {
            MPVLib.setPropertyDouble("sub-scale", size.toDouble())
            MPVLib.setPropertyDouble("sub-pos", (100.0 - (position * 100.0)))
        }
    }

    fun shutdown() {
        executor.shutdown()
    }
}
