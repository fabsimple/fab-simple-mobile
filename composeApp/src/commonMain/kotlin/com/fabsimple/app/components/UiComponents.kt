package com.fabsimple.app.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fabsimple.app.theme.*

/**
 * StatCard — Colored top border card with a metric value and title.
 * Replicates `.stat-card` and `.stat-value` from globals.css.
 */
@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    borderColor: Color = FabColors.Primary,
    subtext: String? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(FabShapes.StatCard)
            .border(1.dp, FabColors.Border, FabShapes.StatCard),
        colors = CardDefaults.cardColors(containerColor = FabTheme.extendedColors.cardBackground),
        shape = FabShapes.StatCard
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Colored top accent border (4px)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(borderColor)
            )

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title.uppercase(),
                    style = FabType.statLabel
                )
                Text(
                    text = value,
                    style = FabType.statValue
                )
                if (subtext != null) {
                    Text(
                        text = subtext,
                        style = FabType.statSub
                    )
                }
            }
        }
    }
}

/**
 * FabButton — Styled button using custom design system tokens.
 * Replicates `.btn`, `.btn-primary`, `.btn-sm` etc.
 */
@Composable
fun FabButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    size: ButtonSize = ButtonSize.Medium,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null
) {
    val containerColor = when (variant) {
        ButtonVariant.Primary -> FabColors.Primary
        ButtonVariant.Secondary -> FabTheme.extendedColors.mutedBackground
        ButtonVariant.Danger -> FabColors.Red
        ButtonVariant.Ghost -> Color.Transparent
    }

    val contentColor = when (variant) {
        ButtonVariant.Primary -> Color.White
        ButtonVariant.Secondary -> FabColors.TextPrimary
        ButtonVariant.Danger -> Color.White
        ButtonVariant.Ghost -> FabColors.Primary
    }

    val borderModifier = if (variant == ButtonVariant.Secondary) {
        Modifier.border(1.dp, FabColors.Border, FabShapes.Button)
    } else Modifier

    val height = when (size) {
        ButtonSize.Small -> 28.dp
        ButtonSize.Medium -> 36.dp
        ButtonSize.Large -> 48.dp
    }

    val padding = when (size) {
        ButtonSize.Small -> PaddingValues(horizontal = 10.dp)
        ButtonSize.Medium -> PaddingValues(horizontal = 14.dp)
        ButtonSize.Large -> PaddingValues(horizontal = 20.dp)
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .height(height)
            .then(borderModifier),
        enabled = enabled,
        shape = FabShapes.Button,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.6f),
            disabledContentColor = contentColor.copy(alpha = 0.6f)
        ),
        contentPadding = padding
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) {
                icon()
            }
            Text(
                text = text,
                style = if (size == ButtonSize.Small) FabType.buttonSmall else FabType.button,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

enum class ButtonVariant { Primary, Secondary, Danger, Ghost }
enum class ButtonSize { Small, Medium, Large }

/**
 * FabTextField — Styled text field with labels and error support.
 * Replicates `.fld` and `.input` from globals.css.
 */
@Composable
fun FabTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    error: String? = null,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (label != null) {
            Text(
                text = label,
                style = FabType.label
            )
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = placeholder?.let { { Text(it, style = FabType.input.copy(color = FabColors.TextFaint)) } },
            isError = error != null,
            singleLine = singleLine,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            trailingIcon = trailingIcon,
            shape = FabShapes.Input,
            textStyle = FabType.input,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = FabColors.TextPrimary,
                unfocusedTextColor = FabColors.TextPrimary,
                focusedContainerColor = FabTheme.extendedColors.cardBackground,
                unfocusedContainerColor = FabTheme.extendedColors.cardBackground,
                focusedBorderColor = FabColors.Primary,
                unfocusedBorderColor = FabColors.Border,
                errorBorderColor = FabColors.Red
            )
        )

        if (error != null) {
            Text(
                text = error,
                color = FabColors.Red,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * ProgressBar — Horizontal progress bar.
 * Replicates `.pbar` and `.pbar-fill` from globals.css.
 */
@Composable
fun ProgressBar(
    progress: Float, // 0.0f to 1.0f
    modifier: Modifier = Modifier,
    color: Color = FabColors.Primary,
    backgroundColor: Color = FabColors.Border
) {
    val animatedProgress = animateFloatAsState(targetValue = progress.coerceIn(0f, 1f))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress.value)
                .clip(RoundedCornerShape(6.dp))
                .background(color)
        )
    }
}

/**
 * SearchBar — Command search panel input field.
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search projects, parts, drawings..."
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, style = FabType.input.copy(color = FabColors.TextFaint)) },
        singleLine = true,
        shape = FabShapes.Input,
        textStyle = FabType.input,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = FabColors.TextPrimary,
            unfocusedTextColor = FabColors.TextPrimary,
            focusedContainerColor = FabTheme.extendedColors.cardBackground,
            unfocusedContainerColor = FabTheme.extendedColors.cardBackground,
            focusedBorderColor = FabColors.Primary,
            unfocusedBorderColor = FabColors.Border
        )
    )
}

/**
 * TabRow — Replicates `.tab-row` and `.tab-btn` horizontal tab buttons.
 */
@Composable
fun TabRow(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(FabTheme.extendedColors.cardBackground, RoundedCornerShape(12.dp))
            .border(1.dp, FabColors.Border, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = index == selectedTabIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) FabColors.Primary else Color.Transparent)
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = if (isSelected) Color.White else FabColors.TextSecondary,
                    style = FabType.button,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * AlertBanner — Colored alert block.
 * Replicates `.alert-warn`, `.alert-danger` classes.
 */
@Composable
fun AlertBanner(
    message: String,
    variant: AlertVariant = AlertVariant.Warning,
    modifier: Modifier = Modifier
) {
    val (bg, border, text) = when (variant) {
        AlertVariant.Warning -> Triple(FabColors.AmberBg, FabColors.AmberBorder, FabColors.AmberDark)
        AlertVariant.Danger -> Triple(FabColors.RedBg, FabColors.RedBorder, FabColors.Red)
        AlertVariant.Info -> Triple(FabColors.BlueBg, FabColors.BlueBorder, FabColors.Blue)
        AlertVariant.Success -> Triple(FabColors.GreenBg, FabColors.GreenBorder, FabColors.Green)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(bg, FabShapes.Alert)
            .border(1.dp, border, FabShapes.Alert)
            .padding(12.dp)
    ) {
        Text(
            text = message,
            style = FabType.bodySmall.copy(color = text),
            fontWeight = FontWeight.Medium
        )
    }
}

enum class AlertVariant { Warning, Danger, Info, Success }
