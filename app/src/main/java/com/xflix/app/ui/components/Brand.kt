package com.xflix.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xflix.app.ui.theme.Inter
import com.xflix.app.ui.theme.XColors

// Proporsi relatif tinggi X
const val MarkAspect = 0.78f
const val MarkLeg = 0.24f

private val LegShade = Color(0xFF9E0711)
private val LegHighlight = Color(0xFFFF2A2F)

/** Dua pita bersilangan */
class XGeometry(val left: Float, val top: Float, val height: Float) {
    val width = height * MarkAspect
    val leg = height * MarkLeg
    val bottom = top + height

    // Kiri-atas ke kanan-bawah
    fun legA(grow: Float = 0f) = Path().apply {
        moveTo(left - grow, top)
        lineTo(left + leg + grow, top)
        lineTo(left + width + grow, bottom)
        lineTo(left + width - leg - grow, bottom)
        close()
    }

    // Kanan-atas ke kiri-bawah
    fun legB(grow: Float = 0f) = Path().apply {
        moveTo(left + width - leg - grow, top)
        lineTo(left + width + grow, top)
        lineTo(left + leg + grow, bottom)
        lineTo(left - grow, bottom)
        close()
    }

    companion object {
        fun centered(canvas: Size, height: Float) =
            XGeometry((canvas.width - height * MarkAspect) / 2f, (canvas.height - height) / 2f, height)
    }
}

fun DrawScope.drawXMark(
    g: XGeometry,
    revealA: Float = 1f,
    revealB: Float = 1f,
    alphaB: Float = 1f,
) {
    val legA = g.legA()
    if (revealA > 0f) {
        clipRect(top = g.top, bottom = g.top + g.height * revealA) {
            drawPath(legA, Brush.verticalGradient(listOf(XColors.Red, LegShade), startY = g.top, endY = g.bottom))
        }
    }
    if (revealB <= 0f || alphaB <= 0f) return
    clipRect(top = g.bottom - g.height * revealB, bottom = g.bottom) {
        // Bayangan lipatan di kaki A
        clipPath(legA) {
            drawPath(g.legB(g.leg * 0.18f), Color.Black, alpha = 0.24f * alphaB)
            drawPath(g.legB(g.leg * 0.07f), Color.Black, alpha = 0.3f * alphaB)
        }
        drawPath(
            g.legB(),
            Brush.verticalGradient(listOf(LegHighlight, XColors.Red), startY = g.top, endY = g.bottom),
            alpha = alphaB,
        )
    }
}

@Composable
fun XMark(height: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(width = height * MarkAspect, height = height)) {
        drawXMark(XGeometry(0f, 0f, size.height))
    }
}

/** Huruf tepi lebih tinggi */
@Composable
fun XflixWordmark(modifier: Modifier = Modifier, height: Dp = 26.dp, color: Color = XColors.Red) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val letters = "XFLIX"
    val layouts = remember(height, color, density) {
        val style = TextStyle(
            fontFamily = Inter,
            fontWeight = FontWeight.Black,
            fontSize = with(density) { (height * 1.18f).toSp() },
            color = color,
        )
        letters.map { measurer.measure(it.toString(), style) }
    }
    val gap = with(density) { (height * 0.03f).toPx() }
    val widthPx = layouts.sumOf { it.size.width } + gap * (layouts.size - 1)
    val widthDp = with(density) { widthPx.toDp() }
    Canvas(modifier.size(widthDp, height)) {
        var x = 0f
        val mid = (layouts.size - 1) / 2f
        layouts.forEachIndexed { i, layout ->
            val d = (i - mid) / mid
            val bow = 1f + 0.14f * d * d
            scale(scaleX = 1f, scaleY = bow, pivot = Offset(x + layout.size.width / 2f, size.height / 2f)) {
                drawText(layout, topLeft = Offset(x, (size.height - layout.size.height) / 2f))
            }
            x += layout.size.width + gap
        }
    }
}
