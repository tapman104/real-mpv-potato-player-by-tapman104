package com.tapman104.mpvplayer.player.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Audiotrack
import androidx.compose.material.icons.rounded.ClosedCaption
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.foundation.LocalIndication
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapman104.mpvplayer.player.model.AudioTrack
import com.tapman104.mpvplayer.player.model.DecodeMode
import com.tapman104.mpvplayer.player.model.SubtitleTrack
import com.tapman104.mpvplayer.player.dialog.AudioTrackDialog
import com.tapman104.mpvplayer.player.dialog.MoreOptionsDialog
import com.tapman104.mpvplayer.player.dialog.PlaybackSpeedDialog
import com.tapman104.mpvplayer.player.dialog.SubtitleTrackDialog


@Composable
fun PlayerTopBar(
    fileName: String,
    onBack: () -> Unit,
    onOpenFile: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    audioTracks: List<AudioTrack>,
    selectedAudioTrackId: Int,
    subtitleTracks: List<SubtitleTrack>,
    selectedSubtitleTrackId: Int,
    onSelectAudioTrack: (Int) -> Unit,
    onSelectSubtitleTrack: (Int) -> Unit,
    onSubtitleAppearanceClick: () -> Unit = {},
    currentDecodeMode: DecodeMode = DecodeMode.HWPlus,
    onDecodeModeChange: (DecodeMode) -> Unit = {},
    currentAspectRatio: com.tapman104.mpvplayer.player.model.AspectRatioMode,
    onAspectRatioClick: () -> Unit = {},
    onDialogOpenChange: (Boolean) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showAudioDialog by remember { mutableStateOf(false) }
    var showSubtitleDialog by remember { mutableStateOf(false) }
    var showMoreOptionsDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }

    val isAnyDialogOpen = showAudioDialog || showSubtitleDialog || showMoreOptionsDialog || showSpeedDialog

    LaunchedEffect(isAnyDialogOpen) {
        onDialogOpenChange(isAnyDialogOpen)
    }

    if (showAudioDialog) {
        AudioTrackDialog(
            tracks = audioTracks,
            selectedTrackId = selectedAudioTrackId,
            onSelectTrack = onSelectAudioTrack,
            onDismiss = { showAudioDialog = false }
        )
    }

    if (showSubtitleDialog) {
        SubtitleTrackDialog(
            tracks = subtitleTracks,
            selectedTrackId = selectedSubtitleTrackId,
            onSelectTrack = onSelectSubtitleTrack,
            onDisableSubtitles = { onSelectSubtitleTrack(-1) },
            onDismiss = { showSubtitleDialog = false },
            onAppearanceClick = { 
                showSubtitleDialog = false
                onSubtitleAppearanceClick()
            }
        )
    }

    if (showMoreOptionsDialog) {
        MoreOptionsDialog(
            currentDecodeMode = currentDecodeMode,
            onDecodeModeChange = onDecodeModeChange,
            currentAspectRatio = currentAspectRatio,
            onOpenFile = { onOpenFile(); showMoreOptionsDialog = false },
            onPlaybackSpeed = { showMoreOptionsDialog = false; showSpeedDialog = true },
            onLoopRepeat = { showMoreOptionsDialog = false },
            onAspectRatio = { showMoreOptionsDialog = false; onAspectRatioClick() },
            onDismiss = { showMoreOptionsDialog = false },
            onSettings = onSettingsClick
        )
    }

    if (showSpeedDialog) {
        PlaybackSpeedDialog(
            currentSpeed = 1f,
            onSelectSpeed = { onSpeedChange(it); showSpeedDialog = false },
            onDismiss = { showSpeedDialog = false }
        )
    }

    // DecodeModeDialog removed

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.9f), Color.Black.copy(alpha = 0.4f), Color.Transparent)
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = fileName,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Right side: Audio, Subtitle, DecodeMode, MoreVert
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
            ) {
                IconButton(
                    onClick = { showAudioDialog = true },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Audiotrack,
                        contentDescription = "Audio track",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
            ) {
                IconButton(
                    onClick = { showSubtitleDialog = true },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ClosedCaption,
                        contentDescription = "Subtitle track",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = LocalIndication.current
                    ) {
                        onDecodeModeChange(
                            when (currentDecodeMode) {
                                DecodeMode.HW -> DecodeMode.HWPlus
                                DecodeMode.HWPlus -> DecodeMode.SW
                                DecodeMode.SW -> DecodeMode.HW
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (currentDecodeMode) {
                        DecodeMode.HW -> "HW"
                        DecodeMode.HWPlus -> "HW+"
                        DecodeMode.SW -> "SW"
                    },
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
            ) {
                IconButton(
                    onClick = { showMoreOptionsDialog = true },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "More options",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}