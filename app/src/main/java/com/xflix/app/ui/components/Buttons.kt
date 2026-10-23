package com.xflix.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xflix.app.ui.theme.Dimens
import com.xflix.app.ui.theme.Space
import com.xflix.app.ui.theme.XColors
import com.xflix.app.ui.theme.XType

enum class XButtonStyle { White, Red, Gray }

private data class ButtonColors(val container: Color, val pressed: Color, val content: Color)

private fun XButtonStyle.colors() = when (this) {
    XButtonStyle.White -> ButtonColors(Color.White, Color(0xFFD6D6D6), Color.Black)
    XButtonStyle.Red -> ButtonColors(XColors.Red, XColors.RedPressed, Color.White)
    XButtonStyle.Gray -> ButtonColors(Color(0xFF2B2B2B), Color(0xFF3A3A3A), Color.White)
}

@Composable
fun XButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: XButtonStyle = XButtonStyle.White,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    height: Dp = Dimens.ButtonHeight,
    leading: (@Composable () -> Unit)? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val colors = style.colors()
    // Nonaktif: garis abu, latar transparan
    val container by animateColorAsState(
        targetValue = when {
            !enabled -> Color.Transparent
            pressed -> colors.pressed
            else -> colors.container
        },
        animationSpec = tween(180),
        label = "button-container",
    )
    val content by animateColorAsState(
        targetValue = if (enabled) colors.content else XColors.TextMuted,
        animationSpec = tween(180),
        label = "button-content",
    )
    val borderColor by animateColorAsState(
        targetValue = if (enabled) Color.Transparent else XColors.TextDisabled,
        animationSpec = tween(180),
        label = "button-border",
    )
    val shape = RoundedCornerShape(Dimens.Radius)
    Box(
        modifier
            .height(height)
            .animatedPressScale(pressed, 0.97f)
            .clip(shape)
            .background(container)
            .border(1.dp, borderColor, shape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled && !loading,
                onClick = onClick,
            )
            .padding(horizontal = Space.l),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = loading,
            transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(120)) },
            label = "button-loading",
        ) { isLoading ->
            if (isLoading) {
                XSpinner(size = 22.dp, stroke = 2.5.dp, color = content)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when {
                        leading != null -> {
                            leading()
                            Spacer(Modifier.width(Space.s))
                        }
                        icon != null -> {
                            Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(Space.s))
                        }
                    }
                    Text(text, style = XType.Button, color = content, maxLines = 1)
                }
            }
        }
    }
}

/** Ikon di atas label */
@Composable
fun IconAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    iconModifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Column(
        modifier
            .widthIn(min = 64.dp)
            .animatedPressScale(pressed, 0.9f)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(vertical = Space.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        AnimatedContent(
            targetState = icon,
            transitionSpec = {
                (scaleIn(tween(220), initialScale = 0.5f) + fadeIn(tween(160))) togetherWith
                    (scaleOut(tween(160), targetScale = 1.4f) + fadeOut(tween(120)))
            },
            label = "icon-action",
        ) { current ->
            Icon(current, contentDescription = null, tint = tint, modifier = iconModifier.size(24.dp))
        }
        Text(label, style = XType.Caption, color = XColors.TextSecondary, textAlign = TextAlign.Center, maxLines = 1)
    }
}

@Composable
fun BarIcon(icon: ImageVector, contentDescription: String?, onClick: () -> Unit, modifier: Modifier = Modifier, tint: Color = Color.White) {
    Box(
        modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = ripple(bounded = false, radius = 20.dp), onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(23.dp))
    }
}

/** Tombol bulat di atas gambar */
@Composable
fun ScrimIcon(icon: ImageVector, contentDescription: String?, onClick: () -> Unit, modifier: Modifier = Modifier, size: Dp = 34.dp) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = Color.White, modifier = Modifier.size(size * 0.55f))
    }
}

@Composable
fun TextAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, color: Color = Color.White) {
    Text(
        text = text,
        style = XType.Label,
        color = color,
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.Radius))
            .clickable(onClick = onClick)
            .padding(horizontal = Space.s, vertical = Space.s),
    )
}
