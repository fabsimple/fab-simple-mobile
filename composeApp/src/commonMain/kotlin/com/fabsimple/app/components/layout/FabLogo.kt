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
 * Mobile App Brand Logo Icon matching exact uploaded logo design.
 * Vibrant rounded square containing a bold white "F" and "FAB" subtext.
 */
@Composable
fun FabLogoIcon(
    size: Dp = 36.dp,
    modifier: Modifier = Modifier
) {
    val cornerRadius = size * 0.28f
    val fSize = (size.value * 0.52f).sp
    val fabSubSize = (size.value * 0.18f).sp

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color(0xFF4C47F4))
            .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.5f), RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = 1.dp)
        ) {
            Text(
                text = "F",
                color = Color.White,
                fontSize = fSize,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                lineHeight = fSize
            )
            Text(
                text = "FAB",
                color = Color.White,
                fontSize = fabSubSize,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 1.sp,
                lineHeight = fabSubSize
            )
        }
    }
}

/**
 * Full Brand Logo Header (Icon + FabSimple Text)
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
