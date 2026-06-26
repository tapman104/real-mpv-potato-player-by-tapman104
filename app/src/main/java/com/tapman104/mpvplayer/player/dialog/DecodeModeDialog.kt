package com.tapman104.mpvplayer.player.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Speed
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
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
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
                    icon = Icons.Outlined.Memory,
                    isSelected = currentDecodeMode == DecodeMode.SW,
                    onClick = {
                        onSelectMode(DecodeMode.SW)
                        onDismiss()
                    }
                )
                DecodeModeOption(
                    title = "Hardware (HW)",
                    description = "Fast, uses device decoder",
                    icon = Icons.Outlined.Speed,
                    isSelected = currentDecodeMode == DecodeMode.HW,
                    onClick = {
                        onSelectMode(DecodeMode.HW)
                        onDismiss()
                    }
                )
                DecodeModeOption(
                    title = "Hardware+ (HW+)",
                    description = "Hardware + copy-back (Recommended)",
                    icon = Icons.Outlined.FlashOn,
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
                Text("Close", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
            }
        },
        containerColor = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 0.dp
    )
}

@Composable
private fun DecodeModeOption(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFF8B5CF6).copy(alpha = 0.10f) else Color(0xFF232323)
    val borderColor = if (isSelected) Color(0xFF8B5CF6).copy(alpha = 0.30f) else Color.White.copy(alpha = 0.06f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(0.5.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) Color(0xFF8B5CF6).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color(0xFF8B5CF6) else Color.White.copy(alpha = 0.55f),
                modifier = Modifier.size(18.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (isSelected) Color(0xFF8B5CF6) else Color.White,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 12.sp
            )
        }
        
        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .size(18.dp)
                .border(1.5.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                .background(if (isSelected) Color(0xFF8B5CF6) else Color.Transparent, CircleShape),
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
