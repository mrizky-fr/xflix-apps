package com.xflix.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xflix.app.R
import com.xflix.app.data.Kind
import com.xflix.app.data.Title
import com.xflix.app.data.WatchProgress
import com.xflix.app.ui.icons.XIcons
import com.xflix.app.ui.motion.sharedTitle
import com.xflix.app.ui.theme.Inter
import com.xflix.app.ui.theme.Dimens
import com.xflix.app.ui.theme.Motion
import com.xflix.app.ui.theme.Space
import com.xflix.app.ui.theme.XColors
import com.xflix.app.ui.theme.XType

private val PosterShade = Brush.verticalGradient(0.45f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.88f))

@Composable
fun PosterCard(
    title: Title,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp? = Dimens.PosterWidth,
    aspect: Float = 2f / 3f,
    art: ArtSize = ArtSize.Poster,
    sharedKey: String? = null,
    badges: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val sized = if (width != null) modifier.width(width) else modifier.fillMaxWidth()
    Box(
        sized
            .aspectRatio(aspect)
            .animatedPressScale(pressed)
            .sharedTitle(sharedKey, Dimens.Radius)
            .clip(RoundedCornerShape(Dimens.Radius))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
    ) {
        XImage(title.poster, Modifier.fillMaxSize(), displayWidth = width ?: 140.dp, url = title.imageUrl)
        Box(Modifier.matchParentSize().background(PosterShade))
        if (badges && title.original) XMark(13.dp, Modifier.padding(6.dp))
        if (badges && title.top10 != null) Top10Badge(Modifier.align(Alignment.TopEnd))
        TitleArt(
            title = title,
            size = art,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 6.dp, end = 6.dp, bottom = if (badges && title.newEpisodes) 22.dp else 9.dp),
        )
        if (badges && title.newEpisodes) NewEpisodesTag(Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun Top10Badge(modifier: Modifier = Modifier) {
    Column(
        modifier
            .size(width = 17.dp, height = 23.dp)
            .background(XColors.Red, RoundedCornerShape(bottomStart = 2.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("TOP", style = TextStyle(fontFamily = Inter, fontSize = 6.sp, lineHeight = 6.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp, color = Color.White))
        Text("10", style = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Black, fontSize = 10.sp, lineHeight = 11.sp, letterSpacing = (-0.3).sp, color = Color.White))
    }
}

@Composable
private fun NewEpisodesTag(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.tag_new_episodes),
        style = XType.Caption.copy(fontSize = 9.5.sp, lineHeight = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White),
        modifier = modifier
            .background(XColors.Red, RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp),
    )
}

/** Angka outline di belakang poster */
@Composable
fun Top10Card(rank: Int, title: Title, onClick: () -> Unit, sharedKey: String?) {
    val posterHeight = Dimens.PosterWidth * 1.5f
    Box(Modifier.width(if (rank >= 10) 172.dp else 146.dp).height(posterHeight)) {
        val numeral = TextStyle(
            fontFamily = Inter,
            fontWeight = FontWeight.Black,
            fontSize = 128.sp,
            lineHeight = 110.sp,
            letterSpacing = (-8).sp,
        )
        val placement = Modifier
            .align(Alignment.BottomStart)
            .offset(x = (-2).dp, y = 12.dp)
        Text(rank.toString(), style = numeral.copy(color = Color.Black), modifier = placement)
        Text(
            rank.toString(),
            style = numeral.copy(color = Color(0xFF8A8A8A), drawStyle = Stroke(width = 5f)),
            modifier = placement,
        )
        PosterCard(title, onClick, Modifier.align(Alignment.BottomEnd), sharedKey = sharedKey, badges = false)
    }
}

@Composable
fun ProgressLine(fraction: Float, modifier: Modifier = Modifier, animate: Boolean = true) {
    val p = remember { Animatable(if (animate) 0f else fraction) }
    LaunchedEffect(fraction) { p.animateTo(fraction, tween(700, delayMillis = 150, easing = Motion.Emphasized)) }
    Box(modifier.height(2.5.dp).background(Color(0xFF4D4D4D))) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(p.value)
                .background(XColors.Red)
        )
    }
}

@Composable
fun ContinueCard(progress: WatchProgress, title: Title, onPlay: () -> Unit, onInfo: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Column(
        Modifier
            .width(Dimens.PosterWidth)
            .animatedPressScale(pressed)
            .clip(RoundedCornerShape(Dimens.Radius))
            .background(XColors.SurfaceRaised)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(0.8f)
                .clickable(interactionSource = interaction, indication = null, onClick = onPlay),
        ) {
            XImage(title.poster, Modifier.fillMaxSize(), displayWidth = Dimens.PosterWidth, url = title.imageUrl)
            Box(Modifier.matchParentSize().background(PosterShade))
            Box(
                Modifier
                    .align(Alignment.Center)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .border(1.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(XIcons.Play, contentDescription = stringResource(R.string.action_play), tint = Color.White, modifier = Modifier.size(16.dp))
            }
            episodeLabel(title, progress.season, progress.episode)?.let {
                Text(
                    it,
                    style = XType.Caption.copy(color = Color.White),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp),
                )
            }
        }
        ProgressLine(progress.fraction, Modifier.fillMaxWidth())
        Row(
            Modifier
                .fillMaxWidth()
                .height(32.dp)
                .clickable(onClick = onInfo)
                .padding(horizontal = Space.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(XIcons.Info, contentDescription = stringResource(R.string.action_info), tint = XColors.TextSecondary, modifier = Modifier.size(17.dp))
            Spacer(Modifier.weight(1f))
            Text(stringResource(R.string.minutes_left, progress.minutesLeft), style = XType.Caption)
        }
    }
}

/** Label M2:E3 atau E4 */
fun episodeLabel(title: Title, season: Int, episode: Int): String? = when {
    title.kind == Kind.Movie -> null
    title.seasons.size > 1 -> "M$season:E$episode"
    else -> "E$episode"
}

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier, onMore: (() -> Unit)? = null) {
    Row(
        modifier
            .fillMaxWidth()
            .then(if (onMore != null) Modifier.clickable(onClick = onMore) else Modifier)
            .padding(horizontal = Dimens.Gutter)
            .padding(bottom = Space.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = XType.Section, modifier = Modifier.weight(1f, fill = false))
        if (onMore != null) {
            Icon(XIcons.ChevronRight, contentDescription = null, tint = XColors.TextSecondary, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun PosterRow(
    titles: List<Title>,
    keyPrefix: String,
    onOpen: (Title, String) -> Unit,
    width: Dp = Dimens.PosterWidth,
    aspect: Float = 2f / 3f,
    art: ArtSize = ArtSize.Poster,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = Dimens.Gutter),
        horizontalArrangement = Arrangement.spacedBy(Dimens.PosterGap),
    ) {
        items(titles, key = { it.id }) { title ->
            val key = "$keyPrefix/${title.id}"
            PosterCard(title, { onOpen(title, key) }, width = width, aspect = aspect, art = art, sharedKey = key)
        }
    }
}

@Composable
fun RatingBadge(text: String) {
    Text(
        text,
        style = XType.Caption.copy(color = Color.White, fontWeight = FontWeight.SemiBold),
        modifier = Modifier
            .background(Color(0xFF4A4A4A), RoundedCornerShape(2.dp))
            .padding(horizontal = 5.dp, vertical = 1.dp),
    )
}

@Composable
fun HdBadge() {
    Text(
        "HD",
        style = XType.Caption.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = XColors.TextSecondary),
        modifier = Modifier
            .border(1.dp, XColors.TextMuted, RoundedCornerShape(2.dp))
            .padding(horizontal = 4.dp, vertical = 0.dp),
    )
}

@Composable
fun runtimeText(title: Title): String = when {
    title.kind == Kind.Movie && title.minutes >= 60 ->
        stringResource(R.string.runtime_hm, title.minutes / 60, title.minutes % 60)
    title.kind == Kind.Movie -> stringResource(R.string.runtime_m, title.minutes)
    title.seasons.size > 1 -> stringResource(R.string.seasons_count, title.seasons.size)
    else -> stringResource(R.string.episodes_count, title.seasons.firstOrNull()?.episodes?.size ?: 0)
}

@Composable
fun MoodLine(title: Title, modifier: Modifier = Modifier, color: Color = XColors.TextSecondary) {
    Text(
        title.moods.joinToString("  •  "),
        style = XType.Caption.copy(color = color),
        modifier = modifier,
        maxLines = 1,
    )
}
