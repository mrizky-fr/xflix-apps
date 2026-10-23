package com.xflix.app.ui.intro

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.xflix.app.ui.components.MarkAspect
import com.xflix.app.ui.components.MarkLeg
import com.xflix.app.ui.components.XGeometry
import com.xflix.app.ui.components.drawXMark
import com.xflix.app.ui.theme.XColors
import kotlin.math.atan
import kotlin.random.Random

private const val IntroMs = 3300

// Sudut kaki A terhadap vertikal
private val DiveAngle = Math.toDegrees(atan((MarkAspect - MarkLeg).toDouble())).toFloat()
private val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
private val DiveEasing = CubicBezierEasing(0.65f, 0f, 0.85f, 0.25f)

private val StrandPalette = listOf(
    Color(0xFFE50914), Color(0xFFFF3B3B), Color(0xFFB20710), Color(0xFFE50914),
    Color(0xFFFF6A1A), Color(0xFFFFB020), Color(0xFFFF2E88), Color(0xFFB517FF),
    Color(0xFF6A3DFF), Color(0xFF2D7BFF), Color(0xFF00C2FF),
)

@Immutable
private class Strand(
    val u: Float,
    val color: Color,
    val thickness: Float,
    val glow: Float,
    val phase: Float,
    val speed: Float,
)

private fun buildStrands(count: Int = 72): List<Strand> {
    val rnd = Random(7)
    return List(count) { i ->
        Strand(
            u = (i + 0.5f + (rnd.nextFloat() - 0.5f) * 0.6f) / count,
            color = StrandPalette[rnd.nextInt(StrandPalette.size)],
            thickness = 0.7f + rnd.nextFloat() * 1.1f,
            glow = 0.45f + rnd.nextFloat() * 0.55f,
            phase = rnd.nextFloat(),
            speed = 0.35f + rnd.nextFloat() * 0.9f,
        )
    }
}

/** Progres 0..1 satu fase */
private fun phase(t: Float, start: Float, end: Float, easing: Easing = LinearEasing): Float =
    easing.transform(((t - start) / (end - start)).coerceIn(0f, 1f))

/** Intro pita X, ketuk lewati */
@Composable
fun IntroScreen(onFinished: () -> Unit) {
    val finished by rememberUpdatedState(onFinished)
    val progress = remember { Animatable(0f) }
    val strands = remember { buildStrands() }
    var done by remember { mutableStateOf(false) }
    val finish = {
        if (!done) {
            done = true
            finished()
        }
    }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(IntroMs, easing = LinearEasing))
        finish()
    }
    Canvas(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .semantics { contentDescription = "Xflix" }
            .pointerInput(Unit) { detectTapGestures { finish() } }
    ) {
        drawIntro(progress.value * IntroMs, strands)
    }
}

private fun DrawScope.drawIntro(t: Float, strands: List<Strand>) {
    val g = XGeometry.centered(size, size.minDimension * 0.36f)

    val revealA = phase(t, 180f, 600f, EaseOutCubic)
    val revealB = phase(t, 480f, 900f, EaseOutCubic)
    val glow = phase(t, 650f, 1300f, FastOutSlowInEasing) * (1f - phase(t, 1500f, 2100f))
    val settle = phase(t, 850f, 1500f, FastOutSlowInEasing)
    val dive = phase(t, 1400f, 2750f, DiveEasing)
    // Helaian muncul setelah pita penuh
    val strandsIn = phase(t, 1850f, 2250f)
    val fadeOut = phase(t, 2800f, 3250f)

    if (glow > 0f) {
        val radius = g.height * 1.15f
        drawCircle(
            Brush.radialGradient(
                listOf(XColors.Red.copy(alpha = 0.3f * glow), Color.Transparent),
                center = center,
                radius = radius,
            ),
            radius = radius,
            center = center,
        )
    }

    // Titik selam di kaki A
    val pivot = Offset(g.left + (g.width - g.leg) * 0.3f + g.leg * 0.5f, g.top + g.height * 0.3f)
    val scale = (1f + 0.05f * settle) * (1f + 24f * dive)

    withTransform({
        translate((center.x - pivot.x) * dive, (center.y - pivot.y) * dive)
        rotate(DiveAngle * dive, pivot)
        scale(scale, scale, pivot)
    }) {
        drawXMark(g, revealA, revealB, alphaB = 1f - phase(t, 1400f, 1650f))
        if (strandsIn > 0f) drawStrands(g, strands, t, strandsIn)
    }

    if (fadeOut > 0f) drawRect(Color.Black, alpha = fadeOut)
}

private fun DrawScope.drawStrands(g: XGeometry, strands: List<Strand>, t: Float, amount: Float) {
    val run = g.width - g.leg
    val unit = g.leg / strands.size
    clipPath(g.legA()) {
        drawRect(Color.Black, topLeft = Offset(g.left, g.top), size = Size(g.width, g.height), alpha = 0.9f * amount)
        strands.forEach { s ->
            val x = g.left + s.u * g.leg
            val start = Offset(x, g.top)
            val end = Offset(x + run, g.bottom)
            val base = lerp(XColors.Red, s.color, amount)
            // Kilatan cahaya per helai
            val head = (s.phase + t / 1000f * s.speed) % 1f
            val brush = Brush.linearGradient(
                0f to base.copy(alpha = 0.25f * s.glow),
                head * 0.85f to base.copy(alpha = 0.55f * s.glow),
                head to lerp(base, Color.White, 0.55f).copy(alpha = s.glow),
                (head + 0.12f).coerceAtMost(1f) to base.copy(alpha = 0.45f * s.glow),
                1f to base.copy(alpha = 0.2f * s.glow),
                start = start,
                end = end,
            )
            drawLine(brush, start, end, strokeWidth = unit * s.thickness, alpha = amount)
        }
    }
}
