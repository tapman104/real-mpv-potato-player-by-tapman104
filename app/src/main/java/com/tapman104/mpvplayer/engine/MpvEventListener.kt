package com.tapman104.mpvplayer.engine

interface MpvEventListener {
    fun onFileLoaded()
    fun onPlaybackStarted()
    fun onPlaybackStopped(endReason: Int)
    fun onPropertyChange(name: String, value: Any?)
    fun onError(message: String)
}
