package com.xflix.app.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.xflix.app.data.Title
import com.xflix.app.data.TitleStyle
import com.xflix.app.ui.theme.BigShoulders
import com.xflix.app.ui.theme.DmSerif
import com.xflix.app.ui.theme.InstrumentSans

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
    val style = when (title.style) {
        TitleStyle.Condensed -> TextStyle(
            fontFamily = BigShoulders, fontWeight = FontWeight.Black,
            fontSize = (23 * k).sp, lineHeight = (21 * k).sp, letterSpacing = (0.4 * k).sp,
        )
        TitleStyle.Serif -> TextStyle(
            fontFamily = DmSerif, fontSize = (20 * k).sp, lineHeight = (20 * k).sp, letterSpacing = (-0.2 * k).sp,
        )
        TitleStyle.SerifItalic -> TextStyle(
            fontFamily = DmSerif, fontStyle = FontStyle.Italic,
            fontSize = (21 * k).sp, lineHeight = (21 * k).sp,
        )
        TitleStyle.Wide -> TextStyle(
            fontFamily = InstrumentSans, fontWeight = FontWeight.SemiBold,
            fontSize = (10.5 * k).sp, lineHeight = (14 * k).sp, letterSpacing = (3.2 * k).sp,
        )
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
    )
}
