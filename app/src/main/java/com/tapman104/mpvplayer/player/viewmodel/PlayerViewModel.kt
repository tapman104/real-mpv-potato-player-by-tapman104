package com.tapman104.mpvplayer.player.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.tapman104.mpvplayer.core.preferences.SubtitlePreferences
import com.tapman104.mpvplayer.core.database.ResumePositionDao
import com.tapman104.mpvplayer.core.database.ResumePositionEntity
import com.tapman104.mpvplayer.core.preferences.UserPreferencesRepository
import com.tapman104.mpvplayer.core.engine.MpvController
import com.tapman104.mpvplayer.core.engine.MpvConstants
import com.tapman104.mpvplayer.core.engine.MpvEventListener
import com.tapman104.mpvplayer.core.engine.TrackListParser
import com.tapman104.mpvplayer.player.model.*
import com.tapman104.mpvplayer.player.state.*
import `is`.xyz.mpv.MPVLib
import `is`.xyz.mpv.MPVNode

class PlayerViewModel(
    private val context: Context,
    private val resumePositionDao: ResumePositionDao,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel(), MpvEventListener {
    private val TAG = "PlayerViewModel"

    private var pendingFileUri: Uri? = null


    val controller = MpvController(context)

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _subtitleAppearance = MutableStateFlow(SubtitleAppearanceState())
    val subtitleAppearance: StateFlow<SubtitleAppearanceState> = _subtitleAppearance.asStateFlow()

    private val _playlistState = MutableStateFlow(PlaylistState())
    val playlistState: StateFlow<PlaylistState> = _playlistState.asStateFlow()

    // ---------------------------------------------------------------------------
    // Preferred subtitle language (from user preferences)
    // ---------------------------------------------------------------------------

    private val _preferredSubtitleLang = MutableStateFlow(UserPreferencesRepository.DEFAULT_SUBTITLE_LANGUAGE)
    val preferredSubtitleLang: StateFlow<String> = _preferredSubtitleLang.asStateFlow()

    init {
        controller.dispatcher.addListener(this)
        controller.init()
        controller.surface.onSurfaceReady = {
            onSurfaceReady()
        }

        // Collect preferred subtitle language from DataStore.
        viewModelScope.launch {
            userPreferencesRepository.subtitleLanguage.collect {
                _preferredSubtitleLang.value = it
            }
        }

        // Load persisted subtitle appearance preferences and apply them immediately.
        viewModelScope.launch {
            combine(
                SubtitlePreferences.sizeFlow(context),
                SubtitlePreferences.positionFlow(context)
            ) { size, position -> Pair(size, position) }
                .collect { (size, position) ->
                    _playerState.update { it.copy(subtitleSize = size, subtitlePosition = position) }
                    controller.executor.setSubtitleAppearance(size, position)
                }
        }
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
        if (uri.scheme != "content") {
            return uri.path ?: uri.toString()
        }
        return try {
            val pfd = context.contentResolver.openFileDescriptor(uri, "r")
                ?: return uri.toString()
            val fd = pfd.detachFd()
            "fd://$fd"
        } catch (e: Exception) {
            uri.toString()
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

        if (controller.surface.hasSurface()) {
            controller.executor.loadFile(resolveUri(uri))
        } else {
            pendingFileUri = uri
        }
    }

    fun onSurfaceReady() {
        val pending = pendingFileUri
        if (pending != null) {
            // First load — surface became ready before loadAndPlay was called
            pendingFileUri = null
            controller.executor.loadFile(resolveUri(pending))
        }
        // If pendingFileUri is null, a file is already loaded in mpv.
        // attachSurface() already ran — mpv resumes rendering automatically.
        // Do NOT reload the file here. No seek needed.
    }

    /** Pauses playback immediately — used by screen-off receiver. */
    fun pausePlayback() {
        controller.executor.pause()
        _playerState.update { it.copy(isPlaying = false) }
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

    fun setSubtitleAppearance(size: Float, position: Float) {
        _playerState.update { it.copy(subtitleSize = size, subtitlePosition = position) }
        controller.executor.setSubtitleAppearance(size, position)
        viewModelScope.launch {
            SubtitlePreferences.save(context, size, position)
        }
    }

    fun resetSubtitleAppearance() {
        val size = SubtitlePreferences.DEFAULT_SIZE
        val position = SubtitlePreferences.DEFAULT_POSITION
        _playerState.update { it.copy(subtitleSize = size, subtitlePosition = position) }
        controller.executor.setSubtitleAppearance(size, position)
        viewModelScope.launch {
            SubtitlePreferences.reset(context)
        }
    }

    // ---------------------------------------------------------------------------
    // Resume position
    // ---------------------------------------------------------------------------

    /**
     * Saves the current playback position for [filePath].
     * Positions under 5 seconds are ignored to avoid saving very early scrubs.
     */
    fun saveCurrentPosition(filePath: String, positionMs: Long) {
        viewModelScope.launch {
            val duration = _playerState.value.durationMs
            if (positionMs > 5000L && (duration == 0L || positionMs < duration - 5000L)) {
                resumePositionDao.savePosition(
                    ResumePositionEntity(filePath = filePath, positionMs = positionMs)
                )
            } else if (duration > 0L && positionMs >= duration - 5000L) {
                resumePositionDao.deletePosition(filePath)
            }
        }
    }

    /**
     * Loads the saved resume position for [filePath] and delivers it via [onResult].
     * Returns null if no position is stored.
     */
    fun loadResumePosition(filePath: String, onResult: (Long?) -> Unit) {
        viewModelScope.launch {
            val entity = resumePositionDao.getPosition(filePath)
            onResult(entity?.positionMs)
        }
    }

    /** Deletes the saved resume position for [filePath] (e.g. user chose "Start Over"). */
    fun clearResumePosition(filePath: String) {
        viewModelScope.launch {
            resumePositionDao.deletePosition(filePath)
        }
    }

    // ---------------------------------------------------------------------------
    // Auto-subtitle selection
    // ---------------------------------------------------------------------------

    /**
     * Selects the first subtitle track whose language starts with [preferredSubtitleLang].
     * Call this once after tracks are loaded (keyed LaunchedEffect in UI).
     */
    fun autoSelectSubtitle(tracks: List<SubtitleTrack>) {
        val lang = _preferredSubtitleLang.value
        val match = tracks.firstOrNull { it.lang.lowercase().startsWith(lang.lowercase()) }
        if (match != null) {
            setSubtitleTrack(match.id)
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
        // endReason 0 = natural EOF. Advance playlist only on clean end
        // AND only when the surface is still alive (not a VO teardown on lock).
        if (endReason == 0 && controller.surface.hasSurface()) {
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
                val id = (value as? Long)?.toInt() ?: -1
                _playerState.update { it.copy(selectedAudioTrackId = id) }
            }
            MpvConstants.PROP_SUBTITLE_ID -> {
                val id = (value as? Long)?.toInt() ?: -1
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
