package com.xflix.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.xflix.app.data.Platform
import com.xflix.app.data.Profile
import com.xflix.app.ui.theme.Inter
import com.xflix.app.ui.theme.XColors

/** Ikon platform: logo atau teks */
@Composable
fun ProfileAvatar(profile: Profile, modifier: Modifier = Modifier, corner: Dp = 6.dp) {
    val shape = RoundedCornerShape(corner)
    val youtube = profile.platform == Platform.XflixYT
    BoxWithConstraints(
        modifier
            .clip(shape)
            .background(profile.color)
            // Kotak hitam tetap terlihat
            .then(if (profile.color == Color.Black) Modifier.border(1.dp, Color.White.copy(alpha = 0.18f), shape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        val density = LocalDensity.current
        val width = maxWidth
        fun size(fraction: Float) = with(density) { (width * fraction).toSp() }
        when {
            profile.logo != 0 -> Image(
                painterResource(profile.logo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            !youtube -> Text(
                "N",
                style = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Black, fontSize = size(0.62f), lineHeight = size(0.62f), color = Color.White),
            )
            width < 48.dp -> XMark(width * 0.56f)
            else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                XflixWordmark(height = width * 0.2f)
                Spacer(Modifier.height(width * 0.05f))
                Text(
                    "YT",
                    style = TextStyle(
                        fontFamily = Inter,
                        fontWeight = FontWeight.Black,
                        fontSize = size(0.15f),
                        lineHeight = size(0.15f),
                        letterSpacing = 0.12.em,
                        color = XColors.Red,
                    ),
                )
            }
        }
    }
}
