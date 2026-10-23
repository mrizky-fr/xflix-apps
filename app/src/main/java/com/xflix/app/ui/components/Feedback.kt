package com.xflix.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xflix.app.ui.theme.Space
import com.xflix.app.ui.theme.XColors
import com.xflix.app.ui.theme.XType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Loader cincin merah */
@Composable
fun XSpinner(modifier: Modifier = Modifier, size: Dp = 44.dp, stroke: Dp = 3.5.dp, color: Color = XColors.Red) {
    val spin = rememberInfiniteTransition(label = "spinner")
    val angle by spin.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(850, easing = LinearEasing)),
        label = "spinner-angle",
    )
    Canvas(modifier.size(size)) {
        val width = stroke.toPx()
        rotate(angle) {
            drawArc(
                brush = Brush.sweepGradient(0f to Color.Transparent, 0.75f to color, center = center),
                startAngle = 0f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(width / 2f, width / 2f),
                size = this.size.copy(this.size.width - width, this.size.height - width),
                style = Stroke(width, cap = StrokeCap.Round),
            )
        }
    }
}

/** Skeleton dengan kilau */
@Composable
fun SkeletonBlock(modifier: Modifier = Modifier, corner: Dp = 4.dp) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val x by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer-x",
    )
    Box(
        modifier
            .background(XColors.SurfaceRaised, RoundedCornerShape(corner))
            .drawWithContent {
                val w = size.width
                drawRect(
                    Brush.linearGradient(
                        0f to Color.Transparent,
                        0.5f to Color.White.copy(alpha = 0.06f),
                        1f to Color.Transparent,
                        start = Offset(w * x - w * 0.6f, 0f),
                        end = Offset(w * x, size.height * 0.4f),
                    )
                )
            }
    )
}

/** Mengecil sedikit saat ditekan. */
@Composable
fun Modifier.animatedPressScale(pressed: Boolean, scale: Float = 0.96f): Modifier {
    val s by animateFloatAsState(
        targetValue = if (pressed) scale else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "press",
    )
    return this.graphicsLayer {
        scaleX = s
        scaleY = s
    }
}

@Stable
class Toaster(private val scope: CoroutineScope) {
    var message by mutableStateOf<String?>(null)
        private set
    private var job: Job? = null

    fun show(text: String) {
        job?.cancel()
        message = text
        job = scope.launch {
            delay(2400)
            message = null
        }
    }
}

val LocalToaster = staticCompositionLocalOf<Toaster> { error("Toaster belum disediakan") }

@Composable
fun ToastHost(toaster: Toaster, modifier: Modifier = Modifier) {
    var last by remember { mutableStateOf("") }
    toaster.message?.let { last = it }
    AnimatedVisibility(
        visible = toaster.message != null,
        modifier = modifier,
        enter = slideInVertically(tween(260)) { it / 2 } + fadeIn(tween(200)) + scaleIn(initialScale = 0.96f),
        exit = slideOutVertically(tween(200)) { it / 3 } + fadeOut(tween(160)),
    ) {
        Box(
            Modifier
                .background(XColors.SurfaceHigh, RoundedCornerShape(6.dp))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                .padding(horizontal = Space.l, vertical = Space.m),
            contentAlignment = Alignment.Center,
        ) {
            Text(last, style = XType.Label)
        }
    }
}
