package com.tapman104.mpvplayer.state

data class PlayerState(
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val audioTracks: List<AudioTrack> = emptyList(),
    val subtitleTracks: List<SubtitleTrack> = emptyList(),
    val selectedAudioTrackId: Int = -1,
    val selectedSubtitleTrackId: Int = -1,
    val speed: Float = 1.0f,
    val volume: Int = 100,
    val decodeMode: DecodeMode = DecodeMode.HWPlus,
    val isLoading: Boolean = true,
    val error: String? = null
)
