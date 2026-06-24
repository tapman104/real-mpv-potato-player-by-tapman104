package com.tapman104.mpvplayer.player.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapman104.mpvplayer.player.model.DecodeMode

@Composable
fun DecodeModeDialog(
    currentDecodeMode: DecodeMode,
    onSelectMode: (DecodeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Decode Mode",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                DecodeModeOption(
                    title = "Software (SW)",
                    description = "Slowest, most compatible",
                    isSelected = currentDecodeMode == DecodeMode.SW,
                    onClick = {
                        onSelectMode(DecodeMode.SW)
                        onDismiss()
                    }
                )
                DecodeModeOption(
                    title = "Hardware (HW)",
                    description = "Fast, uses device decoder",
                    isSelected = currentDecodeMode == DecodeMode.HW,
                    onClick = {
                        onSelectMode(DecodeMode.HW)
                        onDismiss()
                    }
                )
                DecodeModeOption(
                    title = "Hardware+ (HW+)",
                    description = "Hardware + copy-back (Recommended)",
                    isSelected = currentDecodeMode == DecodeMode.HWPlus,
                    onClick = {
                        onSelectMode(DecodeMode.HWPlus)
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
        containerColor = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 8.dp
    )
}

@Composable
private fun DecodeModeOption(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFFFFB300).copy(alpha = 0.15f) else Color(0xFF2A2A2A)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (isSelected) Color(0xFFFFB300) else Color.White,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Icon(
            imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
            contentDescription = null,
            tint = if (isSelected) Color(0xFFFFB300) else Color.White.copy(alpha = 0.4f),
            modifier = Modifier.size(24.dp)
        )
    }
}
