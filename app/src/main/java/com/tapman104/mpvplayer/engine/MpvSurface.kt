package com.tapman104.mpvplayer.engine

import android.view.Surface
import android.view.SurfaceHolder
import android.util.Log
import `is`.xyz.mpv.MPVLib

class MpvSurface(private val executor: MpvCommandExecutor) : SurfaceHolder.Callback {
    private val TAG = "MpvSurface"
    private var attachedSurface: Surface? = null
    var onSurfaceReady: (() -> Unit)? = null

    fun hasSurface(): Boolean = attachedSurface != null

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceCreated: surface is valid = ${holder.surface?.isValid}")
        val surface = holder.surface
        if (surface != null && surface.isValid) {
            attachedSurface = surface
            executor.execute {
                Log.d(TAG, "Calling MPVLib.attachSurface")
                MPVLib.attachSurface(surface)
                onSurfaceReady?.invoke()
            }
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d(TAG, "surfaceChanged: width=$width, height=$height")
        val surface = holder.surface
        if (surface != null && surface.isValid && surface != attachedSurface) {
            attachedSurface = surface
            executor.execute {
                MPVLib.attachSurface(surface)
            }
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceDestroyed")
        attachedSurface = null
        executor.execute {
            Log.d(TAG, "Calling MPVLib.detachSurface")
            MPVLib.detachSurface()
        }
    }
    
    fun detachForce() {
        attachedSurface = null
        executor.execute {
            Log.d(TAG, "Forcing MPVLib.detachSurface")
            MPVLib.detachSurface()
        }
    }
}
