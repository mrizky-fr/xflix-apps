package com.xflix.app.ui.components

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xflix.app.ui.theme.XColors
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Decode seukuran tampil, cache LRU
private object Bitmaps {
    private val cache = object : LruCache<String, ImageBitmap>((Runtime.getRuntime().maxMemory() / 1024 / 6).toInt()) {
        override fun sizeOf(key: String, value: ImageBitmap) = value.width * value.height * 4 / 1024
    }
    private val sourceWidths = ConcurrentHashMap<Int, Int>()

    fun key(id: Int, targetPx: Int) = "$id@$targetPx"
    fun key(url: String, targetPx: Int) = "$url@$targetPx"

    fun cached(key: String): ImageBitmap? = cache.get(key)

    fun load(resources: Resources, id: Int, targetPx: Int): ImageBitmap {
        val key = key(id, targetPx)
        cache.get(key)?.let { return it }
        val source = sourceWidths.getOrPut(id) {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeResource(resources, id, bounds)
            bounds.outWidth
        }
        var sample = 1
        while (source / (sample * 2) >= targetPx) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val bitmap = BitmapFactory.decodeResource(resources, id, options).asImageBitmap()
        cache.put(key, bitmap)
        return bitmap
    }

    fun loadUrl(url: String, targetPx: Int): ImageBitmap? {
        val key = key(url, targetPx)
        cache.get(key)?.let { return it }
        if (!url.startsWith("https://")) return null
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 8_000
            readTimeout = 12_000
        }
        return try {
            if (connection.responseCode !in 200..299 || !connection.contentType.orEmpty().startsWith("image/")) return null
            val bytes = ByteArrayOutputStream()
            connection.inputStream.use { input ->
                val chunk = ByteArray(8192)
                while (true) {
                    val size = input.read(chunk)
                    if (size < 0) break
                    if (bytes.size() + size > 8_000_000) return null
                    bytes.write(chunk, 0, size)
                }
            }
            val data = bytes.toByteArray()
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(data, 0, data.size, bounds)
            var sample = 1
            while (bounds.outWidth / (sample * 2) >= targetPx) sample *= 2
            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size,
                BitmapFactory.Options().apply { inSampleSize = sample })?.asImageBitmap() ?: return null
            cache.put(key, bitmap)
            bitmap
        } finally {
            connection.disconnect()
        }
    }
}

/** Decode async; eager untuk hero */
@Composable
fun XImage(
    @DrawableRes id: Int,
    modifier: Modifier = Modifier,
    displayWidth: Dp = 160.dp,
    contentScale: ContentScale = ContentScale.Crop,
    alignment: Alignment = Alignment.Center,
    placeholder: Color = XColors.SurfaceHigh,
    eager: Boolean = false,
    contentDescription: String? = null,
    url: String? = null,
) {
    val resources = LocalResources.current
    val targetPx = with(LocalDensity.current) { displayWidth.roundToPx() }
    val key = if (url != null) Bitmaps.key(url, targetPx) else Bitmaps.key(id, targetPx)
    val initial = remember(key) {
        Bitmaps.cached(key) ?: if (eager && url == null && id != 0) Bitmaps.load(resources, id, targetPx) else null
    }
    val bitmap by produceState(initial, key) {
        if (value == null) value = withContext(Dispatchers.IO) {
            runCatching { if (url != null) Bitmaps.loadUrl(url, targetPx) else if (id != 0) Bitmaps.load(resources, id, targetPx) else null }.getOrNull()
        }
    }
    val alpha by animateFloatAsState(
        targetValue = if (bitmap != null) 1f else 0f,
        animationSpec = tween(220),
        label = "image-fade",
    )
    Box(modifier.background(placeholder)) {
        bitmap?.let {
            Image(
                bitmap = it,
                contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { this.alpha = alpha },
                contentScale = contentScale,
                alignment = alignment,
            )
        }
    }
}
