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
        title = {
            Text(
                text = "More Options",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MoreOptionsRow(
                    icon = Icons.Filled.FolderOpen,
                    label = "Open File",
                    onClick = {
                        onOpenFile()
                        onDismiss()
                    }
                )
                MoreOptionsRow(
                    icon = Icons.Filled.Speed,
                    label = "Playback Speed",
                    onClick = {
                        onPlaybackSpeed()
                        onDismiss()
                    }
                )
                MoreOptionsRow(
                    icon = Icons.Filled.Loop,
                    label = "Loop / Repeat",
                    onClick = {
                        onLoopRepeat()
                        onDismiss()
                    }
                )
                MoreOptionsRow(
                    icon = Icons.Filled.AspectRatio,
                    label = "Aspect Ratio",
                    onClick = {
                        onAspectRatio()
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFFFFB300), fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {},
        containerColor = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 8.dp
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
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2A2A2A))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFFFFB300),
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}
