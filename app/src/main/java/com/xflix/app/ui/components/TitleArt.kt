package com.xflix.app.ui.components

import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.modifiers.TextAutoSizeLayoutScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.xflix.app.data.Title
import com.xflix.app.data.TitleStyle
import com.xflix.app.ui.theme.Inter

enum class ArtSize(val scale: Float) { Card(0.85f), Poster(1f), Detail(1.5f), Hero(1.95f) }

private val ArtShadow = Shadow(Color.Black.copy(alpha = 0.6f), Offset(0f, 2f), 14f)

/** Logo judul per film */
@Composable
fun TitleArt(
    title: Title,
    size: ArtSize,
    modifier: Modifier = Modifier,
    align: TextAlign = TextAlign.Center,
    maxLines: Int = 2,
) {
    val k = size.scale
    // Ukuran dalam em agar ikut autoSize
    val (style, maxSize) = when (title.style) {
        TitleStyle.Condensed -> TextStyle(
            fontFamily = Inter, fontWeight = FontWeight.Black,
            lineHeight = 1.05.em, letterSpacing = (-0.02).em,
        ) to 20 * k
        TitleStyle.Serif, TitleStyle.SerifItalic -> TextStyle(
            fontFamily = Inter, fontWeight = FontWeight.Bold,
            lineHeight = 1.1.em, letterSpacing = (-0.02).em,
        ) to 19 * k
        TitleStyle.Wide -> TextStyle(
            fontFamily = Inter, fontWeight = FontWeight.SemiBold,
            lineHeight = 1.33.em, letterSpacing = 0.3.em,
        ) to 10.5f * k
    }
    val text = when (title.style) {
        TitleStyle.Condensed, TitleStyle.Wide -> title.name.uppercase()
        else -> title.name
    }
    Text(
        text = text,
        modifier = modifier,
        style = style.copy(color = Color.White, shadow = ArtShadow, textAlign = align),
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        autoSize = FitWholeWords(max = maxSize.sp, min = (maxSize * 0.55f).sp),
    )
}

/** Mengecil sampai kata tidak terpotong */
private class FitWholeWords(private val max: TextUnit, private val min: TextUnit) : TextAutoSize {
    override fun TextAutoSizeLayoutScope.getFontSize(constraints: Constraints, text: AnnotatedString): TextUnit {
        var size = max.value
        while (size > min.value) {
            val layout = performLayout(constraints, text, size.sp)
            if (!layout.hasVisualOverflow && !layout.splitsWord(text)) return size.sp
            size -= 0.5f
        }
        return min
    }

    override fun equals(other: Any?) = other is FitWholeWords && other.max == max && other.min == min

    override fun hashCode() = 31 * max.hashCode() + min.hashCode()
}

private fun TextLayoutResult.splitsWord(text: AnnotatedString): Boolean =
    (0 until lineCount - 1).any { line ->
        val end = getLineEnd(line)
        end in 1 until text.length && !text[end - 1].isWhitespace() && !text[end].isWhitespace()
    }
