package com.xflix.app.ui.player

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.net.Uri
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.xflix.app.data.Catalog
import com.xflix.app.data.Episode
import com.xflix.app.data.Kind
import com.xflix.app.data.NovaApi
import com.xflix.app.data.Title
import com.xflix.app.ui.components.XButton
import com.xflix.app.ui.components.XImage
import com.xflix.app.ui.icons.XIcons
import com.xflix.app.ui.theme.Space
import com.xflix.app.ui.theme.XColors
import com.xflix.app.ui.theme.XType
import kotlinx.coroutines.delay
import org.json.JSONObject

private data class StreamSource(val label: String, val url: String, val native: Boolean)
private enum class PlayerPanel { Episodes, Servers }

@Composable
fun PlayerScreen(id: String, startSeason: Int, startEpisode: Int, onBack: () -> Unit) {
    val title = Catalog.title(id)
    val episodes = title.seasons.firstOrNull()?.episodes.orEmpty()
    var episodeIndex by rememberSaveable(id) { mutableIntStateOf(startEpisode.coerceAtLeast(1)) }
    var serverIndex by rememberSaveable(id) { mutableIntStateOf(0) }
    var sources by remember(id) { mutableStateOf<List<StreamSource>>(emptyList()) }
    var loading by remember(id) { mutableStateOf(true) }
    var error by remember(id) { mutableStateOf<String?>(null) }
    var retry by remember(id) { mutableIntStateOf(0) }
    var started by rememberSaveable(id) { mutableStateOf(false) }
    var controlsVisible by rememberSaveable(id) { mutableStateOf(true) }
    var panel by remember { mutableStateOf<PlayerPanel?>(null) }

    ImmersiveLandscape()

    LaunchedEffect(id, episodeIndex, retry) {
        loading = true
        error = null
        sources = emptyList()
        serverIndex = 0
        try {
            val loaded = Catalog.ensureDetails(id)
            val episodeId = loaded.seasons.firstOrNull()?.episodes?.getOrNull(episodeIndex - 1)?.remoteId
            val path = if (loaded.remoteSource == "film") {
                val base = Catalog.detailPath(loaded)
                if (loaded.kind == Kind.Series) {
                    require(!episodeId.isNullOrBlank()) { "Episode belum tersedia." }
                    "$base?episode=${Uri.encode(episodeId)}"
                } else base
            } else {
                val base = Catalog.detailPath(loaded)
                if (episodeId == null) base else "$base/${Uri.encode(episodeId)}"
            }
            sources = NovaApi.get(path).sources()
            if (sources.isEmpty()) error = "Sumber pemutar belum tersedia untuk judul ini."
        } catch (cause: Exception) {
            error = cause.message ?: "Gagal memuat video."
        } finally {
            loading = false
        }
    }

    LaunchedEffect(started, controlsVisible, panel) {
        if (started && controlsVisible && panel == null && !loading && error == null) {
            delay(5_000)
            controlsVisible = false
        }
    }

    val source = sources.getOrNull(serverIndex)
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (started && !loading && error == null && source != null) {
            if (source.native) NativeStream(source.url, controlsVisible)
            else EmbedStream(source.url)
        } else {
            PlayerCover(title, loading, error, onPlay = {
                started = true
                controlsVisible = true
            }, onRetry = { retry++ })
        }

        AnimatedVisibility(
            visible = controlsVisible || !started || loading || error != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            PlayerToolbar(
                title = title.name,
                episode = episodes.getOrNull(episodeIndex - 1)?.name,
                server = source?.label,
                showEpisodes = episodes.size > 1,
                showServers = sources.size > 1,
                onBack = onBack,
                onEpisodes = { panel = PlayerPanel.Episodes; controlsVisible = true },
                onServers = { panel = PlayerPanel.Servers; controlsVisible = true },
            )
        }
        if (started && !controlsVisible && panel == null) {
            Box(
                Modifier.align(Alignment.TopStart).padding(12.dp).size(36.dp)
                    .clip(CircleShape).background(Color.Black.copy(alpha = 0.72f))
                    .clickable { controlsVisible = true },
                contentAlignment = Alignment.Center,
            ) {
                Text("X", style = XType.Title.copy(color = XColors.Red, fontSize = 20.sp))
            }
        }

        if (panel != null) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.58f)).clickable { panel = null })
            PlayerSelectionPanel(
                panel = panel ?: PlayerPanel.Episodes,
                episodes = episodes,
                sources = sources,
                episodeIndex = episodeIndex,
                serverIndex = serverIndex,
                onClose = { panel = null },
                onEpisode = { episodeIndex = it; panel = null; controlsVisible = true },
                onServer = { serverIndex = it; panel = null; controlsVisible = true },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun PlayerCover(title: Title, loading: Boolean, error: String?, onPlay: () -> Unit, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        XImage(0, Modifier.fillMaxSize(), displayWidth = 900.dp, url = title.imageUrl, placeholder = Color.Black)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
            0f to Color.Black.copy(alpha = 0.74f),
            0.5f to Color.Black.copy(alpha = 0.70f),
            1f to Color.Black.copy(alpha = 0.90f),
        )))
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 80.dp)) {
            Text("XFLIX", style = XType.Overline.copy(color = XColors.Red, fontSize = 14.sp))
            Spacer(Modifier.height(Space.s))
            Text(title.name, style = XType.Headline.copy(fontSize = 29.sp), maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(Space.s))
            Text(if (title.kind == Kind.Series) "Serial · Streaming" else "Film · Streaming", style = XType.Caption)
            Spacer(Modifier.height(Space.xl))
            when {
                loading -> {
                    CircularProgressIndicator(color = XColors.Red, modifier = Modifier.size(34.dp))
                    Spacer(Modifier.height(Space.s))
                    Text("Memuat pemutar…", style = XType.Caption)
                }
                error != null -> {
                    Text(error, style = XType.Body, color = Color.White)
                    Spacer(Modifier.height(Space.m))
                    XButton("Coba lagi", onRetry, Modifier.width(160.dp))
                }
                else -> XButton("Putar", onPlay, Modifier.width(190.dp), icon = XIcons.Play, height = 48.dp)
            }
        }
    }
}

@Composable
private fun PlayerToolbar(
    title: String,
    episode: String?,
    server: String?,
    showEpisodes: Boolean,
    showServers: Boolean,
    onBack: () -> Unit,
    onEpisodes: () -> Unit,
    onServers: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.86f), Color.Transparent)))
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RoundAction(XIcons.Back, "Kembali", onBack)
        Column(Modifier.weight(1f)) {
            Text(title, style = XType.Label.copy(fontSize = 15.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (episode != null) Text(episode, style = XType.Caption, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (showServers) ToolbarAction(XIcons.Settings, server ?: "Server", onServers)
        if (showEpisodes) ToolbarAction(XIcons.Episodes, "Episode", onEpisodes)
    }
}

@Composable
private fun RoundAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Box(
        Modifier.size(40.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.55f))
            .border(1.dp, Color.White.copy(alpha = 0.23f), CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, label, tint = Color.White, modifier = Modifier.size(21.dp))
    }
}

@Composable
private fun ToolbarAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        Modifier.clip(RoundedCornerShape(20.dp)).background(Color.Black.copy(alpha = 0.60f))
            .border(1.dp, Color.White.copy(alpha = 0.23f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(17.dp))
        Text(label, style = XType.Label, maxLines = 1)
    }
}

@Composable
private fun PlayerSelectionPanel(
    panel: PlayerPanel,
    episodes: List<Episode>,
    sources: List<StreamSource>,
    episodeIndex: Int,
    serverIndex: Int,
    onClose: () -> Unit,
    onEpisode: (Int) -> Unit,
    onServer: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxWidth().heightIn(max = 260.dp)
            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
            .background(Color(0xF5101010)).clickable { }
            .padding(vertical = 14.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = Space.xl), verticalAlignment = Alignment.CenterVertically) {
            Text(if (panel == PlayerPanel.Episodes) "Pilih Episode" else "Pilih Server", style = XType.Title, modifier = Modifier.weight(1f))
            RoundAction(XIcons.Close, "Tutup", onClose)
        }
        Spacer(Modifier.height(Space.m))
        if (panel == PlayerPanel.Episodes) {
            LazyRow(contentPadding = PaddingValues(horizontal = Space.xl), horizontalArrangement = Arrangement.spacedBy(Space.m)) {
                itemsIndexed(episodes, key = { _, ep -> ep.remoteId ?: ep.number.toString() }) { index, ep ->
                    val selected = index + 1 == episodeIndex
                    Column(Modifier.width(172.dp).clickable { onEpisode(index + 1) }) {
                        Box(
                            Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(5.dp))
                                .border(2.dp, if (selected) XColors.Red else Color.Transparent, RoundedCornerShape(5.dp)),
                        ) {
                            XImage(ep.still, Modifier.fillMaxSize(), displayWidth = 172.dp, url = ep.imageUrl)
                            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.18f)))
                            Text("${index + 1}", style = XType.Title, modifier = Modifier.align(Alignment.BottomStart).padding(8.dp))
                        }
                        Spacer(Modifier.height(5.dp))
                        Text(ep.name, style = XType.Label, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        } else {
            LazyRow(contentPadding = PaddingValues(horizontal = Space.xl), horizontalArrangement = Arrangement.spacedBy(Space.s)) {
                itemsIndexed(sources) { index, source ->
                    val selected = index == serverIndex
                    Text(
                        source.label,
                        style = XType.Label,
                        modifier = Modifier.clip(RoundedCornerShape(6.dp))
                            .background(if (selected) XColors.Red else XColors.SurfaceHigh)
                            .clickable { onServer(index) }
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                    )
                }
            }
        }
    }
}

private fun JSONObject.sources(): List<StreamSource> {
    val result = mutableListOf<StreamSource>()
    val players = optJSONArray("players")
    if (players != null) for (i in 0 until players.length()) {
        val player = players.optJSONObject(i) ?: continue
        val url = player.optString("url")
        if (url.startsWith("https://")) result += StreamSource(player.optString("label", "Server ${i + 1}"), url, false)
    }
    val videoId = optString("videoId")
    if (optString("format") == "youtube" && videoId.matches(Regex("[A-Za-z0-9_-]{11}"))) {
        result += StreamSource("YouTube", "https://www.youtube-nocookie.com/embed/$videoId?autoplay=1", false)
    }
    val media = optString("url")
    if (optString("format") in listOf("mp4", "hls") && media.isNotBlank()) {
        val full = if (media.startsWith("/api/")) NovaApi.BASE + media else media
        if (full.startsWith("https://")) result += StreamSource("Stream", full, true)
    }
    return result
}

@Composable
private fun EmbedStream(url: String) {
    val html = remember(url) {
        val safeUrl = TextUtils.htmlEncode(url)
        """<!doctype html><html><head><meta name="viewport" content="width=device-width, initial-scale=1"></head>
        <body style="margin:0;background:#000"><iframe src="$safeUrl" style="position:fixed;inset:0;width:100%;height:100%;border:0" allow="autoplay; fullscreen; picture-in-picture" allowfullscreen></iframe></body></html>"""
    }
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                setBackgroundColor(android.graphics.Color.BLACK)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.setSupportMultipleWindows(false)
                settings.javaScriptCanOpenWindowsAutomatically = false
                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                        request.isForMainFrame && request.url.host != "novaplay.asia"
                }
            }
        },
        update = {
            if (it.tag != html) {
                it.tag = html
                it.loadDataWithBaseURL(NovaApi.BASE, html, "text/html", "UTF-8", null)
            }
        },
        onRelease = { it.stopLoading(); it.destroy() },
    )
}

@Composable
private fun NativeStream(url: String, controlsVisible: Boolean) {
    val context = LocalContext.current
    val player = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = true
        }
    }
    var position by remember(player) { mutableLongStateOf(0L) }
    var duration by remember(player) { mutableLongStateOf(0L) }
    var playing by remember(player) { mutableStateOf(true) }
    DisposableEffect(player) { onDispose { player.release() } }
    LaunchedEffect(player) {
        while (true) {
            position = player.currentPosition.coerceAtLeast(0L)
            duration = player.duration.coerceAtLeast(0L)
            playing = player.isPlaying
            delay(500)
        }
    }
    Box(Modifier.fillMaxSize()) {
        AndroidView(
            factory = { PlayerView(it).apply { useController = false; this.player = player } },
            modifier = Modifier.fillMaxSize(),
            onRelease = { it.player = null },
        )
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(), exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Column(
                Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f))))
                    .padding(horizontal = Space.xl, vertical = Space.m),
            ) {
                Slider(
                    value = if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f,
                    onValueChange = { if (duration > 0) player.seekTo((it * duration).toLong()) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    RoundAction(XIcons.Rewind, "Mundur 10 detik") { player.seekTo((player.currentPosition - 10_000L).coerceAtLeast(0L)) }
                    Spacer(Modifier.width(Space.s))
                    RoundAction(if (playing) XIcons.Pause else XIcons.Play, if (playing) "Jeda" else "Putar") {
                        if (playing) player.pause() else player.play()
                    }
                    Spacer(Modifier.width(Space.s))
                    RoundAction(XIcons.Forward, "Maju 10 detik") { player.seekTo(player.currentPosition + 10_000L) }
                    Spacer(Modifier.weight(1f))
                    Text("${formatTime(position)} / ${formatTime(duration)}", style = XType.Caption)
                }
            }
        }
    }
}

private fun formatTime(milliseconds: Long): String {
    val seconds = milliseconds.coerceAtLeast(0L) / 1000
    return if (seconds >= 3600) "%d:%02d:%02d".format(seconds / 3600, (seconds / 60) % 60, seconds % 60)
    else "%02d:%02d".format(seconds / 60, seconds % 60)
}

@Composable
private fun ImmersiveLandscape() {
    val activity = LocalContext.current.findActivity()
    val view = LocalView.current
    DisposableEffect(activity, view) {
        if (activity == null) onDispose { }
        else {
            val window = activity.window
            val oldOrientation = activity.requestedOrientation
            val oldUiFlags = window.decorView.systemUiVisibility
            val controller = WindowCompat.getInsetsController(window, view)
            val oldBehavior = controller.systemBarsBehavior
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
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
    LaunchedEffect(activity, view) {
        delay(250)
        if (activity != null) WindowCompat.getInsetsController(activity.window, view)
            .hide(WindowInsetsCompat.Type.systemBars())
    }
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
