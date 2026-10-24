package com.xflix.app.ui.player

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.net.Uri
import android.text.TextUtils
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.xflix.app.data.Catalog
import com.xflix.app.data.Kind
import com.xflix.app.data.NovaApi
import com.xflix.app.data.Title
import org.json.JSONObject

private data class StreamSource(val label: String, val url: String, val native: Boolean)

@Composable
fun PlayerScreen(id: String, startSeason: Int, startEpisode: Int, onBack: () -> Unit) {
    val context = LocalContext.current
    val title = Catalog.title(id)
    var episodeIndex by remember(id) { mutableIntStateOf(startEpisode.coerceAtLeast(1)) }
    var serverIndex by remember(id) { mutableIntStateOf(0) }
    var sources by remember(id) { mutableStateOf<List<StreamSource>>(emptyList()) }
    var loading by remember(id) { mutableStateOf(true) }
    var error by remember(id) { mutableStateOf<String?>(null) }
    var retry by remember(id) { mutableIntStateOf(0) }

    DisposableEffect(context) {
        val activity = context.findActivity()
        val oldOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        onDispose {
            if (oldOrientation != null) activity.requestedOrientation = oldOrientation
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

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
            val playback = NovaApi.get(path)
            sources = playback.sources()
            if (sources.isEmpty()) error = "Sumber pemutar belum tersedia untuk judul ini."
        } catch (cause: Exception) {
            error = cause.message ?: "Gagal memuat video."
        } finally {
            loading = false
        }
    }

    val episodes = title.seasons.firstOrNull()?.episodes.orEmpty()
    Column(Modifier.fillMaxSize().background(Color.Black)) {
        Row(Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("‹  Kembali", color = Color.White, modifier = Modifier.clickable(onClick = onBack))
            Spacer(Modifier.width(20.dp))
            Text(title.name, color = Color.White, maxLines = 1)
        }
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            when {
                loading -> Text("Memuat pemutar…", color = Color.White)
                error != null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error.orEmpty(), color = Color.White)
                    Spacer(Modifier.height(14.dp))
                    Text("Coba lagi", color = Color.White, modifier = Modifier.clickable { retry++ })
                }
                sources.isNotEmpty() -> {
                    val source = sources[serverIndex.coerceIn(sources.indices)]
                    if (source.native) NativeStream(source.url)
                    else EmbedStream(source.url)
                }
            }
        }
        if (sources.size > 1) {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                sources.forEachIndexed { index, source ->
                    Text(source.label, color = if (index == serverIndex) Color.White else Color.LightGray,
                        modifier = Modifier.background(if (index == serverIndex) Color.DarkGray else Color.Black)
                            .clickable { serverIndex = index }.padding(horizontal = 12.dp, vertical = 8.dp))
                }
            }
        }
        if (episodes.size > 1) {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                episodes.forEachIndexed { index, episode ->
                    Text(episode.name, color = if (index + 1 == episodeIndex) Color.White else Color.LightGray,
                        maxLines = 1,
                        modifier = Modifier.background(if (index + 1 == episodeIndex) Color.DarkGray else Color.Black)
                            .clickable { episodeIndex = index + 1 }.padding(horizontal = 12.dp, vertical = 8.dp))
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
private fun NativeStream(url: String) {
    val context = LocalContext.current
    val player = remember(url) { ExoPlayer.Builder(context).build().apply {
        setMediaItem(MediaItem.fromUri(url))
        prepare()
        playWhenReady = true
    } }
    DisposableEffect(player) { onDispose { player.release() } }
    AndroidView(factory = { PlayerView(it).apply { this.player = player } }, modifier = Modifier.fillMaxSize(),
        onRelease = { it.player = null })
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
