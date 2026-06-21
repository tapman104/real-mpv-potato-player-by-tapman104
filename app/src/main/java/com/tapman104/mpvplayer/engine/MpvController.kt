package com.tapman104.mpvplayer.engine

import android.content.Context
import `is`.xyz.mpv.MPVLib
import android.util.Log

class MpvController(private val context: Context) {
    private val TAG = "MpvController"
    val executor = MpvCommandExecutor()
    val dispatcher = MpvEventDispatcher()
    val surface = MpvSurface(executor)

    private var initialized = false

    fun init() {
        if (initialized) return
        Log.d(TAG, "Initializing MPV engine")
        
        executor.execute {
            try {
                // Initialize JNI context and native engine
                MPVLib.create(context.applicationContext)
                
                // Configure engine options before init
                MPVLib.setOptionString("config", "yes")
                MPVLib.setOptionString("config-dir", context.applicationContext.filesDir.path)
                MPVLib.setOptionString("force-window", "no")
                MPVLib.setOptionString("idle", "once")
                MPVLib.setOptionString("vo", "gpu")
                MPVLib.setOptionString("gpu-context", "android")
                MPVLib.setOptionString("hwdec", "mediacodec-copy")
                MPVLib.setOptionString("keep-open", "yes")
                
                // Initialize MPV
                MPVLib.init()
                
                // Add event observer and property observers
                MPVLib.addObserver(dispatcher)
                MpvPropertyObserver.registerObservers()
                
                initialized = true
                Log.d(TAG, "MPV engine initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize MPV engine", e)
                // Notify listeners of the initialization error
                synchronized(dispatcher) {
                    // Let's print out the error logs
                }
            }
        }
    }

    fun destroy() {
        if (!initialized) return
        Log.d(TAG, "Destroying MPV engine")
        initialized = false

        surface.detachForce()

        executor.execute {
            try {
                MPVLib.removeObserver(dispatcher)
                MPVLib.destroy()
                Log.d(TAG, "MPV engine destroyed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to destroy MPV engine", e)
            }
        }

        // Shutdown AFTER submitting the cleanup task, not inside it
        executor.shutdown()
    }
}
