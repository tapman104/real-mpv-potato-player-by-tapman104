package com.tapman104.mpvplayer.player.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
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
                Text("Close", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
            }
        },
        containerColor = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 0.dp,
        title = {
            Text("More Options", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 17.sp)
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
                HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)
                MoreOptionsRow(
                    icon = Icons.Filled.Speed,
                    label = "Playback Speed",
                    onClick = {
                        onPlaybackSpeed()
                        onDismiss()
                    }
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)
                MoreOptionsRow(
                    icon = Icons.Filled.Loop,
                    label = "Loop / Repeat",
                    onClick = {
                        onLoopRepeat()
                        onDismiss()
                    }
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)
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
            .padding(vertical = 12.dp, horizontal = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(17.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.White
        )
    }
}
