package com.xflix.app.ui.player

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.View
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.xflix.app.R
import com.xflix.app.data.Catalog
import com.xflix.app.data.Episode
import com.xflix.app.data.Kind
import com.xflix.app.data.NovaApi
import com.xflix.app.data.Platform
import com.xflix.app.data.ShortDrama
import com.xflix.app.data.Title
import com.xflix.app.ui.components.LocalToaster
import com.xflix.app.ui.components.XImage
import com.xflix.app.ui.icons.XIcons
import com.xflix.app.ui.theme.Motion
import com.xflix.app.ui.theme.Space
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

private val Speeds = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f)
private const val SeekSeconds = 10
// Area sentuh saat kontrol sumber
private val TopZone = 76.dp

private enum class PlayerPanel { Episodes, Servers }

/** Pemutar streaming Xflix */
@Composable
fun PlayerScreen(id: String, startSeason: Int, startEpisode: Int, onBack: () -> Unit) {
    val title = Catalog.title(id)
    val isSeries = title.kind == Kind.Series
    val toaster = LocalToaster.current
    val audioMessage = stringResource(R.string.toast_audio)
    val directMessage = stringResource(R.string.player_direct_hint)
    val noSource = stringResource(R.string.player_no_source)
    val streamFailed = stringResource(R.string.player_stream_failed_body)

    var season by rememberSaveable(id) { mutableIntStateOf(startSeason.coerceIn(1, title.seasons.size.coerceAtLeast(1))) }
    var episode by rememberSaveable(id) { mutableIntStateOf(startEpisode.coerceAtLeast(1)) }
    val episodes = title.seasons.getOrNull(season - 1)?.episodes.orEmpty()
    var server by rememberSaveable(id) { mutableIntStateOf(0) }
    var sources by remember(id) { mutableStateOf<List<StreamSource>>(emptyList()) }
    var loading by remember(id) { mutableStateOf(true) }
    var error by remember(id) { mutableStateOf<String?>(null) }
    var retry by remember { mutableIntStateOf(0) }
    var resumeAt by remember { mutableFloatStateOf(0f) }

    var controls by remember { mutableStateOf(true) }
    var poke by remember { mutableIntStateOf(0) }
    var locked by rememberSaveable { mutableStateOf(false) }
    var lockHint by remember { mutableStateOf(false) }
    var speed by rememberSaveable { mutableIntStateOf(2) }
    var scrubbing by remember { mutableStateOf(false) }
    var scrubAt by remember { mutableFloatStateOf(0f) }
    var panel by remember { mutableStateOf<PlayerPanel?>(null) }
    var lastPanel by remember { mutableStateOf(PlayerPanel.Episodes) }
    var panelSeason by remember { mutableIntStateOf(season) }
    var flash by remember { mutableStateOf<SeekFlash?>(null) }

    ImmersivePlayer(portrait = title.vertical)

    // Muat server episode aktif
    LaunchedEffect(id, season, episode, retry) {
        loading = true
        error = null
        sources = emptyList()
        try {
            val loaded = loadSources(id, season, episode)
            server = server.coerceIn(0, (loaded.size - 1).coerceAtLeast(0))
            sources = loaded
            if (loaded.isEmpty()) error = noSource
        } catch (cause: CancellationException) {
            throw cause
        } catch (cause: Exception) {
            error = cause.message ?: streamFailed
        } finally {
            loading = false
        }
    }

    val source = sources.getOrNull(server)
    val youtubePlayback = title.remoteSource == "youtube" || source?.url?.startsWith("https://www.youtube.com/embed/") == true
    val state = remember(source, retry) { StreamState() }
    val passthrough = state.direct && !state.started
    val failed = error != null || state.failed
    val waiting = !failed && !state.started && !passthrough
    // YouTube: kontrol sendiri, tanpa timpaan
    val youtubeRail = youtubePlayback && passthrough && !failed
    val railShown = youtubeRail && (controls || panel != null)
    val seasonText = stringResource(R.string.season_label, season)
    val subtitle = episodes.getOrNull(episode - 1)?.let {
        val label = episodeLabel(title, it)
        if (title.seasons.size > 1) "$seasonText · $label" else label
    }
    val next = if (isSeries) nextEpisode(episodes, episode) else null
    val nextSeason = if (next == null && season < title.seasons.size) season + 1 else null
    val hasEpisodes = isSeries && (episodes.size > 1 || title.seasons.size > 1)
    val hasServers = sources.size > 1

    fun seek(forward: Boolean) {
        state.seekTo(state.position + if (forward) SeekSeconds else -SeekSeconds)
        flash = SeekFlash(forward, System.nanoTime())
        poke++
    }

    fun openPanel(target: PlayerPanel) {
        if (target == PlayerPanel.Episodes) panelSeason = season
        lastPanel = target
        panel = target
    }

    fun openEpisode(targetSeason: Int, number: Int) {
        panel = null
        controls = true
        if (targetSeason == season && number == episode) return
        resumeAt = 0f
        server = 0
        season = targetSeason
        episode = number
    }

    fun openServer(index: Int) {
        panel = null
        controls = true
        if (index == server) return
        // Lanjut di detik yang sama
        if (state.started) resumeAt = state.position
        server = index
    }

    // Kontrol hilang sendiri
    LaunchedEffect(controls, poke, state.playing, scrubbing, panel, passthrough, youtubeRail) {
        val idle = youtubeRail || (state.playing && !passthrough)
        if (controls && idle && !scrubbing && panel == null) {
            delay(3500)
            controls = false
        }
    }
    LaunchedEffect(state.ended) {
        if (state.ended) controls = true
    }
    LaunchedEffect(state, state.started, speed) {
        state.setSpeed(Speeds[speed])
    }
    LaunchedEffect(passthrough) {
        if (passthrough) {
            controls = true
            if (!youtubePlayback) toaster.show(directMessage)
        }
    }
    LaunchedEffect(failed) {
        if (failed) {
            locked = false
            controls = true
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
    // Jeda saat app ditinggal
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, state) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> state.pause()
                Lifecycle.Event.ON_RESUME -> if (!state.started) state.play()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    BackHandler(enabled = locked) { lockHint = true }
    BackHandler(enabled = panel != null && !locked) { panel = null }

    val dim by animateFloatAsState(
        when {
            panel != null -> 0.6f
            state.started && controls && !locked -> 0.5f
            else -> 0f
        },
        tween(Motion.Medium),
        label = "dim",
    )
    // Video bergeser saat rel tampil
    val railInset by animateDpAsState(if (railShown) RailWidth else 0.dp, tween(Motion.Medium), label = "rail")
    val quickActions: @Composable RowScope.() -> Unit = {
        if (hasServers) PlayerAction(XIcons.Settings, stringResource(R.string.player_server)) { openPanel(PlayerPanel.Servers) }
        if (hasEpisodes) PlayerAction(XIcons.Episodes, stringResource(R.string.tab_episodes)) { openPanel(PlayerPanel.Episodes) }
    }
    val barPadding = Modifier.padding(horizontal = Space.xl, vertical = Space.m)
    // Kontrol sumber: sentuhan tembus
    val fullTouch = !passthrough || panel != null || failed

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (source != null) {
            key(source.url, retry) {
                if (source.native) NativeStream(source.url, resumeAt, state, Modifier.fillMaxSize())
                else Box(
                    Modifier
                        .fillMaxSize()
                        .padding(start = railInset)
                        .onAnyTouch(youtubeRail) {
                            controls = true
                            poke++
                        }
                ) {
                    EmbedStream(source.url, resumeAt, state, Modifier.fillMaxSize())
                }
            }
        }
        AnimatedVisibility(
            visible = failed || waiting,
            enter = fadeIn(),
            exit = fadeOut(tween(Motion.Medium)),
        ) {
            Box(Modifier.fillMaxSize()) {
                XImage(title.backdrop, Modifier.fillMaxSize(), displayWidth = 900.dp, eager = true, placeholder = Color.Black, url = title.imageUrl)
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.65f))
                )
            }
        }
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = dim))
        )

        AnimatedVisibility(
            visible = railShown,
            modifier = Modifier.align(Alignment.CenterStart),
            enter = fadeIn(tween(Motion.Medium)) + slideInHorizontally(tween(Motion.Medium)) { -it / 2 },
            exit = fadeOut(tween(Motion.Medium)) + slideOutHorizontally(tween(Motion.Medium)) { -it / 2 },
        ) {
            PlayerRail(onBack) {
                if (hasEpisodes) RailAction(XIcons.Episodes, stringResource(R.string.tab_episodes)) { openPanel(PlayerPanel.Episodes) }
                if (hasServers) RailAction(XIcons.Settings, stringResource(R.string.player_server)) { openPanel(PlayerPanel.Servers) }
            }
        }

        // Satu ketukan: tampil/sembunyi kontrol
        if (!youtubeRail || panel != null) Box(
            (if (fullTouch) Modifier.fillMaxSize() else Modifier.fillMaxWidth().height(TopZone))
                .pointerInput(state, fullTouch) {
                    detectTapGestures(
                        onTap = {
                            when {
                                locked -> lockHint = true
                                panel != null -> panel = null
                                else -> {
                                    controls = !controls
                                    poke++
                                }
                            }
                        },
                        onDoubleTap = { offset ->
                            if (!locked && panel == null && state.started && !state.failed) seek(forward = offset.x > size.width / 2f)
                        },
                    )
                }
        ) {
            SeekIndicator(flash, SeekSeconds)
            when {
                failed || waiting -> PlayerTopBar(title.name, subtitle, onBack, barPadding, quickActions)
                passthrough -> AnimatedVisibility(
                    visible = controls && panel == null,
                    enter = fadeIn(tween(Motion.Short)),
                    exit = fadeOut(tween(Motion.Medium)),
                ) {
                    PlayerTopBar(title.name, subtitle, onBack, barPadding, quickActions)
                }
                else -> AnimatedVisibility(
                    visible = controls && !locked && panel == null,
                    enter = fadeIn(tween(Motion.Short)),
                    exit = fadeOut(tween(Motion.Medium)),
                ) {
                    Controls(
                        titleText = title.name,
                        subtitle = subtitle,
                        playing = state.playing,
                        buffering = state.buffering,
                        position = if (scrubbing) scrubAt else state.position,
                        total = state.duration,
                        buffered = state.buffered,
                        speedLabel = stringResource(R.string.player_speed, speedLabel(Speeds[speed])),
                        isSeries = hasEpisodes,
                        hasNext = next != null || nextSeason != null,
                        hasServers = hasServers,
                        onBack = onBack,
                        onTogglePlay = {
                            if (state.playing) {
                                state.pause()
                            } else {
                                if (state.ended) state.seekTo(0f)
                                state.play()
                            }
                            poke++
                        },
                        onSeek = ::seek,
                        onScrub = {
                            scrubAt = it * state.duration
                            if (!scrubbing) state.seekTo(scrubAt)
                            poke++
                        },
                        onScrubbing = {
                            scrubbing = it
                            if (!it) state.seekTo(scrubAt)
                        },
                        onSpeed = {
                            speed = (speed + 1) % Speeds.size
                            poke++
                        },
                        onLock = {
                            locked = true
                            controls = false
                            lockHint = true
                        },
                        onEpisodes = { openPanel(PlayerPanel.Episodes) },
                        onServers = { openPanel(PlayerPanel.Servers) },
                        onAudio = {
                            toaster.show(audioMessage)
                            poke++
                        },
                        onNext = {
                            if (next != null) openEpisode(season, next)
                            else if (nextSeason != null) openEpisode(nextSeason, 1)
                        },
                    )
                }
            }
        }

        when {
            failed -> PlayerNotice(
                heading = stringResource(R.string.player_failed_title),
                body = error ?: streamFailed,
                modifier = Modifier.align(Alignment.Center),
                action = stringResource(if (hasServers && error == null) R.string.player_next_server else R.string.player_retry),
                onAction = { if (hasServers && error == null) openServer((server + 1) % sources.size) else retry++ },
            )
            waiting -> PlayerLoading(
                caption = if (loading || source == null) stringResource(R.string.player_loading) else source.label,
                modifier = Modifier.align(Alignment.Center),
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
            UnlockPill(onUnlock = {
                locked = false
                lockHint = false
                controls = true
                poke++
            })
        }

        AnimatedVisibility(
            visible = panel != null,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(tween(Motion.Long, easing = Motion.EmphasizedDecelerate)) { it } + fadeIn(),
            exit = slideOutVertically(tween(Motion.Medium, easing = Motion.EmphasizedAccelerate)) { it } + fadeOut(),
        ) {
            when (lastPanel) {
                PlayerPanel.Episodes -> key(panelSeason) {
                    EpisodePanel(
                        seasons = title.seasons,
                        selectedSeason = panelSeason,
                        onSeasonSelect = { panelSeason = it },
                        episodes = title.seasons.getOrNull(panelSeason - 1)?.episodes.orEmpty(),
                        current = if (panelSeason == season) episode else 0,
                        label = { episodeLabel(title, it) },
                        onClose = { panel = null },
                        onSelect = { openEpisode(panelSeason, it) },
                    )
                }
                PlayerPanel.Servers -> ServerPanel(
                    servers = sources.map { it.label },
                    current = server,
                    onClose = { panel = null },
                    onSelect = ::openServer,
                )
            }
        }
    }
}

/** Pantau sentuhan tanpa menahannya */
private fun Modifier.onAnyTouch(enabled: Boolean, onTouch: () -> Unit): Modifier =
    if (!enabled) this
    else pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                if (event.changes.any { it.changedToDown() }) onTouch()
            }
        }
    }

private suspend fun loadSources(id: String, season: Int, episode: Int): List<StreamSource> {
    // PineDrama tak butuh detail
    val title = if (Catalog.title(id).platform == Platform.PineDrama) Catalog.title(id) else Catalog.ensureDetails(id)
    val episodeId = title.seasons.getOrNull(season - 1)?.episodes?.getOrNull(episode - 1)?.remoteId
    if (title.remoteSource == "youtube") {
        require(!episodeId.isNullOrBlank()) { "Episode YouTube belum tersedia." }
        return listOf(youtubeSource(episodeId))
    }
    if (title.vertical) {
        val current = requireNotNull(title.seasons.getOrNull(season - 1)?.episodes?.getOrNull(episode - 1)) { "Episode belum tersedia." }
        return ShortDrama.streams(title, current).map { (label, url) -> StreamSource(label, url, native = true) }
    }
    val base = Catalog.detailPath(title)
    val path = when {
        title.remoteSource == "film" && title.kind == Kind.Series -> {
            require(!episodeId.isNullOrBlank()) { "Episode belum tersedia." }
            "$base?episode=${Uri.encode(episodeId)}"
        }
        title.remoteSource == "film" || episodeId == null -> base
        else -> "$base/${Uri.encode(episodeId)}"
    }
    return NovaApi.get(path).streamSources()
}

/** Buang judul berulang di nama */
private fun episodeLabel(title: Title, episode: Episode): String =
    episode.name.removePrefix(title.name).trim(' ', '-', '–', ':', '|').ifBlank { episode.name }

private val EpisodeNumber = Regex("""(?i)\b(?:episode|eps?)\.?\s*(\d+)""")

/** Daftar episode bisa menurun */
private fun nextEpisode(episodes: List<Episode>, current: Int): Int? {
    fun numberOf(episode: Episode?) = episode?.let { EpisodeNumber.find(it.name)?.groupValues?.get(1)?.toIntOrNull() }
    val first = numberOf(episodes.firstOrNull())
    val last = numberOf(episodes.lastOrNull())
    val step = if (first != null && last != null && first > last) -1 else 1
    return (current + step).takeIf { it in 1..episodes.size }
}

private fun speedLabel(speed: Float): String =
    if (speed % 1f == 0f) "${speed.toInt()}x" else "${speed}x"

/** Layar penuh, tegak bila perlu */
@Composable
private fun ImmersivePlayer(portrait: Boolean) {
    val activity = LocalContext.current.findActivity()
    val view = LocalView.current
    DisposableEffect(activity, view, portrait) {
        if (activity == null) onDispose { }
        else {
            val window = activity.window
            val oldOrientation = activity.requestedOrientation
            val oldUiFlags = window.decorView.systemUiVisibility
            val controller = WindowCompat.getInsetsController(window, view)
            val oldBehavior = controller.systemBarsBehavior
            activity.requestedOrientation =
                if (portrait) ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            window.decorView.systemUiVisibility = oldUiFlags or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
            controller.hide(WindowInsetsCompat.Type.systemBars())
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            onDispose {
                window.decorView.systemUiVisibility = oldUiFlags
                controller.systemBarsBehavior = oldBehavior
                controller.show(WindowInsetsCompat.Type.systemBars())
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                activity.requestedOrientation = oldOrientation
            }
        }
    }
    // WebView kadang memunculkan bar
    LaunchedEffect(activity, view) {
        delay(250)
        if (activity != null) WindowCompat.getInsetsController(activity.window, view)
            .hide(WindowInsetsCompat.Type.systemBars())
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
