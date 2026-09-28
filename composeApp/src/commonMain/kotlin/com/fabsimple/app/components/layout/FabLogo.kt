package com.fabsimple.app.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Mobile App Brand Logo Icon matching the exact 4-grid modular steel block SVG logo.
 * Vibrant rounded square containing a 2x2 grid of rounded white blocks with precise opacities.
 */
@Composable
fun FabLogoIcon(
    size: Dp = 36.dp,
    modifier: Modifier = Modifier
) {
    val cornerRadius = size * 0.25f
    val blockSize = size * 0.28f
    val blockRadius = size * 0.06f
    val gapSize = size * 0.08f

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color(0xFF4F46E5))
            .border(1.dp, Color(0xFF818CF8).copy(alpha = 0.4f), RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(gapSize),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(gapSize)
            ) {
                // Top-Left (Opacity 1.0)
                Box(
                    modifier = Modifier
                        .size(blockSize)
                        .clip(RoundedCornerShape(blockRadius))
                        .background(Color.White)
                )
                // Top-Right (Opacity 0.5)
                Box(
                    modifier = Modifier
                        .size(blockSize)
                        .clip(RoundedCornerShape(blockRadius))
                        .background(Color.White.copy(alpha = 0.5f))
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(gapSize)
            ) {
                // Bottom-Left (Opacity 0.5)
                Box(
                    modifier = Modifier
                        .size(blockSize)
                        .clip(RoundedCornerShape(blockRadius))
                        .background(Color.White.copy(alpha = 0.5f))
                )
                // Bottom-Right (Opacity 0.85)
                Box(
                    modifier = Modifier
                        .size(blockSize)
                        .clip(RoundedCornerShape(blockRadius))
                        .background(Color.White.copy(alpha = 0.85f))
                )
            }
        }
    }
}

/**
 * Full Brand Logo Header (4-Grid Icon + FabSimple Text)
 */
@Composable
fun FabLogoHeader(
    iconSize: Dp = 34.dp,
    textColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FabLogoIcon(size = iconSize)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Fab",
                color = textColor,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif
            )
            Text(
                text = "Simple",
                color = Color(0xFF818CF8),
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}
