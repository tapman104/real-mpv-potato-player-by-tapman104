package com.tapman104.mpvplayer.core.engine

import android.os.Handler
import android.os.Looper
import android.view.Surface
import android.view.SurfaceHolder
import android.util.Log
import `is`.xyz.mpv.MPVLib

class MpvSurface(private val executor: MpvCommandExecutor) : SurfaceHolder.Callback {
    private val TAG = "MpvSurface"
    private val mainHandler = Handler(Looper.getMainLooper())

    /** The surface that is currently presented to MPV. Written on the main thread only. */
    private var attachedSurface: Surface? = null

    /**
     * Set to the surface being queued for attachment before the executor task runs;
     * cleared inside the executor task once MPVLib.attachSurface() has returned.
     * Allows surfaceChanged to skip a redundant re-attach when the holder delivers
     * the same surface object we already have in flight.
     * Volatile so the executor thread's write is visible to the main thread immediately.
     */
    @Volatile private var pendingAttachSurface: Surface? = null

    var onSurfaceReady: (() -> Unit)? = null
    var onSurfaceDestroyed: (() -> Unit)? = null

    fun hasSurface(): Boolean = attachedSurface != null || pendingAttachSurface != null

    override fun surfaceCreated(holder: SurfaceHolder) {
        val surface = holder.surface ?: return
        if (!surface.isValid) return

        attachedSurface = surface
        pendingAttachSurface = surface
        // Increment the generation *before* queuing so any concurrent detachSurface
        // that runs after us on the executor thread will see the updated generation
        // and recognise itself as stale.
        val gen = executor.nextSurfaceGeneration()
        // Capture callback ref now (main thread) so late changes to onSurfaceReady
        // don't affect this particular surface-ready cycle.
        val callback = onSurfaceReady
        executor.execute {
            Log.d(TAG, "attachSurface gen=$gen")
            MPVLib.attachSurface(surface)
            pendingAttachSurface = null
            try { MPVLib.command("video-reload") } catch (_: Throwable) {}
            mainHandler.post { callback?.invoke() }
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d(TAG, "surfaceChanged: width=$width, height=$height")
        val surface = holder.surface ?: return
        if (!surface.isValid) return
        // Skip if we have already attached this surface or if it is already in the
        // executor queue waiting to be attached (pendingAttachSurface).
        if (surface == attachedSurface || surface == pendingAttachSurface) return

        attachedSurface = surface
        pendingAttachSurface = surface
        val gen = executor.nextSurfaceGeneration()
        executor.execute {
            Log.d(TAG, "attachSurface (changed) gen=$gen")
            MPVLib.attachSurface(surface)
            pendingAttachSurface = null
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceDestroyed")
        attachedSurface = null
        mainHandler.post { onSurfaceDestroyed?.invoke() }
        executor.detachSurface()
    }
}
