package com.tapman104.mpvplayer.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SubtitleAppearanceSection(
    subtitleSize: Float,
    subtitlePosition: Float,
    onSizeChange: (Float) -> Unit,
    onPositionChange: (Float) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {

        // --- Subtitle Size row ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Subtitle Size", color = Color.White, fontSize = 15.sp)
                Text(
                    "${"%.1f".format(subtitleSize)}×",
                    color = Color(0xFF8B5CF6),
                    fontSize = 13.sp
                )
            }
            Slider(
                value = subtitleSize,
                onValueChange = onSizeChange,
                valueRange = 0.5f..3.0f,
                steps = 9,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color(0xFF8B5CF6),
                    inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)

        // --- Subtitle Position row ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            val positionLabel = when {
                subtitlePosition >= 0.66f -> "Top"
                subtitlePosition >= 0.33f -> "Middle"
                else -> "Bottom"
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Subtitle Position", color = Color.White, fontSize = 15.sp)
                Text(positionLabel, color = Color(0xFF8B5CF6), fontSize = 13.sp)
            }
            Slider(
                value = subtitlePosition,
                onValueChange = onPositionChange,
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color(0xFF8B5CF6),
                    inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)
    }
}
