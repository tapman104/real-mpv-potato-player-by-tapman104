package com.tapman104.mpvplayer.player.ui.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Audiotrack
import androidx.compose.material.icons.rounded.ClosedCaption
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapman104.mpvplayer.player.model.AudioTrack
import com.tapman104.mpvplayer.player.model.SubtitleTrack
import com.tapman104.mpvplayer.player.ui.dialog.AudioTrackDialog
import com.tapman104.mpvplayer.player.ui.dialog.MoreOptionsDialog
import com.tapman104.mpvplayer.player.ui.dialog.PlaybackSpeedDialog
import com.tapman104.mpvplayer.player.ui.dialog.SubtitleTrackDialog


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
    modifier: Modifier = Modifier
) {
    var showAudioDialog by remember { mutableStateOf(false) }
    var showSubtitleDialog by remember { mutableStateOf(false) }
    var showMoreOptionsDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }

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
            onAppearanceClick = { showSubtitleDialog = false }
        )
    }

    if (showMoreOptionsDialog) {
        MoreOptionsDialog(
            onOpenFile = { onOpenFile(); showMoreOptionsDialog = false },
            onPlaybackSpeed = { showMoreOptionsDialog = false; showSpeedDialog = true },
            onLoopRepeat = { showMoreOptionsDialog = false },
            onAspectRatio = { showMoreOptionsDialog = false },
            onDismiss = { showMoreOptionsDialog = false }
        )
    }

    if (showSpeedDialog) {
        PlaybackSpeedDialog(
            currentSpeed = 1f,
            onSelectSpeed = { onSpeedChange(it); showSpeedDialog = false },
            onDismiss = { showSpeedDialog = false }
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = fileName,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Right side: Audio, Subtitle, FolderOpen, MoreVert
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { showAudioDialog = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Audiotrack,
                    contentDescription = "Audio track",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            IconButton(
                onClick = { showSubtitleDialog = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ClosedCaption,
                    contentDescription = "Subtitle track",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            IconButton(
                onClick = { showMoreOptionsDialog = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "More options",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
