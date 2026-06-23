package com.tapman104.mpvplayer.player.ui.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun ResumeDialog(
    resumePositionMs: Long,
    onResume: () -> Unit,
    onStartOver: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        title = {
            Text(
                text = "Resume Playback",
                color = Color.White
            )
        },
        text = {
            Text(
                text = "Resume from ${formatResumeMs(resumePositionMs)}?",
                color = Color.White.copy(alpha = 0.87f)
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onResume()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B5CF6)
                )
            ) {
                Text("Resume", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onStartOver()
                    onDismiss()
                }
            ) {
                Text("Start Over", color = Color.White.copy(alpha = 0.7f))
            }
        }
    )
}

/**
 * Formats milliseconds as HH:MM:SS if hours > 0, otherwise MM:SS.
 */
private fun formatResumeMs(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
