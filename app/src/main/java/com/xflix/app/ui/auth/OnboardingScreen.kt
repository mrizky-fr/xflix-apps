package com.xflix.app.ui.auth

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xflix.app.R
import com.xflix.app.data.Catalog
import com.xflix.app.ui.components.DownloadRing
import com.xflix.app.ui.components.LocalToaster
import com.xflix.app.ui.components.ProfileAvatar
import com.xflix.app.ui.components.TextAction
import com.xflix.app.ui.components.XButton
import com.xflix.app.ui.components.XButtonStyle
import com.xflix.app.ui.components.XImage
import com.xflix.app.ui.components.XflixWordmark
import com.xflix.app.ui.theme.Dimens
import com.xflix.app.ui.theme.Motion
import com.xflix.app.ui.theme.Space
import com.xflix.app.ui.theme.XColors
import com.xflix.app.ui.theme.XType
import kotlin.math.absoluteValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val PageCount = 3

@Composable
fun OnboardingScreen(onSignIn: () -> Unit, onGetStarted: () -> Unit) {
    val toaster = LocalToaster.current
    val privacy = stringResource(R.string.toast_privacy)
    val pager = rememberPagerState { PageCount }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        HorizontalPager(pager, Modifier.fillMaxSize()) { page ->
            val offset = (pager.currentPage - page) + pager.currentPageOffsetFraction
            when (page) {
                0 -> OnboardPage(R.string.onb_1_title, R.string.onb_1_body, offset) { PosterWall() }
                1 -> OnboardPage(R.string.onb_2_title, R.string.onb_2_body, offset) { DownloadIllustration() }
                else -> OnboardPage(R.string.onb_3_title, R.string.onb_3_body, offset) {
                    ProfilesIllustration(active = pager.settledPage == 2)
                }
            }
        }
        Row(
            Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .height(52.dp)
                .padding(start = Dimens.Gutter + 2.dp, end = Space.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            XflixWordmark(height = 24.dp)
            Spacer(Modifier.weight(1f))
            TextAction(stringResource(R.string.action_privacy), { toaster.show(privacy) })
            TextAction(stringResource(R.string.action_sign_in_caps), onSignIn)
        }
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = Space.l, vertical = Space.l),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PagerDots(pager)
            Spacer(Modifier.height(Space.l))
            XButton(
                text = stringResource(R.string.action_get_started),
                onClick = onGetStarted,
                style = XButtonStyle.Red,
                height = 46.dp,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun OnboardPage(
    @StringRes headline: Int,
    @StringRes body: Int,
    offset: Float,
    illustration: @Composable () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        illustration()
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 28.dp, end = 28.dp, bottom = 128.dp)
                .graphicsLayer {
                    // Parallax teks lebih lambat
                    translationX = offset * size.width * 0.35f
                    alpha = 1f - offset.absoluteValue.coerceAtMost(1f)
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                stringResource(headline),
                style = XType.Headline.copy(fontSize = 25.sp, lineHeight = 31.sp),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(body),
                style = XType.Body.copy(fontSize = 15.sp, lineHeight = 21.sp, color = XColors.TextSecondary),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PagerDots(pager: PagerState) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(PageCount) { i ->
            val selected = pager.currentPage == i
            val width by animateDpAsState(if (selected) 18.dp else 6.dp, tween(Motion.Medium, easing = Motion.Emphasized), label = "dot-width")
            val color by animateColorAsState(if (selected) Color.White else Color(0xFF4D4D4D), tween(Motion.Medium), label = "dot-color")
            Box(
                Modifier
                    .height(6.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

/** Dinding poster miring bergulir */
@Composable
private fun PosterWall() {
    val drift by rememberInfiniteTransition(label = "wall").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(48_000, easing = LinearEasing)),
        label = "wall-drift",
    )
    // Poster plus gambar episode
    val posters = remember {
        Catalog.titles.flatMap { t -> listOf(t.poster) + t.seasons.flatMap { s -> s.episodes.map { it.still } } }
    }
    Box(
        Modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = -10f
                    scaleX = 1.5f
                    scaleY = 1.5f
                },
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(4) { col ->
                val ids = remember(col) { List(7) { i -> posters[(col * 3 + i) % posters.size] } }
                PosterColumn(
                    ids = ids,
                    progress = { (drift + col * 0.21f) % 1f },
                    reverse = col % 2 == 1,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        }
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.85f),
                        0.22f to Color.Black.copy(alpha = 0.35f),
                        0.5f to Color.Black.copy(alpha = 0.55f),
                        0.72f to Color.Black,
                    )
                )
        )
    }
}

@Composable
private fun PosterColumn(ids: List<Int>, progress: () -> Float, reverse: Boolean, modifier: Modifier) {
    var setHeight by remember { mutableFloatStateOf(0f) }
    Box(modifier.clipToBounds()) {
        Column(
            Modifier
                .wrapContentHeight(align = Alignment.Top, unbounded = true)
                .onSizeChanged { setHeight = it.height / 2f }
                .graphicsLayer {
                    val p = progress()
                    translationY = if (reverse) -setHeight * (1f - p) else -setHeight * p
                },
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(2) {
                ids.forEach { id ->
                    XImage(
                        id = id,
                        displayWidth = 110.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f / 3f)
                            .clip(RoundedCornerShape(4.dp)),
                    )
                }
            }
        }
    }
}

/** Ponsel melayang, cincin unduhan */
@Composable
private fun DownloadIllustration() {
    val loop = rememberInfiniteTransition(label = "download-loop")
    val t by loop.animateFloat(0f, 1f, infiniteRepeatable(tween(3600, easing = LinearEasing)), label = "download-t")
    val float by loop.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float",
    )
    val title = remember { Catalog.title("sekolah_normal") }
    val progress = ((t - 0.08f) / 0.54f).coerceIn(0f, 1f)
    val check = ((t - 0.66f) / 0.1f).coerceIn(0f, 1f)
    Box(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.72f),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .graphicsLayer { translationY = float.dp.toPx() }
                .width(172.dp)
                .aspectRatio(9f / 18.5f)
                .clip(RoundedCornerShape(24.dp))
                .background(XColors.Surface)
                .border(2.dp, Color(0xFF3A3A3A), RoundedCornerShape(24.dp))
                .padding(6.dp)
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp))
            ) {
                XImage(title.poster, Modifier.fillMaxSize(), displayWidth = 156.dp)
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(0.55f to Color.Transparent, 1f to Color.Black))
                )
                Row(
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(title.name, style = XType.Label)
                        Text(stringResource(R.string.onb_download_meta), style = XType.Caption)
                    }
                    DownloadRing(progress = progress, done = check, modifier = Modifier.size(30.dp))
                }
            }
        }
    }
}

/** Avatar muncul bergiliran */
@Composable
private fun ProfilesIllustration(active: Boolean) {
    val profiles = Catalog.profiles
    val scales = remember { profiles.map { Animatable(0f) } }
    LaunchedEffect(active) {
        if (!active) {
            scales.forEach { it.snapTo(0f) }
            return@LaunchedEffect
        }
        scales.forEachIndexed { i, anim ->
            launch {
                delay(90L * i)
                anim.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 380f))
            }
        }
    }
    Box(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.68f),
        contentAlignment = Alignment.Center,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            listOf(0..2, 3..4).forEach { range ->
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    range.forEach { i ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            ProfileAvatar(
                                profiles[i],
                                Modifier
                                    .size(72.dp)
                                    .graphicsLayer {
                                        scaleX = scales[i].value
                                        scaleY = scales[i].value
                                        alpha = scales[i].value.coerceIn(0f, 1f)
                                    },
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                profiles[i].name,
                                style = XType.Caption,
                                modifier = Modifier.graphicsLayer { alpha = scales[i].value.coerceIn(0f, 1f) },
                            )
                        }
                    }
                }
            }
        }
    }
}
