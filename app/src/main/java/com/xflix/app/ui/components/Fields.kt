package com.xflix.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.xflix.app.ui.theme.Dimens
import com.xflix.app.ui.theme.Motion
import com.xflix.app.ui.theme.Space
import com.xflix.app.ui.theme.XColors
import com.xflix.app.ui.theme.XType

/** Field dengan label mengambang */
@Composable
fun XTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    password: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailing: (@Composable () -> Unit)? = null,
) {
    var focused by remember { mutableStateOf(false) }
    val focus = remember { FocusRequester() }
    val floated by animateFloatAsState(
        targetValue = if (focused || value.isNotEmpty()) 1f else 0f,
        animationSpec = tween(180, easing = Motion.Emphasized),
        label = "label-float",
    )
    val border by animateColorAsState(
        targetValue = when {
            error != null -> XColors.Warning
            focused -> Color(0xFF8C8C8C)
            else -> Color.Transparent
        },
        animationSpec = tween(160),
        label = "field-border",
    )
    var lastError by remember { mutableStateOf("") }
    if (error != null) lastError = error

    Column(modifier) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(54.dp)
                .background(XColors.Input, RoundedCornerShape(Dimens.Radius))
                .border(1.dp, border, RoundedCornerShape(Dimens.Radius))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { focus.requestFocus() }
                .padding(start = Space.l, end = if (trailing != null) Space.xs else Space.l),
        ) {
            Text(
                text = label,
                style = XType.Body.copy(fontSize = 15.sp, color = XColors.TextMuted),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .graphicsLayer {
                        val s = lerp(1f, 0.74f, floated)
                        scaleX = s
                        scaleY = s
                        transformOrigin = TransformOrigin(0f, 0.5f)
                        translationY = lerp(0f, -11.dp.toPx(), floated)
                    },
            )
            Row(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focus)
                        .onFocusChanged { focused = it.isFocused },
                    textStyle = XType.Body.copy(fontSize = 15.sp, color = Color.White),
                    cursorBrush = SolidColor(Color.White),
                    singleLine = true,
                    visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                )
                if (trailing != null) {
                    Spacer(Modifier.width(Space.xs))
                    Box(Modifier.padding(bottom = 0.dp)) { trailing() }
                }
            }
        }
        AnimatedVisibility(
            visible = error != null,
            enter = expandVertically(tween(180)) + fadeIn(tween(180)),
            exit = shrinkVertically(tween(160)) + fadeOut(tween(120)),
        ) {
            Text(
                lastError,
                style = XType.Caption.copy(fontSize = 12.sp, color = XColors.Warning),
                modifier = Modifier.padding(top = 6.dp, start = 2.dp),
            )
        }
    }
}

/** Centang yang tergambar */
@Composable
fun XCheckbox(checked: Boolean, modifier: Modifier = Modifier) {
    val draw by animateFloatAsState(if (checked) 1f else 0f, tween(220, easing = Motion.Emphasized), label = "check")
    val box by animateColorAsState(if (checked) Color.White else Color.Transparent, tween(160), label = "check-box")
    Canvas(
        modifier
            .size(20.dp)
            .background(box, RoundedCornerShape(3.dp))
            .border(1.5.dp, if (checked) Color.White else XColors.TextMuted, RoundedCornerShape(3.dp))
    ) {
        if (draw <= 0f) return@Canvas
        val w = size.width
        val a = Offset(w * 0.22f, w * 0.52f)
        val b = Offset(w * 0.42f, w * 0.72f)
        val c = Offset(w * 0.8f, w * 0.3f)
        val first = (draw / 0.4f).coerceAtMost(1f)
        val second = ((draw - 0.4f) / 0.6f).coerceIn(0f, 1f)
        val path = Path().apply {
            moveTo(a.x, a.y)
            lineTo(a.x + (b.x - a.x) * first, a.y + (b.y - a.y) * first)
            if (second > 0f) lineTo(b.x + (c.x - b.x) * second, b.y + (c.y - b.y) * second)
        }
        drawPath(path, Color.Black, style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}
