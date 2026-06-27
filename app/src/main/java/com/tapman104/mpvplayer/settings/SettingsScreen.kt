package com.tapman104.mpvplayer.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    preferredSubtitleLang: String,
    onSubtitleLangChange: (String) -> Unit,
    subtitleSize: Float,
    subtitlePosition: Float,
    onSubtitleSizeChange: (Float) -> Unit,
    onSubtitlePositionChange: (Float) -> Unit,
    resumePlayback: Boolean,
    onResumePlaybackChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Settings",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)

        // Section: Subtitles
        Text(
            text = "Subtitles",
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        )

        // Preferred Subtitle Language row
        val subtitleLangs = listOf("eng", "jpn", "kor", "none")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val currentIndex = subtitleLangs.indexOf(preferredSubtitleLang)
                    val nextIndex = if (currentIndex < 0) 1 else (currentIndex + 1) % subtitleLangs.size
                    onSubtitleLangChange(subtitleLangs[nextIndex])
                }
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Preferred Subtitle Language",
                color = Color.White,
                fontSize = 15.sp
            )
            Text(
                text = preferredSubtitleLang,
                color = Color(0xFF8B5CF6),
                fontSize = 13.sp
            )
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)

        SubtitleAppearanceSection(
            subtitleSize = subtitleSize,
            subtitlePosition = subtitlePosition,
            onSizeChange = onSubtitleSizeChange,
            onPositionChange = onSubtitlePositionChange
        )

        // Section: Playback
        Text(
            text = "Playback",
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        )

        // Resume Playback toggle row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Resume Playback", color = Color.White, fontSize = 15.sp)
            Switch(
                checked = resumePlayback,
                onCheckedChange = onResumePlaybackChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF8B5CF6),
                    uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
                )
            )
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)

        AboutSection()
    }
}
