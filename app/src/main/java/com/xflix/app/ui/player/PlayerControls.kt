package com.xflix.app.ui.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xflix.app.R
import com.xflix.app.data.Episode
import com.xflix.app.data.Season
import com.xflix.app.ui.components.BarIcon
import com.xflix.app.ui.components.XButton
import com.xflix.app.ui.components.XImage
import com.xflix.app.ui.icons.XIcons
import com.xflix.app.ui.theme.Motion
import com.xflix.app.ui.theme.Space
import com.xflix.app.ui.theme.XColors
import com.xflix.app.ui.theme.XType
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

internal data class SeekFlash(val forward: Boolean, val id: Long)

@Composable
internal fun PlayerTopBar(
    titleText: String,
    subtitle: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        BarIcon(XIcons.Back, stringResource(R.string.action_back), onBack)
        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = Space.m),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(titleText, style = XType.Label.copy(fontSize = 14.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            subtitle?.let { Text(it, style = XType.Caption, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        }
        // Lebar seimbang dengan Back
        Row(Modifier.widthIn(min = 40.dp), horizontalArrangement = Arrangement.End, content = actions)
    }
}

@Composable
internal fun Controls(
    titleText: String,
    subtitle: String?,
    playing: Boolean,
    buffering: Boolean,
    position: Float,
    total: Float,
    buffered: Float,
    speedLabel: String,
    isSeries: Boolean,
    hasNext: Boolean,
    hasServers: Boolean,
    onBack: () -> Unit,
    onTogglePlay: () -> Unit,
    onSeek: (Boolean) -> Unit,
    onScrub: (Float) -> Unit,
    onScrubbing: (Boolean) -> Unit,
    onSpeed: () -> Unit,
    onLock: () -> Unit,
    onEpisodes: () -> Unit,
    onServers: () -> Unit,
    onAudio: () -> Unit,
    onNext: () -> Unit,
) {
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .padding(horizontal = Space.xl, vertical = Space.m)
    ) {
        // Layar tegak: aksi bertumpuk
        val compact = maxWidth < 600.dp
        PlayerTopBar(titleText, subtitle, onBack)

        Row(
            Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SeekButton(XIcons.Rewind, stringResource(R.string.player_rewind)) { onSeek(false) }
            Box(
                Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onTogglePlay),
                contentAlignment = Alignment.Center,
            ) {
                if (playing && buffering) {
                    CircularProgressIndicator(color = XColors.Red, strokeWidth = 3.dp, modifier = Modifier.size(46.dp))
                } else {
                    AnimatedContent(
                        targetState = playing,
                        transitionSpec = {
                            (scaleIn(tween(200), initialScale = 0.6f) + fadeIn(tween(150))) togetherWith
                                (scaleOut(tween(150), targetScale = 0.6f) + fadeOut(tween(100)))
                        },
                        label = "play-pause",
                    ) { isPlaying ->
                        Icon(
                            if (isPlaying) XIcons.Pause else XIcons.Play,
                            contentDescription = stringResource(if (isPlaying) R.string.action_pause else R.string.action_play),
                            tint = Color.White,
                            modifier = Modifier.size(48.dp),
                        )
                    }
                }
            }
            SeekButton(XIcons.Forward, stringResource(R.string.player_forward)) { onSeek(true) }
        }

        Column(Modifier.align(Alignment.BottomCenter)) {
            // Live atau belum tahu durasi
            if (total > 0f) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Scrubber(
                        fraction = position / total,
                        buffered = buffered,
                        onScrub = onScrub,
                        onScrubbing = onScrubbing,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(Space.m))
                    Text("-" + formatTime(total - position), style = XType.Caption.copy(color = Color.White))
                }
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                val item = if (compact) Modifier.weight(1f) else Modifier
                PlayerAction(XIcons.Speed, speedLabel, item, compact, onSpeed)
                PlayerAction(XIcons.Lock, stringResource(R.string.player_lock), item, compact, onLock)
                if (isSeries) PlayerAction(XIcons.Episodes, stringResource(R.string.tab_episodes), item, compact, onEpisodes)
                if (hasServers) PlayerAction(XIcons.Settings, stringResource(R.string.player_server), item, compact, onServers)
                PlayerAction(XIcons.Subtitles, stringResource(R.string.player_audio), item, compact, onAudio)
                if (hasNext) PlayerAction(XIcons.NextEpisode, stringResource(R.string.player_next), item, compact, onNext)
            }
        }
    }
}

@Composable
private fun SeekButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    val spin = remember { Animatable(0f) }
    var taps by remember { mutableIntStateOf(0) }
    LaunchedEffect(taps) {
        if (taps == 0) return@LaunchedEffect
        spin.snapTo(0f)
        spin.animateTo(1f, tween(320, easing = Motion.Emphasized))
    }
    val forward = icon == XIcons.Forward
    Box(
        Modifier
            .size(52.dp)
            .clip(CircleShape)
            .clickable {
                taps++
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = description,
            tint = Color.White,
            modifier = Modifier
                .size(38.dp)
                .graphicsLayer { rotationZ = (if (forward) 30f else -30f) * sin(PI.toFloat() * spin.value) },
        )
        Text("10", style = XType.Caption.copy(color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold), modifier = Modifier.offset(y = 1.dp))
    }
}

/** Indikator ketuk dua kali */
@Composable
internal fun SeekIndicator(flash: SeekFlash?, seconds: Int) {
    var last by remember { mutableStateOf(SeekFlash(true, 0)) }
    if (flash != null) last = flash
    AnimatedVisibility(
        visible = flash != null,
        enter = fadeIn(tween(80)),
        exit = fadeOut(tween(260)),
    ) {
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .align(if (last.forward) Alignment.CenterEnd else Alignment.CenterStart)
                    .fillMaxHeight()
                    .fillMaxWidth(0.42f)
                    .clip(if (last.forward) RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50) else RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50))
                    .background(Color.White.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(if (last.forward) XIcons.Forward else XIcons.Rewind, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.height(Space.xs))
                    Text(stringResource(R.string.player_seconds, seconds), style = XType.Label)
                }
            }
        }
    }
}

@Composable
private fun Scrubber(
    fraction: Float,
    buffered: Float,
    onScrub: (Float) -> Unit,
    onScrubbing: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrub by rememberUpdatedState(onScrub)
    val scrubbingChanged by rememberUpdatedState(onScrubbing)
    var width by remember { mutableIntStateOf(1) }
    var dragging by remember { mutableStateOf(false) }
    val thumb by animateDpAsState(if (dragging) 18.dp else 13.dp, tween(Motion.Short), label = "thumb")
    val progress = fraction.coerceIn(0f, 1f)
    Box(
        modifier
            .height(32.dp)
            .onSizeChanged { width = it.width.coerceAtLeast(1) }
            .pointerInput(Unit) {
                detectTapGestures { scrub((it.x / width).coerceIn(0f, 1f)) }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        dragging = true
                        scrubbingChanged(true)
                        scrub((it.x / width).coerceIn(0f, 1f))
                    },
                    onDragEnd = {
                        dragging = false
                        scrubbingChanged(false)
                    },
                    onDragCancel = {
                        dragging = false
                        scrubbingChanged(false)
                    },
                ) { change, _ ->
                    change.consume()
                    scrub((change.position.x / width).coerceIn(0f, 1f))
                }
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(Color.White.copy(alpha = 0.25f))
        )
        Box(
            Modifier
                .fillMaxWidth(buffered)
                .height(3.dp)
                .background(Color.White.copy(alpha = 0.45f))
        )
        Box(
            Modifier
                .fillMaxWidth(progress)
                .height(3.dp)
                .background(XColors.Red)
        )
        Box(
            Modifier
                .offset { IntOffset((progress * width - thumb.toPx() / 2f).roundToInt(), 0) }
                .size(thumb)
                .background(XColors.Red, CircleShape)
        )
    }
}

@Composable
internal fun PlayerAction(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    stacked: Boolean = false,
    onClick: () -> Unit,
) {
    val shape = modifier
        .clip(RoundedCornerShape(4.dp))
        .clickable(onClick = onClick)
    if (stacked) {
        Column(shape.padding(vertical = Space.xs), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(Space.xs))
            Text(label, style = XType.Label.copy(fontSize = 10.sp, lineHeight = 12.sp), maxLines = 2, textAlign = TextAlign.Center)
        }
    } else {
        Row(shape.padding(horizontal = Space.s, vertical = Space.s), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, style = XType.Label.copy(fontSize = 12.sp), maxLines = 1, softWrap = false)
        }
    }
}

internal val RailWidth = 72.dp

/** Rel samping, tidak menimpa video */
@Composable
internal fun PlayerRail(onBack: () -> Unit, modifier: Modifier = Modifier, actions: @Composable ColumnScope.() -> Unit = {}) {
    Column(
        modifier
            .fillMaxHeight()
            .width(RailWidth)
            .padding(vertical = Space.m),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Space.l),
    ) {
        BarIcon(XIcons.Back, stringResource(R.string.action_back), onBack)
        actions()
    }
}

@Composable
internal fun RailAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = Space.xs, vertical = Space.s),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(Space.xs))
        Text(label, style = XType.Caption.copy(color = Color.White, fontSize = 10.sp), maxLines = 1)
    }
}

/** Memuat sebelum video main */
@Composable
internal fun PlayerLoading(caption: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(color = XColors.Red, strokeWidth = 3.dp, modifier = Modifier.size(42.dp))
        Spacer(Modifier.height(Space.m))
        Text(caption, style = XType.Caption)
    }
}

@Composable
internal fun PlayerNotice(
    heading: String,
    body: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: () -> Unit = {},
) {
    Column(
        modifier.padding(horizontal = Space.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(heading, style = XType.Title)
        Spacer(Modifier.height(Space.s))
        Text(
            body,
            style = XType.Body.copy(color = XColors.TextSecondary),
            textAlign = TextAlign.Center,
        )
        if (action != null) {
            Spacer(Modifier.height(Space.l))
            XButton(action, onAction, Modifier.width(220.dp))
        }
    }
}

@Composable
internal fun UnlockPill(onUnlock: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.7f))
            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
            .clickable(onClick = onUnlock)
            .padding(horizontal = Space.l, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(XIcons.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(Space.s))
        Text(stringResource(R.string.player_unlock), style = XType.Label)
    }
}

/** Lembar bawah: episode atau server */
@Composable
private fun PlayerSheet(heading: String, onClose: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.88f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { }
            .padding(vertical = Space.m)
    ) {
        Row(Modifier.padding(horizontal = Space.xl), verticalAlignment = Alignment.CenterVertically) {
            Text(heading, style = XType.Section, modifier = Modifier.weight(1f))
            BarIcon(XIcons.Close, stringResource(R.string.action_close), onClose)
        }
        content()
    }
}

@Composable
internal fun EpisodePanel(
    seasons: List<Season>,
    selectedSeason: Int,
    onSeasonSelect: (Int) -> Unit,
    episodes: List<Episode>,
    current: Int,
    label: (Episode) -> String,
    onClose: () -> Unit,
    onSelect: (Int) -> Unit,
) {
    // Langsung ke episode aktif
    val list = rememberLazyListState(initialFirstVisibleItemIndex = (current - 1).coerceIn(0, (episodes.size - 1).coerceAtLeast(0)))
    PlayerSheet(stringResource(R.string.tab_episodes), onClose) {
        if (seasons.size > 1) {
            // Mulai dari musim aktif
            val seasonList = rememberLazyListState(initialFirstVisibleItemIndex = (seasons.indexOfFirst { it.number == selectedSeason }).coerceAtLeast(0))
            LazyRow(
                state = seasonList,
                contentPadding = PaddingValues(horizontal = Space.xl),
                horizontalArrangement = Arrangement.spacedBy(Space.s),
            ) {
                itemsIndexed(seasons) { _, season ->
                    val active = season.number == selectedSeason
                    Text(
                        stringResource(R.string.season_label, season.number),
                        style = XType.Label,
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(if (active) XColors.Red else XColors.SurfaceHigh)
                            .clickable { onSeasonSelect(season.number) }
                            .padding(horizontal = Space.m, vertical = Space.s),
                    )
                }
            }
            Spacer(Modifier.height(Space.m))
        }
        LazyRow(
            state = list,
            contentPadding = PaddingValues(horizontal = Space.xl),
            horizontalArrangement = Arrangement.spacedBy(Space.s),
        ) {
            itemsIndexed(episodes, key = { _, ep -> ep.remoteId ?: ep.number.toString() }) { _, ep ->
                val active = ep.number == current
                Column(
                    Modifier
                        .width(176.dp)
                        .clickable { onSelect(ep.number) }
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(4.dp))
                            .border(2.dp, if (active) XColors.Red else Color.Transparent, RoundedCornerShape(4.dp))
                    ) {
                        XImage(ep.still, Modifier.fillMaxSize(), displayWidth = 176.dp, url = ep.imageUrl)
                        if (active) {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.45f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(XIcons.Play, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(label(ep), style = XType.Label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (ep.minutes > 0) Text(stringResource(R.string.runtime_m, ep.minutes), style = XType.Caption)
                }
            }
        }
    }
}

@Composable
internal fun ServerPanel(
    servers: List<String>,
    current: Int,
    onClose: () -> Unit,
    onSelect: (Int) -> Unit,
) {
    PlayerSheet(stringResource(R.string.player_choose_server), onClose) {
        Spacer(Modifier.height(Space.s))
        LazyRow(
            contentPadding = PaddingValues(horizontal = Space.xl),
            horizontalArrangement = Arrangement.spacedBy(Space.s),
        ) {
            itemsIndexed(servers) { index, server ->
                val active = index == current
                Row(
                    Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (active) XColors.Red else XColors.SurfaceHigh)
                        .clickable { onSelect(index) }
                        .padding(horizontal = Space.l, vertical = Space.m),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (active) {
                        Icon(XIcons.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(server, style = XType.Label, maxLines = 1)
                }
            }
        }
        Spacer(Modifier.height(Space.s))
    }
}

internal fun formatTime(seconds: Float): String {
    val s = seconds.toInt().coerceAtLeast(0)
    val h = s / 3600
    val m = (s % 3600) / 60
    val sec = s % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, sec) else "%d:%02d".format(m, sec)
}
