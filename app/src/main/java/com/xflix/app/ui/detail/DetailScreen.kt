package com.xflix.app.ui.detail

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xflix.app.R
import com.xflix.app.XflixViewModel
import com.xflix.app.data.Catalog
import com.xflix.app.data.Episode
import com.xflix.app.data.Kind
import com.xflix.app.data.Platform
import com.xflix.app.data.Title
import com.xflix.app.ui.components.ArtSize
import com.xflix.app.ui.components.DownloadRing
import com.xflix.app.ui.components.HdBadge
import com.xflix.app.ui.components.IconAction
import com.xflix.app.ui.components.LocalToaster
import com.xflix.app.ui.components.MoodLine
import com.xflix.app.ui.components.PickerOverlay
import com.xflix.app.ui.components.PosterCard
import com.xflix.app.ui.components.RatingBadge
import com.xflix.app.ui.components.ScrimIcon
import com.xflix.app.ui.components.TitleArt
import com.xflix.app.ui.components.Top10Badge
import com.xflix.app.ui.components.XButton
import com.xflix.app.ui.components.XButtonStyle
import com.xflix.app.ui.components.XImage
import com.xflix.app.ui.components.XMark
import com.xflix.app.ui.components.rememberOnline
import com.xflix.app.ui.components.runtimeText
import com.xflix.app.ui.home.ListIcon
import com.xflix.app.ui.icons.XIcons
import com.xflix.app.ui.motion.sharedTitle
import com.xflix.app.ui.theme.Dimens
import com.xflix.app.ui.theme.Motion
import com.xflix.app.ui.theme.Space
import com.xflix.app.ui.theme.XColors
import com.xflix.app.ui.theme.XType
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

private val Indonesian = Locale.forLanguageTag("id-ID")

@Composable
fun DetailScreen(
    id: String,
    sharedKey: String?,
    vm: XflixViewModel,
    onBack: () -> Unit,
    onPlay: (Title, Int, Int) -> Unit,
    onOpenTitle: (Title, String?) -> Unit,
) {
    val title = Catalog.title(id)
    val similar = remember(id) { Catalog.similar(title) }
    LaunchedEffect(id) { runCatching { Catalog.ensureDetails(id) } }
    val isSeries = title.kind == Kind.Series
    val toaster = LocalToaster.current
    val context = LocalContext.current
    val shareText = stringResource(R.string.share_text, title.name)
    val castMessage = stringResource(R.string.toast_cast)

    var season by rememberSaveable { mutableIntStateOf(1) }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var seasonPicker by rememberSaveable { mutableStateOf(false) }
    var muted by rememberSaveable { mutableStateOf(true) }
    val listState = rememberLazyListState()
    val tabs = buildList {
        if (isSeries) add(stringResource(R.string.tab_episodes))
        if (similar.isNotEmpty()) add(stringResource(R.string.tab_similar))
    }
    val showEpisodes = isSeries && tab == 0
    val episodes = title.seasons.getOrNull(season - 1)?.episodes.orEmpty()
    val barFill by remember {
        derivedStateOf { if (listState.firstVisibleItemIndex > 0) 1f else (listState.firstVisibleItemScrollOffset / 400f).coerceIn(0f, 1f) }
    }
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = bottomInset + Space.xl),
        ) {
            item(key = "hero") {
                DetailHero(title, sharedKey, muted) { muted = !muted }
            }
            item(key = "info") {
                DetailInfo(
                    title = title,
                    vm = vm,
                    onPlay = { onPlay(title, 1, 1) },
                    onShare = {
                        val send = Intent(Intent.ACTION_SEND)
                            .setType("text/plain")
                            .putExtra(Intent.EXTRA_TEXT, shareText)
                        context.startActivity(Intent.createChooser(send, null))
                    },
                )
            }
            if (tabs.isNotEmpty()) {
                item(key = "tabs") {
                    DetailTabs(tabs, tab) { tab = it }
                }
            }
            if (showEpisodes) {
                if (title.seasons.size > 1) {
                    item(key = "season") {
                        SeasonButton(stringResource(R.string.season_label, season)) { seasonPicker = true }
                    }
                }
                items(episodes, key = { "s$season-e${it.number}" }) { episode ->
                    EpisodeRow(
                        episode = episode,
                        downloadKey = "${title.id}/s$season/e${episode.number}",
                        vm = vm,
                        onPlay = { onPlay(title, season, episode.number) },
                        modifier = Modifier.animateItem(),
                    )
                }
            } else {
                items(similar.chunked(3), key = { row -> "similar-" + row.joinToString { it.id } }) { row ->
                    Row(
                        Modifier
                            .animateItem()
                            .padding(horizontal = Dimens.Gutter)
                            .padding(top = Dimens.PosterGap),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.PosterGap),
                    ) {
                        row.forEach { t ->
                            val key = "similar/${t.id}"
                            PosterCard(t, { onOpenTitle(t, key) }, Modifier.weight(1f), width = null, sharedKey = key)
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }

        // Bar atas mengeras saat digulir
        Row(
            Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.92f * barFill))
                .statusBarsPadding()
                .height(52.dp)
                .padding(horizontal = Space.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ScrimIcon(XIcons.Back, stringResource(R.string.action_back), onBack)
            Spacer(Modifier.width(Space.m))
            Text(
                title.name,
                style = XType.Label,
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer { alpha = barFill },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            ScrimIcon(XIcons.Cast, stringResource(R.string.action_cast), { toaster.show(castMessage) })
        }

        PickerOverlay(
            visible = seasonPicker,
            options = title.seasons.map { stringResource(R.string.season_label, it.number) },
            selected = season - 1,
            onPick = {
                season = it + 1
                seasonPicker = false
            },
            onDismiss = { seasonPicker = false },
        )
    }
}

@Composable
private fun DetailHero(title: Title, sharedKey: String?, muted: Boolean, onToggleMute: () -> Unit) {
    // Zoom lambat, seolah trailer berjalan
    val drift by rememberInfiniteTransition(label = "hero-drift").animateFloat(
        initialValue = 1f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(tween(14_000, easing = LinearEasing), RepeatMode.Reverse),
        label = "hero-scale",
    )
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .sharedTitle(sharedKey, 0.dp)
            .clip(RoundedCornerShape(0.dp))
    ) {
        XImage(
            id = title.backdrop,
            url = title.imageUrl,
            displayWidth = 420.dp,
            eager = true,
            placeholder = Color.Black,
            // Cover tegak: wajah di atas
            alignment = if (title.vertical) Alignment.TopCenter else Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = drift
                    scaleY = drift
                },
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(0f to Color.Black.copy(alpha = 0.45f), 0.25f to Color.Transparent, 0.75f to Color.Transparent, 1f to Color.Black))
        )
        Row(
            Modifier
                .align(Alignment.BottomStart)
                .padding(Space.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.label_preview),
                style = XType.Caption.copy(color = Color.White, fontWeight = FontWeight.SemiBold),
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
        ScrimIcon(
            icon = if (muted) XIcons.VolumeOff else XIcons.VolumeOn,
            contentDescription = stringResource(R.string.action_mute),
            onClick = onToggleMute,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Space.m),
            size = 32.dp,
        )
    }
}

@Composable
private fun DetailInfo(title: Title, vm: XflixViewModel, onPlay: () -> Unit, onShare: () -> Unit) {
    val isSeries = title.kind == Kind.Series
    var expanded by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.padding(horizontal = Dimens.Gutter)) {
        Spacer(Modifier.height(Space.xs))
        if (title.original) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                XMark(12.dp)
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(if (isSeries) R.string.kind_series_caps else R.string.kind_movie_caps),
                    style = XType.Overline.copy(letterSpacing = 3.sp, color = XColors.TextSecondary),
                )
            }
            Spacer(Modifier.height(6.dp))
        }
        TitleArt(title, ArtSize.Detail, align = TextAlign.Start)
        Spacer(Modifier.height(Space.s))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.s)) {
            Text(title.year.toString(), style = XType.Caption.copy(fontSize = 12.5.sp))
            RatingBadge(title.rating)
            Text(runtimeText(title), style = XType.Caption.copy(fontSize = 12.5.sp))
            HdBadge()
        }
        title.top10?.let { rank ->
            Spacer(Modifier.height(Space.s))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Top10Badge(Modifier.clip(RoundedCornerShape(2.dp)))
                Spacer(Modifier.width(Space.s))
                Text(
                    stringResource(if (isSeries) R.string.top10_rank_series else R.string.top10_rank_movies, rank),
                    style = XType.Label.copy(fontSize = 14.sp),
                )
            }
        }
        if (title.streaming) {
            val online = rememberOnline()
            Spacer(Modifier.height(Space.s))
            Text(
                stringResource(when {
                    !online -> R.string.detail_streaming_offline
                    else -> when (title.platform) {
                        Platform.Novaplay -> R.string.detail_streaming
                        Platform.XflixYT -> R.string.detail_streaming_youtube
                        Platform.PineDrama -> R.string.detail_streaming_pinedrama
                        Platform.Melolo -> R.string.detail_streaming_melolo
                    }
                }),
                style = XType.Caption.copy(fontSize = 12.5.sp, color = if (online) XColors.TextSecondary else XColors.Warning),
            )
        }
        Spacer(Modifier.height(Space.m))

        if (title.isUpcoming) {
            val reminded = title.id in vm.reminders
            title.premiere?.let { date ->
                Text(
                    stringResource(R.string.premieres_on, date.format(DateTimeFormatter.ofPattern("d MMMM", Indonesian))),
                    style = XType.Label.copy(color = XColors.TextSecondary),
                )
                Spacer(Modifier.height(Space.s))
            }
            XButton(
                text = stringResource(if (reminded) R.string.action_reminded else R.string.action_remind_me),
                onClick = { vm.toggleReminder(title.id) },
                icon = if (reminded) XIcons.Check else XIcons.Bell,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            XButton(
                text = stringResource(R.string.action_play),
                onClick = onPlay,
                icon = XIcons.Play,
                modifier = Modifier.fillMaxWidth(),
            )
            if (!title.streaming) {
                Spacer(Modifier.height(Space.s))
                DownloadButton(title, vm)
            }
        }

        Spacer(Modifier.height(Space.m))
        Text(
            title.synopsis,
            style = XType.Body,
            maxLines = if (expanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .animateContentSize(tween(Motion.Medium, easing = Motion.Emphasized))
                .clickable { expanded = !expanded },
        )
        Spacer(Modifier.height(Space.s))
        if (title.cast.isNotEmpty()) {
            Text(
                stringResource(R.string.label_cast, title.cast.joinToString(", ")),
                style = XType.Caption.copy(fontSize = 12.sp, lineHeight = 16.sp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            stringResource(if (isSeries) R.string.label_creator else R.string.label_director, title.creator),
            style = XType.Caption.copy(fontSize = 12.sp, lineHeight = 16.sp),
        )
        Spacer(Modifier.height(Space.xs))
        MoodLine(title)
        Spacer(Modifier.height(Space.m))
        DetailActions(title, vm, onShare)
    }
}

@Composable
private fun DownloadButton(title: Title, vm: XflixViewModel) {
    val key = if (title.kind == Kind.Series) "${title.id}/s1/e1" else title.id
    val progress = vm.downloads[key]
    val label = when {
        progress == null -> stringResource(if (title.kind == Kind.Series) R.string.action_download_episode else R.string.action_download)
        progress < 1f -> stringResource(R.string.downloading_percent, (progress * 100).toInt())
        else -> stringResource(R.string.downloaded)
    }
    XButton(
        text = label,
        onClick = { if (progress == null) vm.startDownload(key) },
        style = XButtonStyle.Gray,
        modifier = Modifier.fillMaxWidth(),
        leading = {
            AnimatedContent(
                targetState = progress == null,
                transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(120)) },
                label = "download-icon",
            ) { idle ->
                if (idle) {
                    Icon(XIcons.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    val p = progress ?: 0f
                    DownloadRing(progress = p, done = if (p >= 1f) 1f else 0f, modifier = Modifier.size(20.dp))
                }
            }
        },
    )
}

@Composable
private fun DetailActions(title: Title, vm: XflixViewModel, onShare: () -> Unit) {
    val inList = title.id in vm.myList
    val liked = title.id in vm.liked
    val thumb = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    Row(horizontalArrangement = Arrangement.spacedBy(Space.l)) {
        Column(
            Modifier
                .clickable { vm.toggleMyList(title.id) }
                .padding(vertical = Space.xs),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) { ListIcon(inList, 24.dp) }
            Text(stringResource(R.string.action_my_list), style = XType.Caption)
        }
        IconAction(
            icon = XIcons.ThumbUp,
            label = stringResource(if (liked) R.string.action_rated else R.string.action_rate),
            tint = if (liked) XColors.Positive else Color.White,
            onClick = {
                vm.toggleLike(title.id)
                scope.launch {
                    thumb.snapTo(0f)
                    thumb.animateTo(0f, keyframes {
                        durationMillis = 420
                        -18f at 90
                        10f at 200
                        -4f at 300
                    })
                }
            },
            iconModifier = Modifier.graphicsLayer { rotationZ = thumb.value },
        )
        IconAction(icon = XIcons.Share, label = stringResource(R.string.action_share), onClick = onShare)
    }
}

/** Tab dengan indikator meluncur */
@Composable
private fun DetailTabs(tabs: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val density = LocalDensity.current
    val positions = remember { mutableStateMapOf<Int, Pair<Dp, Dp>>() }
    val target = positions[selected]
    val indicatorX by animateDpAsState(target?.first ?: 0.dp, tween(Motion.Medium, easing = Motion.Emphasized), label = "tab-x")
    val indicatorW by animateDpAsState(target?.second ?: 0.dp, tween(Motion.Medium, easing = Motion.Emphasized), label = "tab-w")
    Column(Modifier.padding(top = Space.l)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(XColors.Divider)
        ) {
            Box(
                Modifier
                    .offset { IntOffset((Dimens.Gutter + indicatorX).roundToPx(), (-1).dp.roundToPx()) }
                    .width(indicatorW)
                    .height(4.dp)
                    .background(XColors.Red)
            )
        }
        Row(Modifier.padding(horizontal = Dimens.Gutter), horizontalArrangement = Arrangement.spacedBy(Space.xl)) {
            tabs.forEachIndexed { i, label ->
                Text(
                    label,
                    style = XType.Label.copy(
                        fontSize = 14.sp,
                        color = if (i == selected) Color.White else XColors.TextSecondary,
                        fontWeight = if (i == selected) FontWeight.Bold else FontWeight.Medium,
                    ),
                    modifier = Modifier
                        .onPlaced { coords ->
                            val x = with(density) { coords.positionInParent().x.toDp() }
                            val w = with(density) { coords.size.width.toDp() }
                            positions[i] = x to w
                        }
                        .clickable { onSelect(i) }
                        .padding(vertical = Space.m),
                )
            }
        }
    }
}

@Composable
private fun SeasonButton(label: String, onClick: () -> Unit) {
    Row(
        Modifier
            .padding(horizontal = Dimens.Gutter, vertical = Space.s)
            .clip(RoundedCornerShape(Dimens.Radius))
            .background(XColors.SurfaceHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = Space.m, vertical = Space.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = XType.Label)
        Spacer(Modifier.width(6.dp))
        Icon(XIcons.ChevronDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun EpisodeRow(
    episode: Episode,
    downloadKey: String,
    vm: XflixViewModel,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress = vm.downloads[downloadKey]
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.Gutter, vertical = Space.s)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .width(124.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(Dimens.Radius))
                    .clickable(onClick = onPlay),
                contentAlignment = Alignment.Center,
            ) {
                XImage(episode.still, Modifier.fillMaxSize(), displayWidth = 124.dp, url = episode.imageUrl)
                Box(
                    Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(XIcons.Play, contentDescription = stringResource(R.string.action_play), tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(Modifier.width(Space.m))
            Column(Modifier.weight(1f)) {
                Text(
                    "${episode.number}. ${episode.name}",
                    style = XType.Label.copy(fontSize = 14.sp),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                if (episode.minutes > 0) {
                    Spacer(Modifier.height(2.dp))
                    Text(stringResource(R.string.runtime_m, episode.minutes), style = XType.Caption)
                }
            }
        }
        if (episode.synopsis.isNotBlank()) {
            Spacer(Modifier.height(Space.s))
            Text(episode.synopsis, style = XType.BodySmall, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
    }
}
