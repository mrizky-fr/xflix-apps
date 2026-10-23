package com.xflix.app.ui.player

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.xflix.app.R
import com.xflix.app.data.Catalog
import com.xflix.app.data.Episode
import com.xflix.app.data.Kind
import com.xflix.app.ui.components.BarIcon
import com.xflix.app.ui.components.LocalToaster
import com.xflix.app.ui.components.XImage
import com.xflix.app.ui.icons.XIcons
import com.xflix.app.ui.theme.Motion
import com.xflix.app.ui.theme.Space
import com.xflix.app.ui.theme.XColors
import com.xflix.app.ui.theme.XType
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.delay

private val Speeds = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f)
private const val SeekSeconds = 10

private data class SeekFlash(val forward: Boolean, val id: Long)

/** Player tiruan tanpa video */
@Composable
fun PlayerScreen(
    id: String,
    startSeason: Int,
    startEpisode: Int,
    onBack: () -> Unit,
) {
    val title = remember(id) { Catalog.title(id) }
    // Ganti episode tanpa navigasi
    var season by rememberSaveable { mutableIntStateOf(startSeason) }
    var episode by rememberSaveable { mutableIntStateOf(startEpisode) }
    val isSeries = title.kind == Kind.Series
    val currentEpisode = title.seasons.getOrNull(season - 1)?.episodes?.getOrNull(episode - 1)
    val nextEpisode = title.seasons.getOrNull(season - 1)?.episodes?.getOrNull(episode)
    val total = ((currentEpisode?.minutes ?: title.minutes).coerceAtLeast(1) * 60).toFloat()
    val resumeAt = Catalog.continueWatching
        .firstOrNull { it.titleId == id && (!isSeries || (it.season == season && it.episode == episode)) }
        ?.fraction ?: 0f
    val toaster = LocalToaster.current
    val audioMessage = stringResource(R.string.toast_audio)

    ImmersiveLandscape()

    var playing by rememberSaveable { mutableStateOf(true) }
    var position by rememberSaveable(id, season, episode) { mutableFloatStateOf(total * resumeAt) }
    var controls by remember { mutableStateOf(true) }
    var poke by remember { mutableIntStateOf(0) }
    var locked by rememberSaveable { mutableStateOf(false) }
    var lockHint by remember { mutableStateOf(false) }
    var speed by rememberSaveable { mutableIntStateOf(2) }
    var scrubbing by remember { mutableStateOf(false) }
    var episodesOpen by remember { mutableStateOf(false) }
    var flash by remember { mutableStateOf<SeekFlash?>(null) }

    fun seek(forward: Boolean) {
        position = (position + if (forward) SeekSeconds else -SeekSeconds).coerceIn(0f, total)
        flash = SeekFlash(forward, System.nanoTime())
        poke++
    }

    // Jam putar tiruan
    LaunchedEffect(playing, speed, scrubbing) {
        while (playing && !scrubbing) {
            delay(250)
            position = (position + 0.25f * Speeds[speed]).coerceAtMost(total)
            if (position >= total) playing = false
        }
    }
    // Kontrol hilang sendiri
    LaunchedEffect(controls, poke, playing, scrubbing, episodesOpen) {
        if (controls && playing && !scrubbing && !episodesOpen) {
            delay(3500)
            controls = false
        }
    }
    LaunchedEffect(flash) {
        if (flash != null) {
            delay(650)
            flash = null
        }
    }
    LaunchedEffect(lockHint) {
        if (lockHint) {
            delay(2500)
            lockHint = false
        }
    }

    // Panning pelan selama diputar
    val pan = remember { Animatable(0f) }
    LaunchedEffect(playing) {
        while (playing) {
            pan.animateTo(if (pan.value < 0.5f) 1f else 0f, tween(9000, easing = LinearEasing))
        }
    }
    val dim by animateFloatAsState(if (controls || episodesOpen) 0.5f else 0f, tween(Motion.Medium), label = "dim")

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(locked) {
                detectTapGestures(
                    onTap = {
                        when {
                            locked -> lockHint = true
                            episodesOpen -> episodesOpen = false
                            else -> {
                                controls = !controls
                                poke++
                            }
                        }
                    },
                    onDoubleTap = { offset -> if (!locked) seek(forward = offset.x > size.width / 2f) },
                )
            }
    ) {
        XImage(
            id = title.backdrop,
            displayWidth = 900.dp,
            eager = true,
            placeholder = Color.Black,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = 1.08f
                    scaleY = 1.08f
                    translationX = (pan.value - 0.5f) * size.width * 0.05f
                },
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = dim))
        )

        SeekIndicator(flash)

        AnimatedVisibility(
            visible = controls && !locked && !episodesOpen,
            enter = fadeIn(tween(Motion.Short)),
            exit = fadeOut(tween(Motion.Medium)),
        ) {
            Controls(
                titleText = title.name,
                subtitle = currentEpisode?.let { stringResource(R.string.player_episode, season, it.number, it.name) },
                playing = playing,
                position = position,
                total = total,
                speedLabel = stringResource(R.string.player_speed, speedLabel(Speeds[speed])),
                isSeries = isSeries,
                hasNext = nextEpisode != null,
                onBack = onBack,
                onTogglePlay = {
                    if (position >= total) position = 0f
                    playing = !playing
                    poke++
                },
                onSeek = ::seek,
                onScrub = {
                    position = it * total
                    poke++
                },
                onScrubbing = { scrubbing = it },
                onSpeed = {
                    speed = (speed + 1) % Speeds.size
                    poke++
                },
                onLock = {
                    locked = true
                    controls = false
                    lockHint = true
                },
                onEpisodes = { episodesOpen = true },
                onAudio = { toaster.show(audioMessage) },
                onNext = { nextEpisode?.let { episode = it.number } },
            )
        }

        AnimatedVisibility(
            visible = locked && lockHint,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = Space.xl),
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut(),
        ) {
            Row(
                Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    .clickable {
                        locked = false
                        lockHint = false
                        controls = true
                    }
                    .padding(horizontal = Space.l, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(XIcons.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(Space.s))
                Text(stringResource(R.string.player_unlock), style = XType.Label)
            }
        }

        AnimatedVisibility(
            visible = episodesOpen,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(tween(Motion.Long, easing = Motion.EmphasizedDecelerate)) { it } + fadeIn(),
            exit = slideOutVertically(tween(Motion.Medium, easing = Motion.EmphasizedAccelerate)) { it } + fadeOut(),
        ) {
            EpisodePanel(
                seasonLabel = stringResource(R.string.season_label, season),
                episodes = title.seasons.getOrNull(season - 1)?.episodes.orEmpty(),
                current = episode,
                onClose = { episodesOpen = false },
                onSelect = { number ->
                    episodesOpen = false
                    episode = number
                },
            )
        }
    }
}

@Composable
private fun Controls(
    titleText: String,
    subtitle: String?,
    playing: Boolean,
    position: Float,
    total: Float,
    speedLabel: String,
    isSeries: Boolean,
    hasNext: Boolean,
    onBack: () -> Unit,
    onTogglePlay: () -> Unit,
    onSeek: (Boolean) -> Unit,
    onScrub: (Float) -> Unit,
    onScrubbing: (Boolean) -> Unit,
    onSpeed: () -> Unit,
    onLock: () -> Unit,
    onEpisodes: () -> Unit,
    onAudio: () -> Unit,
    onNext: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(horizontal = Space.xl, vertical = Space.m)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            BarIcon(XIcons.Back, stringResource(R.string.action_back), onBack)
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(titleText, style = XType.Label.copy(fontSize = 14.sp))
                subtitle?.let { Text(it, style = XType.Caption) }
            }
            Spacer(Modifier.width(40.dp))
        }

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
            SeekButton(XIcons.Forward, stringResource(R.string.player_forward)) { onSeek(true) }
        }

        Column(Modifier.align(Alignment.BottomCenter)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Scrubber(
                    fraction = position / total,
                    buffered = (position / total + 0.12f).coerceAtMost(1f),
                    onScrub = onScrub,
                    onScrubbing = onScrubbing,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(Space.m))
                Text("-" + formatTime(total - position), style = XType.Caption.copy(color = Color.White))
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                PlayerAction(XIcons.Speed, speedLabel, onSpeed)
                PlayerAction(XIcons.Lock, stringResource(R.string.player_lock), onLock)
                if (isSeries) PlayerAction(XIcons.Episodes, stringResource(R.string.tab_episodes), onEpisodes)
                PlayerAction(XIcons.Subtitles, stringResource(R.string.player_audio), onAudio)
                if (isSeries && hasNext) PlayerAction(XIcons.NextEpisode, stringResource(R.string.player_next), onNext)
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
private fun SeekIndicator(flash: SeekFlash?) {
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
                    Text(stringResource(R.string.player_seconds, SeekSeconds), style = XType.Label)
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
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(3.dp)
                .background(XColors.Red)
        )
        Box(
            Modifier
                .offset { IntOffset((fraction * width - thumb.toPx() / 2f).roundToInt(), 0) }
                .size(thumb)
                .background(XColors.Red, CircleShape)
        )
    }
}

@Composable
private fun PlayerAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        Modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = Space.s, vertical = Space.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, style = XType.Label.copy(fontSize = 12.sp))
    }
}

@Composable
private fun EpisodePanel(
    seasonLabel: String,
    episodes: List<Episode>,
    current: Int,
    onClose: () -> Unit,
    onSelect: (Int) -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.88f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { }
            .padding(vertical = Space.m)
    ) {
        Row(Modifier.padding(horizontal = Space.xl), verticalAlignment = Alignment.CenterVertically) {
            Text(seasonLabel, style = XType.Section, modifier = Modifier.weight(1f))
            BarIcon(XIcons.Close, stringResource(R.string.action_close), onClose)
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = Space.xl),
            horizontalArrangement = Arrangement.spacedBy(Space.s),
        ) {
            items(episodes, key = { it.number }) { ep ->
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
                        XImage(ep.still, Modifier.fillMaxSize(), displayWidth = 176.dp)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text("${ep.number}. ${ep.name}", style = XType.Label, maxLines = 1)
                    Text(stringResource(R.string.runtime_m, ep.minutes), style = XType.Caption)
                }
            }
        }
    }
}

private fun speedLabel(speed: Float): String =
    if (speed % 1f == 0f) "${speed.toInt()}x" else "${speed}x"

private fun formatTime(seconds: Float): String {
    val s = seconds.toInt().coerceAtLeast(0)
    val h = s / 3600
    val m = (s % 3600) / 60
    val sec = s % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, sec) else "%d:%02d".format(m, sec)
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/** Landscape dan imersif */
@Composable
private fun ImmersiveLandscape() {
    val context = LocalContext.current
    val view = LocalView.current
    DisposableEffect(Unit) {
        val activity = context.findActivity() ?: return@DisposableEffect onDispose { }
        val original = activity.requestedOrientation
        val controller = WindowCompat.getInsetsController(activity.window, view)
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            controller.show(WindowInsetsCompat.Type.systemBars())
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            activity.requestedOrientation = original
        }
    }
}
