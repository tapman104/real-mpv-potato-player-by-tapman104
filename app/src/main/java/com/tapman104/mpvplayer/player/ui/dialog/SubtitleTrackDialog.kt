package com.tapman104.mpvplayer.player.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapman104.mpvplayer.player.model.SubtitleTrack

@Composable
fun SubtitleTrackDialog(
    tracks: List<SubtitleTrack>,
    selectedTrackId: Int,
    onSelectTrack: (Int) -> Unit,
    onDisableSubtitles: () -> Unit,
    onDismiss: () -> Unit,
    onAppearanceClick: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onAppearanceClick) {
                Text(
                    "Appearance",
                    color = Color(0xFF8B5CF6),
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Close",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 13.sp
                )
            }
        },
        containerColor = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 0.dp,
        title = {
            Text(
                "Subtitle Track",
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 17.sp
            )
        },
        text = {
            if (tracks.isEmpty()) {
                Text(
                    text = "No subtitle tracks available",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .drawBehind {
                                    if (tracks.isNotEmpty()) {
                                        drawLine(
                                            color = Color.White.copy(alpha = 0.06f),
                                            start = Offset(0f, size.height),
                                            end = Offset(size.width, size.height),
                                            strokeWidth = 0.5.dp.toPx()
                                        )
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onDisableSubtitles()
                                        onDismiss()
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Disable Subtitles",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 14.sp
                                )
                                val isSelected = selectedTrackId == -1
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .border(1.5.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                        .background(if (isSelected) Color(0xFFFFB300) else Color.Transparent, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(Color.White, CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    itemsIndexed(tracks) { index, track ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .drawBehind {
                                    if (index < tracks.lastIndex) {
                                        drawLine(
                                            color = Color.White.copy(alpha = 0.06f),
                                            start = Offset(0f, size.height),
                                            end = Offset(size.width, size.height),
                                            strokeWidth = 0.5.dp.toPx()
                                        )
                                    }
                                }
                        ) {
                            TrackRow(
                                title = track.title,
                                lang = track.lang,
                                isSelected = track.id == selectedTrackId,
                                onClick = {
                                    onSelectTrack(track.id)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun TrackRow(
    title: String,
    lang: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (isSelected) Color(0xFFFFB300) else Color.White,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
            Text(
                text = "[$lang]",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 12.sp
            )
        }
        Box(
            modifier = Modifier
                .size(18.dp)
                .border(1.5.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                .background(if (isSelected) Color(0xFFFFB300) else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color.White, CircleShape)
                )
            }
        }
    }
}
