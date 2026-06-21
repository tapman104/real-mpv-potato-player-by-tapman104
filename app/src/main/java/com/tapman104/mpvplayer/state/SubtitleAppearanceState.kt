package com.tapman104.mpvplayer.state

data class SubtitleAppearanceState(
    val fontSize: Int = 55,
    val fontColor: String = "#FFFFFF",
    val bold: Boolean = false,
    val borderStyle: String = "outline",
    val borderSize: Float = 2.0f,
    val shadow: Float = 0.0f,
    val backgroundAlpha: Float = 0.0f
)
