package com.tapman104.mpvplayer.player.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MoreOptionsDialog(
    currentDecodeMode: com.tapman104.mpvplayer.player.model.DecodeMode,
    onDecodeModeChange: (com.tapman104.mpvplayer.player.model.DecodeMode) -> Unit,
    currentAspectRatio: com.tapman104.mpvplayer.player.model.AspectRatioMode,
    onOpenFile: () -> Unit,
    onPlaybackSpeed: () -> Unit,
    onLoopRepeat: () -> Unit,
    onAspectRatio: () -> Unit,
    onDismiss: () -> Unit,
    onSettings: () -> Unit = {}
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.CenterEnd
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(320.dp)
                    .clickable(
                        interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    bottomStart = 20.dp,
                    topEnd = 0.dp,
                    bottomEnd = 0.dp
                ),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "More Options",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        fontSize = 17.sp,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    
                    Column(modifier = Modifier.fillMaxWidth()) {
                        MoreOptionsRow(
                            icon = Icons.Filled.FolderOpen,
                            label = "Open File",
                            onClick = {
                                onOpenFile()
                                onDismiss()
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f), thickness = 0.5.dp)
                        MoreOptionsRow(
                            icon = Icons.Filled.Speed,
                            label = "Playback Speed",
                            onClick = {
                                onPlaybackSpeed()
                                onDismiss()
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f), thickness = 0.5.dp)
                        MoreOptionsRow(
                            icon = Icons.Filled.Loop,
                            label = "Loop / Repeat",
                            onClick = {
                                onLoopRepeat()
                                onDismiss()
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f), thickness = 0.5.dp)
                        MoreOptionsRow(
                            icon = Icons.Filled.AspectRatio,
                            label = "Aspect Ratio: ${currentAspectRatio.displayName}",
                            onClick = {
                                onAspectRatio()
                                onDismiss()
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f), thickness = 0.5.dp)
                        MoreOptionsRow(
                            icon = Icons.Filled.Speed, // Using Speed icon as a fallback, ideally a Memory icon
                            label = "Decoder: ${when(currentDecodeMode) {
                                com.tapman104.mpvplayer.player.model.DecodeMode.HW -> "HW"
                                com.tapman104.mpvplayer.player.model.DecodeMode.HWPlus -> "HW+"
                                com.tapman104.mpvplayer.player.model.DecodeMode.SW -> "SW"
                            }}",
                            onClick = {
                                val nextMode = when (currentDecodeMode) {
                                    com.tapman104.mpvplayer.player.model.DecodeMode.HW -> com.tapman104.mpvplayer.player.model.DecodeMode.HWPlus
                                    com.tapman104.mpvplayer.player.model.DecodeMode.HWPlus -> com.tapman104.mpvplayer.player.model.DecodeMode.SW
                                    com.tapman104.mpvplayer.player.model.DecodeMode.SW -> com.tapman104.mpvplayer.player.model.DecodeMode.HW
                                }
                                onDecodeModeChange(nextMode)
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f), thickness = 0.5.dp)
                        MoreOptionsRow(
                            icon = Icons.Rounded.Settings,
                            label = "Settings",
                            onClick = {
                                onSettings()
                                onDismiss()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Close", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun MoreOptionsRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.size(17.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
