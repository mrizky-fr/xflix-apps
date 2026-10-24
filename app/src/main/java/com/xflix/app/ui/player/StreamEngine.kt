package com.xflix.app.ui.player

import android.annotation.SuppressLint
import android.net.Uri
import android.os.SystemClock
import android.text.TextUtils
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy
import androidx.media3.ui.PlayerView
import androidx.webkit.JavaScriptReplyProxy
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.xflix.app.data.NovaApi
import kotlin.math.abs
import kotlinx.coroutines.delay
import org.json.JSONObject

internal data class StreamSource(val label: String, val url: String, val native: Boolean)

internal fun youtubeSource(videoId: String): StreamSource {
    require(videoId.matches(Regex("[A-Za-z0-9_-]{11}")))
    return StreamSource(
        "YouTube",
        "https://www.youtube.com/embed/$videoId?autoplay=1&playsinline=1&controls=1&rel=0&fs=0&enablejsapi=1",
        false,
    )
}

internal interface StreamCommands {
    fun play()
    fun pause()
    fun seekTo(seconds: Float)
    fun setSpeed(speed: Float)
}

/** Status pemutar, native atau embed */
@Stable
internal class StreamState {
    var started by mutableStateOf(false)
    var direct by mutableStateOf(false)
    var failed by mutableStateOf(false)
    var playing by mutableStateOf(true)
    var buffering by mutableStateOf(true)
    var ended by mutableStateOf(false)
    var position by mutableFloatStateOf(0f)
    var duration by mutableFloatStateOf(0f)
    var buffered by mutableFloatStateOf(0f)
    var commands: StreamCommands? = null

    private var seekTarget = -1f
    private var holdUntil = 0L

    fun play() {
        playing = true
        ended = false
        hold()
        commands?.play()
    }

    fun pause() {
        playing = false
        hold()
        commands?.pause()
    }

    fun seekTo(seconds: Float) {
        val target = if (duration > 0f) seconds.coerceIn(0f, duration) else seconds.coerceAtLeast(0f)
        position = target
        seekTarget = target
        ended = false
        hold()
        commands?.seekTo(target)
    }

    fun setSpeed(speed: Float) {
        commands?.setSpeed(speed)
    }

    // Laporan basi diabaikan sebentar
    private fun hold() {
        holdUntil = SystemClock.elapsedRealtime() + 1_200
    }

    fun report(position: Float, duration: Float, buffered: Float, playing: Boolean, buffering: Boolean, ended: Boolean) {
        val settling = SystemClock.elapsedRealtime() < holdUntil
        if (!settling || seekTarget < 0f || abs(position - seekTarget) < 2f) {
            this.position = position
            seekTarget = -1f
        }
        if (!settling) this.playing = playing
        this.duration = duration.coerceAtLeast(0f)
        this.buffered = if (duration > 0f) (buffered / duration).coerceIn(0f, 1f) else 0f
        this.buffering = buffering
        this.ended = ended
        if (playing && !buffering && position > 0.3f) {
            started = true
            direct = false
        }
    }
}

/** MP4/HLS langsung lewat Media3 */
@OptIn(UnstableApi::class)
@Composable
internal fun NativeStream(url: String, startAt: Float, state: StreamState, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val exo = remember {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(context).setLoadErrorHandlingPolicy(BusyRetry))
            .build()
    }
    DisposableEffect(exo) {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                state.failed = true
            }
        }
        exo.addListener(listener)
        exo.setMediaItem(MediaItem.fromUri(url), (startAt * 1000).toLong())
        exo.prepare()
        exo.playWhenReady = true
        state.commands = object : StreamCommands {
            override fun play() {
                if (exo.playbackState == Player.STATE_ENDED) exo.seekTo(0L)
                exo.play()
            }

            override fun pause() = exo.pause()
            override fun seekTo(seconds: Float) = exo.seekTo((seconds * 1000).toLong())
            override fun setSpeed(speed: Float) = exo.setPlaybackSpeed(speed)
        }
        onDispose {
            state.commands = null
            exo.removeListener(listener)
            exo.release()
        }
    }
    LaunchedEffect(exo) {
        while (true) {
            val stateNow = exo.playbackState
            state.report(
                position = exo.currentPosition / 1000f,
                duration = if (exo.duration > 0) exo.duration / 1000f else 0f,
                buffered = exo.bufferedPosition / 1000f,
                playing = exo.playWhenReady && stateNow != Player.STATE_ENDED,
                buffering = stateNow == Player.STATE_BUFFERING || stateNow == Player.STATE_IDLE,
                ended = stateNow == Player.STATE_ENDED,
            )
            delay(250)
        }
    }
    AndroidView(
        factory = {
            PlayerView(it).apply {
                useController = false
                player = exo
            }
        },
        onRelease = { it.player = null },
        modifier = modifier,
    )
}

/** 429: tunggu lalu coba lagi */
@OptIn(UnstableApi::class)
private object BusyRetry : DefaultLoadErrorHandlingPolicy() {
    override fun getRetryDelayMsFor(info: LoadErrorHandlingPolicy.LoadErrorInfo): Long {
        val error = info.exception
        return if (error is HttpDataSource.InvalidResponseCodeException && error.responseCode == 429) 12_000L
        else super.getRetryDelayMsFor(info)
    }

    override fun getMinimumLoadableRetryCount(dataType: Int): Int = 6
}

/** Embed iframe lewat jembatan JS */
@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun EmbedStream(url: String, startAt: Float, state: StreamState, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bridge = remember { EmbedBridge(state, startAt) }
    val isYouTube = remember(url) { Uri.parse(url).host == "www.youtube.com" }
    val script = remember(isYouTube) {
        if (isYouTube) null
        else runCatching { context.assets.open("player_bridge.js").bufferedReader().use { it.readText() } }.getOrNull()
    }
    // Tanpa jembatan, pakai kontrol sumber
    LaunchedEffect(bridge) {
        val limit = if (script != null && EmbedBridge.supported()) 10_000L else 1_500L
        var waited = 0L
        // Pemutar terdeteksi: tunggu lebih lama
        while (!state.started && waited < if (bridge.connected) 25_000L else limit) {
            delay(500)
            waited += 500
        }
        if (!state.started) state.direct = true
    }
    var view by remember { mutableStateOf<WebView?>(null) }
    // YouTube dijeda saat ditinggal
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, isYouTube) {
        val observer = LifecycleEventObserver { _, event ->
            if (isYouTube && event == Lifecycle.Event.ON_PAUSE) view?.evaluateJavascript(YouTubePause, null)
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).also { view = it }.apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                setBackgroundColor(android.graphics.Color.BLACK)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.setSupportMultipleWindows(false)
                settings.javaScriptCanOpenWindowsAutomatically = false
                if (isYouTube) CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    // Iklan tidak boleh pindah halaman
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                        request.isForMainFrame && request.url.host != if (isYouTube) ctx.packageName else Uri.parse(NovaApi.BASE).host

                    override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
                        state.failed = true
                        return true
                    }
                }
                if (script != null) bridge.install(this, script)
                // Referer wajib untuk YouTube
                val baseUrl = if (isYouTube) "https://${ctx.packageName}/" else NovaApi.BASE
                loadDataWithBaseURL(baseUrl, embedPage(url), "text/html", "UTF-8", null)
            }
        },
        onRelease = {
            view = null
            bridge.release()
            it.stopLoading()
            it.destroy()
        },
    )
}

// Perintah resmi IFrame API YouTube
private const val YouTubePause =
    """document.querySelector('iframe').contentWindow.postMessage('{"event":"command","func":"pauseVideo","args":""}', '*')"""

private fun embedPage(url: String): String {
    val safeUrl = TextUtils.htmlEncode(url)
    return """<!doctype html><html><head><meta name="viewport" content="width=device-width, initial-scale=1"></head>""" +
        """<body style="margin:0;background:#000"><iframe src="$safeUrl" referrerpolicy="strict-origin-when-cross-origin" style="position:fixed;inset:0;width:100%;height:100%;border:0" """ +
        """allow="autoplay; fullscreen; encrypted-media; picture-in-picture" allowfullscreen></iframe></body></html>"""
}

/** Pesan dari semua frame embed */
private class EmbedBridge(private val state: StreamState, private val startAt: Float) : StreamCommands {
    private class Frame(var proxy: JavaScriptReplyProxy, var score: Double, var seen: Long)

    private val frames = HashMap<String, Frame>()
    private var active: Frame? = null
    var connected = false
        private set
    private var wantPlay = true
    private var kicks = 0
    private var lastKick = 0L
    private var resumed = startAt < 1f

    fun install(view: WebView, script: String) {
        if (!supported()) return
        val origins = setOf("*")
        WebViewCompat.addWebMessageListener(view, BridgeName, origins) { _, message, _, _, proxy ->
            receive(message.data, proxy)
        }
        WebViewCompat.addDocumentStartJavaScript(view, script, origins)
        state.commands = this
    }

    fun release() {
        if (state.commands === this) state.commands = null
        frames.clear()
        active = null
    }

    // Data frame tidak dipercaya
    private fun receive(data: String?, proxy: JavaScriptReplyProxy) {
        val json = data?.let { runCatching { JSONObject(it) }.getOrNull() } ?: return
        val id = json.optString("f").takeIf { it.isNotEmpty() && it.length < 32 } ?: return
        val now = SystemClock.elapsedRealtime()
        connected = true
        val frame = frames.getOrPut(id) { Frame(proxy, 0.0, now) }
        frame.proxy = proxy
        frame.score = json.optDouble("k", 0.0).takeIf { it.isFinite() } ?: 0.0
        frame.seen = now
        frames.values.removeAll { now - it.seen > 3_000 }
        val current = active?.takeIf { now - it.seen <= 3_000 }
        if (current == null || frame.score > current.score) active = frame
        if (active !== frame) return

        val duration = json.optDouble("d", 0.0).toFloat().takeIf { it.isFinite() } ?: 0f
        val playing = json.optBoolean("on")
        state.report(
            position = json.optDouble("p", 0.0).toFloat().takeIf { it.isFinite() } ?: 0f,
            duration = duration,
            buffered = json.optDouble("b", 0.0).toFloat().takeIf { it.isFinite() } ?: 0f,
            playing = playing,
            buffering = json.optBoolean("w"),
            ended = json.optBoolean("e"),
        )
        // Lanjut dari server sebelumnya
        if (!resumed && duration > startAt) {
            resumed = true
            seekTo(startAt)
        }
        // Mulai otomatis
        if (!state.started && wantPlay && !playing && now - lastKick > 2_000 && kicks < 6) {
            kicks++
            lastKick = now
            send("play")
        }
    }

    private fun send(command: String, value: Float? = null) {
        val proxy = active?.proxy ?: return
        val json = JSONObject().put("c", command)
        if (value != null) json.put("v", value.toDouble())
        runCatching { proxy.postMessage(json.toString()) }
    }

    override fun play() {
        wantPlay = true
        send("play")
    }

    override fun pause() {
        wantPlay = false
        send("pause")
    }

    override fun seekTo(seconds: Float) = send("seek", seconds)
    override fun setSpeed(speed: Float) = send("rate", speed)

    companion object {
        const val BridgeName = "XflixBridge"

        fun supported(): Boolean =
            WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER) &&
                WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)
    }
}

internal fun JSONObject.streamSources(): List<StreamSource> {
    val result = mutableListOf<StreamSource>()
    val players = optJSONArray("players")
    if (players != null) for (i in 0 until players.length()) {
        val player = players.optJSONObject(i) ?: continue
        val url = player.optString("url")
        if (url.startsWith("https://")) result += StreamSource(player.optString("label", "Server ${i + 1}"), url, false)
    }
    val videoId = optString("videoId")
    if (optString("format") == "youtube" && videoId.matches(Regex("[A-Za-z0-9_-]{11}"))) {
        result += youtubeSource(videoId)
    }
    val media = optString("url")
    if (optString("format") in listOf("mp4", "hls") && media.isNotBlank()) {
        val full = if (media.startsWith("/api/")) NovaApi.BASE + media else media
        if (full.startsWith("https://")) result += StreamSource("Stream", full, true)
    }
    return result
}
