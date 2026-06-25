package com.tapman104.mpvplayer.player.ui.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ResumeDialog(
    resumePositionMs: Long,
    onResume: () -> Unit,
    onStartOver: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 0.dp,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = formatResumeMs(resumePositionMs),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Continue where you left off?",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.45f)
                )
                Spacer(Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { onStartOver(); onDismiss() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.7f))
                    ) { Text("Start over", fontSize = 14.sp) }
                    Button(
                        onClick = { onResume(); onDismiss() },
                        modifier = Modifier.weight(1.4f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                    ) { Text("Resume", fontSize = 14.sp, fontWeight = FontWeight.Medium) }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
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
