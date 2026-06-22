package com.tapman104.mpvplayer.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MoreOptionsDialog(
    onOpenFile: () -> Unit,
    onPlaybackSpeed: () -> Unit,
    onLoopRepeat: () -> Unit,
    onAspectRatio: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color.White)
            }
        },
        containerColor = Color(0xFF1E1E1E),
        title = {
            Text("More Options", color = Color.White)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                MoreOptionsRow(
                    icon = Icons.Filled.FolderOpen,
                    label = "Open File",
                    onClick = {
                        onOpenFile()
                        onDismiss()
                    }
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)
                MoreOptionsRow(
                    icon = Icons.Filled.Speed,
                    label = "Playback Speed",
                    onClick = {
                        onPlaybackSpeed()
                        onDismiss()
                    }
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)
                MoreOptionsRow(
                    icon = Icons.Filled.Loop,
                    label = "Loop / Repeat",
                    onClick = {
                        onLoopRepeat()
                        onDismiss()
                    }
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)
                MoreOptionsRow(
                    icon = Icons.Filled.AspectRatio,
                    label = "Aspect Ratio",
                    onClick = {
                        onAspectRatio()
                        onDismiss()
                    }
                )
            }
        }
    )
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
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.White
        )
    }
}
