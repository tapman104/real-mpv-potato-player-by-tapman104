package com.tapman104.mpvplayer.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.tapman104.mpvplayer.engine.MpvController
import com.tapman104.mpvplayer.engine.MpvConstants
import com.tapman104.mpvplayer.engine.MpvEventListener
import com.tapman104.mpvplayer.engine.TrackListParser
import com.tapman104.mpvplayer.state.*
import `is`.xyz.mpv.MPVLib
import `is`.xyz.mpv.MPVNode

class PlayerViewModel(private val context: Context) : ViewModel(), MpvEventListener {

    val controller = MpvController(context)

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _subtitleAppearance = MutableStateFlow(SubtitleAppearanceState())
    val subtitleAppearance: StateFlow<SubtitleAppearanceState> = _subtitleAppearance.asStateFlow()

    private val _playlistState = MutableStateFlow(PlaylistState())
    val playlistState: StateFlow<PlaylistState> = _playlistState.asStateFlow()

    init {
        controller.dispatcher.addListener(this)
        controller.init()
    }

    // ---------------------------------------------------------------------------
    // Playback controls
    // ---------------------------------------------------------------------------

    fun play() = controller.executor.play()
    fun pause() = controller.executor.pause()
    fun togglePlay() = controller.executor.togglePlay()
    fun seekTo(positionMs: Long) = controller.executor.seek(positionMs / 1000.0)
    fun seekRelative(offsetMs: Long) = controller.executor.seekRelative(offsetMs / 1000.0)
    fun setSpeed(speed: Float) = controller.executor.setSpeed(speed.toDouble())
    fun setVolume(volume: Int) = controller.executor.setVolume(volume)
    fun setAudioTrack(id: Int) = controller.executor.setAudioTrack(id)
    fun setSubtitleTrack(id: Int) = controller.executor.setSubtitleTrack(id)

    // ---------------------------------------------------------------------------
    // Load / URI
    // ---------------------------------------------------------------------------

    private fun resolveUri(uri: Uri): String {
        return when (uri.scheme) {
            "content" -> {
                val fd = context.contentResolver
                    .openFileDescriptor(uri, "r") ?: return uri.toString()
                "fd://${fd.detachFd()}"
            }
            "file" -> uri.path ?: uri.toString()
            else -> uri.toString()
        }
    }

    fun loadAndPlay(uri: Uri) {
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (e: SecurityException) {
            // Permission may already be held or not grantable; proceed anyway
        }
        // Update playlist state so currentUri reflects the new file immediately.
        // If the URI is already in the list, jump to it; otherwise append and jump.
        _playlistState.update { current ->
            val uriStr = uri.toString()
            val existingIndex = current.items.indexOf(uriStr)
            if (existingIndex >= 0) {
                current.copy(currentIndex = existingIndex)
            } else {
                current.copy(
                    items = current.items + uriStr,
                    currentIndex = current.items.size
                )
            }
        }
        _playerState.update { it.copy(isLoading = true, error = null) }
        controller.executor.loadFile(resolveUri(uri))
    }

    // ---------------------------------------------------------------------------
    // Playlist
    // ---------------------------------------------------------------------------

    fun setPlaylist(uris: List<Uri>) {
        val stringUris = uris.map { it.toString() }
        _playlistState.update { PlaylistState(items = stringUris, currentIndex = 0) }
    }

    fun addToPlaylist(uri: Uri) {
        _playlistState.update { current ->
            current.copy(items = current.items + uri.toString())
        }
    }

    fun playNext() {
        val playlist = _playlistState.value
        if (playlist.hasNext) {
            val nextIndex = playlist.currentIndex + 1
            _playlistState.update { it.copy(currentIndex = nextIndex) }
            val nextUri = Uri.parse(playlist.items[nextIndex])
            loadAndPlay(nextUri)
        }
    }

    fun playPrevious() {
        val playlist = _playlistState.value
        if (playlist.hasPrevious) {
            val prevIndex = playlist.currentIndex - 1
            _playlistState.update { it.copy(currentIndex = prevIndex) }
            val prevUri = Uri.parse(playlist.items[prevIndex])
            loadAndPlay(prevUri)
        }
    }

    fun playAt(index: Int) {
        val playlist = _playlistState.value
        if (index in playlist.items.indices) {
            _playlistState.update { it.copy(currentIndex = index) }
            val uri = Uri.parse(playlist.items[index])
            loadAndPlay(uri)
        }
    }

    // ---------------------------------------------------------------------------
    // Decode mode
    // ---------------------------------------------------------------------------

    fun setDecodeMode(mode: DecodeMode) {
        controller.executor.setHwdec(mode.mpvValue)
        _playerState.update { it.copy(decodeMode = mode) }
    }

    // ---------------------------------------------------------------------------
    // Subtitle appearance
    // ---------------------------------------------------------------------------

    fun setSubtitleFontSize(size: Int) {
        _subtitleAppearance.update { it.copy(fontSize = size) }
        controller.executor.execute {
            MPVLib.setPropertyInt("sub-font-size", size)
        }
    }

    fun setSubtitleFontColor(color: String) {
        _subtitleAppearance.update { it.copy(fontColor = color) }
        controller.executor.execute {
            MPVLib.setPropertyString("sub-color", color)
        }
    }

    fun setSubtitleBold(bold: Boolean) {
        _subtitleAppearance.update { it.copy(bold = bold) }
        controller.executor.execute {
            MPVLib.setPropertyBoolean("sub-bold", bold)
        }
    }

    fun setSubtitleBorderStyle(style: String) {
        _subtitleAppearance.update { it.copy(borderStyle = style) }
        controller.executor.execute {
            MPVLib.setPropertyString("sub-border-style", style)
        }
    }

    fun setSubtitleBorderSize(size: Float) {
        _subtitleAppearance.update { it.copy(borderSize = size) }
        controller.executor.execute {
            MPVLib.setPropertyDouble("sub-border-size", size.toDouble())
        }
    }

    fun setSubtitleShadow(shadow: Float) {
        _subtitleAppearance.update { it.copy(shadow = shadow) }
        controller.executor.execute {
            MPVLib.setPropertyDouble("sub-shadow-offset", shadow.toDouble())
        }
    }

    fun setSubtitleBackgroundAlpha(alpha: Float) {
        _subtitleAppearance.update { it.copy(backgroundAlpha = alpha) }
        // sub-back-color expects an ASS ARGB color string "#AARRggBB".
        // We keep RGB as black (000000) and only vary the alpha channel.
        val alphaInt = (alpha * 255).toInt().coerceIn(0, 255)
        val color = String.format("#%02X000000", alphaInt)
        controller.executor.execute {
            MPVLib.setPropertyString("sub-back-color", color)
        }
    }

    // ---------------------------------------------------------------------------
    // MpvEventListener
    // ---------------------------------------------------------------------------

    override fun onFileLoaded() {
        _playerState.update { it.copy(isLoading = false, error = null) }
    }

    override fun onPlaybackStarted() {
        _playerState.update { it.copy(isPlaying = true, isLoading = false) }
    }

    override fun onPlaybackStopped(endReason: Int) {
        _playerState.update { it.copy(isPlaying = false) }
        // Auto-advance: endReason 0 = EOF (natural end). Advance playlist only on natural end.
        if (endReason == 0) {
            playNext()
        }
    }

    override fun onPropertyChange(name: String, value: Any?) {
        when (name) {
            MpvConstants.PROP_PAUSE -> {
                val paused = value as? Boolean ?: return
                _playerState.update { it.copy(isPlaying = !paused) }
            }
            MpvConstants.PROP_TIME_POS -> {
                val seconds = value as? Double ?: return
                _playerState.update { it.copy(currentPositionMs = (seconds * 1000).toLong()) }
            }
            MpvConstants.PROP_DURATION -> {
                val seconds = value as? Double ?: return
                _playerState.update { it.copy(durationMs = (seconds * 1000).toLong()) }
            }
            MpvConstants.PROP_DEMUXER_CACHE_TIME -> {
                val seconds = value as? Double ?: return
                _playerState.update { it.copy(demuxerCacheTimeMs = (seconds * 1000).toLong()) }
            }
            MpvConstants.PROP_TRACK_LIST -> {
                val node = value as? MPVNode ?: return
                val audioTracks = TrackListParser.parseAudioTracks(node)
                val subtitleTracks = TrackListParser.parseSubtitleTracks(node)
                _playerState.update { it.copy(audioTracks = audioTracks, subtitleTracks = subtitleTracks) }
            }
            MpvConstants.PROP_AUDIO_ID -> {
                val id = (value as? String)?.toIntOrNull() ?: -1
                _playerState.update { it.copy(selectedAudioTrackId = id) }
            }
            MpvConstants.PROP_SUBTITLE_ID -> {
                val id = (value as? String)?.toIntOrNull() ?: -1
                _playerState.update { it.copy(selectedSubtitleTrackId = id) }
            }
            MpvConstants.PROP_SPEED -> {
                val speed = value as? Double ?: return
                _playerState.update { it.copy(speed = speed.toFloat()) }
            }
            MpvConstants.PROP_VOLUME -> {
                val volume = value as? Double ?: return
                _playerState.update { it.copy(volume = volume.toInt()) }
            }
        }
    }

    override fun onError(message: String) {
        _playerState.update { it.copy(error = message, isLoading = false) }
    }

    // ---------------------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------------------

    override fun onCleared() {
        controller.dispatcher.removeListener(this)
        controller.destroy()
        super.onCleared()
    }
}
