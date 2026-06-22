package com.tapman104.mpvplayer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.FolderOpen
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapman104.mpvplayer.state.AudioTrack
import com.tapman104.mpvplayer.state.SubtitleTrack
import com.tapman104.mpvplayer.ui.dialogs.AudioTrackDialog
import com.tapman104.mpvplayer.ui.dialogs.SubtitleTrackDialog
import com.tapman104.mpvplayer.ui.components.SubtitleAppearanceDialog

@Composable
fun PlayerTopBar(
    fileName: String,
    onOpenFile: () -> Unit,
    audioTracks: List<AudioTrack>,
    selectedAudioTrackId: Int,
    subtitleTracks: List<SubtitleTrack>,
    selectedSubtitleTrackId: Int,
    onSelectAudioTrack: (Int) -> Unit,
    onSelectSubtitleTrack: (Int) -> Unit,
    subtitleSize: Float = 1.0f,
    subtitlePosition: Float = 0.12f,
    onSubtitleAppearance: (size: Float, position: Float) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var showAudioDialog by remember { mutableStateOf(false) }
    var showSubtitleDialog by remember { mutableStateOf(false) }
    var showSubtitleAppearance by remember { mutableStateOf(false) }

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
                showSubtitleAppearance = true
            }
        )
    }

    if (showSubtitleAppearance) {
        SubtitleAppearanceDialog(
            initialSize = subtitleSize,
            initialPosition = subtitlePosition,
            onApply = { size, position ->
                onSubtitleAppearance(size, position)
                showSubtitleAppearance = false
            },
            onDismiss = { showSubtitleAppearance = false },
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = fileName,
            color = Color.White,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .shadow(elevation = 4.dp, ambientColor = Color.Black, spotColor = Color.Black)
        )
        IconButton(
            onClick = { showAudioDialog = true },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Audiotrack,
                contentDescription = "Audio track",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        IconButton(
            onClick = { showSubtitleDialog = true },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ClosedCaption,
                contentDescription = "Subtitle track",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        IconButton(
            onClick = onOpenFile,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.FolderOpen,
                contentDescription = "Open file",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
