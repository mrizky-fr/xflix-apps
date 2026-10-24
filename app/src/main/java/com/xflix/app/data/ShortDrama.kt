package com.xflix.app.data

import android.media.MediaCodecList
import android.os.SystemClock
import androidx.compose.ui.graphics.Color
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/** API demo Sansekai, 10 request/menit */
internal object Sansekai {
    private const val BASE = "https://api.sansekai.my.id/api"
    private const val Window = 60_000L
    // Sisakan satu untuk cadangan
    private const val Limit = 9
    private val lock = Mutex()
    private val recent = ArrayDeque<Long>()
    private val cache = HashMap<String, Pair<Long, JSONObject>>()

    suspend fun get(path: String, ttl: Long = 10 * 60_000L): JSONObject = lock.withLock {
        cache[path]?.let { (at, json) -> if (SystemClock.elapsedRealtime() - at < ttl) return@withLock json }
        val now = SystemClock.elapsedRealtime()
        while (recent.isNotEmpty() && now - recent.first() >= Window) recent.removeFirst()
        if (recent.size >= Limit) {
            delay(Window - (now - recent.first()))
            recent.removeFirst()
        }
        recent.addLast(SystemClock.elapsedRealtime())
        val json = fetchPatiently(path)
        cache[path] = SystemClock.elapsedRealtime() to json
        json
    }

    // Server sibuk: tunggu lalu ulangi
    private suspend fun fetchPatiently(path: String): JSONObject {
        repeat(2) { attempt ->
            try {
                return fetch(path)
            } catch (busy: Busy) {
                delay(10_000L * (attempt + 1))
            }
        }
        return fetch(path)
    }

    private class Busy : IllegalStateException("Server drama sedang sibuk. Coba lagi sebentar.")

    private suspend fun fetch(path: String): JSONObject = withContext(Dispatchers.IO) {
        val connection = (URL(BASE + path).openConnection() as HttpURLConnection).apply {
            connectTimeout = 12_000
            readTimeout = 20_000
            setRequestProperty("Accept", "application/json")
        }
        try {
            val code = connection.responseCode
            if (code == 429) throw Busy()
            if (code !in 200..299) error("Server drama merespons HTTP $code")
            JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
        } finally {
            connection.disconnect()
        }
    }
}

/** PineDrama dan Melolo lewat Sansekai */
internal object ShortDrama {
    private const val Pine = "pinedrama"
    private const val Melolo = "melolo"

    private val hevc by lazy {
        MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos.any { info ->
            !info.isEncoder && info.supportedTypes.any { it.equals("video/hevc", ignoreCase = true) }
        }
    }

    suspend fun pineDrama(): List<Title> = listOf("/pinedrama/trending?lang=id", "/pinedrama/foryou?lang=id")
        .flatMap { path -> Sansekai.get(path).optJSONArray("collections").objects().map(::pineTitle) }
        .distinctBy { it.id }

    // Dua daftar saja, hemat kuota
    suspend fun melolo(): List<Title> = listOf("/melolo/trending?lang=id", "/melolo/get-latest?lang=id")
        .flatMap { path -> books(Sansekai.get(path)) }
        .map(::meloloTitle)
        .distinctBy { it.id }

    /** Sinopsis dan ID video episode */
    suspend fun details(title: Title): Title = when (title.remoteSource) {
        Pine -> {
            val json = Sansekai.get("/pinedrama/detail?collection_id=${remoteId(title)}&lang=id")
            val total = json.optInt("total_episodes", title.seasons.firstOrNull()?.episodes?.size ?: 0)
            title.copy(
                synopsis = json.text("description").ifBlank { title.synopsis },
                seasons = listOf(Season(1, numbered(total, title.imageUrl))),
            )
        }
        Melolo -> {
            val data = Sansekai.get("/melolo/detail?book_id=${remoteId(title)}&lang=id")
                .optJSONObject("data")?.optJSONObject("video_data")
            val episodes = data?.optJSONArray("video_list").objects().mapIndexed { i, video ->
                Episode(
                    number = i + 1,
                    name = "Episode ${video.optInt("vid_index", i + 1)}",
                    minutes = (video.optInt("duration") + 59) / 60,
                    synopsis = "",
                    still = 0,
                    remoteId = video.text("vid").ifBlank { null },
                    imageUrl = title.imageUrl,
                )
            }
            title.copy(
                synopsis = data?.text("series_intro")?.ifBlank { null } ?: title.synopsis,
                seasons = if (episodes.isEmpty()) title.seasons else listOf(Season(1, episodes)),
            )
        }
        else -> title
    }

    /** Pasangan label dan URL MP4 */
    suspend fun streams(title: Title, episode: Episode): List<Pair<String, String>> {
        val id = requireNotNull(episode.remoteId) { "Episode belum tersedia." }
        return when (title.remoteSource) {
            Pine -> {
                val json = Sansekai.get("/pinedrama/get-episode?collection_id=${remoteId(title)}&episodeNumber=$id&lang=id", ttl = 4 * 60_000L)
                val urls = buildList {
                    add(json.text("best_url"))
                    for (part in listOf("main", "alt")) {
                        val group = json.optJSONObject(part) ?: continue
                        for (key in listOf("indo_hd_cdn_urls", "indo_cdn_urls", "cdn_urls")) addAll(group.strings(key))
                    }
                }.filter { it.startsWith("https://") }.distinct()
                urls.mapIndexed { i, url -> (if (i == 0) "HD" else "Cadangan $i") to url }
            }
            Melolo -> {
                val json = Sansekai.get("/melolo/get-episode?videoId=${URLEncoder.encode(id, "UTF-8")}", ttl = 4 * 60_000L)
                json.optJSONArray("qualities").objects()
                    // HEVC hanya jika didukung
                    .filter { hevc || !it.optString("codec").equals("bytevc1", ignoreCase = true) }
                    .sortedByDescending { it.optString("definition").filter(Char::isDigit).toIntOrNull() ?: 0 }
                    .mapNotNull { q -> q.text("streamUrl").takeIf { it.startsWith("https://") }?.let { q.optString("definition") to it } }
            }
            else -> emptyList()
        }
    }

    private fun remoteId(title: Title) = title.id.substringAfter('-')

    private fun pineTitle(item: JSONObject): Title {
        val cover = item.text("cover").takeIf { it.startsWith("https://") }
        return drama(
            id = "$Pine-${item.text("collection_id")}",
            name = item.text("title"),
            synopsis = item.text("description"),
            tags = item.strings("tags"),
            cover = cover,
            source = Pine,
            creator = "PineDrama",
            episodes = numbered(item.optInt("total_episodes"), cover),
            fresh = item.optBoolean("label_new"),
        )
    }

    private fun meloloTitle(book: JSONObject): Title {
        val cover = book.text("thumb_url").takeIf { it.startsWith("https://") }
        val total = book.text("serial_count").toIntOrNull() ?: 0
        return drama(
            id = "$Melolo-${book.text("book_id")}",
            name = book.text("book_name"),
            synopsis = book.text("abstract"),
            tags = book.strings("stat_infos").flatMap { it.split(",") }.map(String::trim).filter(String::isNotEmpty),
            cover = cover,
            source = Melolo,
            creator = "Melolo",
            // ID video menyusul dari detail
            episodes = (1..total).map { n -> Episode(n, "Episode $n", 0, "", 0, imageUrl = cover) },
            fresh = book.text("is_new_book") == "1",
        )
    }

    private fun numbered(total: Int, cover: String?) =
        (1..total).map { n -> Episode(n, "Episode $n", 0, "", 0, remoteId = n.toString(), imageUrl = cover) }

    private fun drama(
        id: String,
        name: String,
        synopsis: String,
        tags: List<String>,
        cover: String?,
        source: String,
        creator: String,
        episodes: List<Episode>,
        fresh: Boolean,
    ) = Title(
        id = id,
        name = name.ifBlank { "Tanpa judul" },
        kind = Kind.Series,
        year = LocalDate.now().year,
        rating = "13+",
        genres = tags.mapNotNull(::genreOf).toSet().ifEmpty { setOf(Genre.Drama) },
        moods = tags + "drama pendek",
        synopsis = synopsis,
        cast = emptyList(),
        creator = creator,
        poster = 0,
        backdrop = 0,
        tint = Color(0xFF2A1F2D),
        style = TitleStyle.Condensed,
        seasons = listOf(Season(1, episodes)),
        newEpisodes = fresh,
        imageUrl = cover,
        remoteSource = source,
    )

    private fun genreOf(tag: String): Genre? = when {
        tag.contains("cinta", true) || tag.contains("romans", true) || tag.contains("romance", true) -> Genre.Romance
        tag.contains("balas dendam", true) || tag.contains("aksi", true) || tag.contains("petualangan", true) -> Genre.Action
        tag.contains("misteri", true) || tag.contains("gaib", true) -> Genre.Mystery
        tag.contains("horor", true) -> Genre.Horror
        tag.contains("komedi", true) || tag.contains("lucu", true) -> Genre.Comedy
        tag.contains("fantasi", true) || tag.contains("sistem", true) -> Genre.SciFi
        tag.contains("keluarga", true) || tag.contains("drama", true) -> Genre.Drama
        else -> null
    }

    // Kumpulkan buku dari struktur bersarang
    private fun books(root: JSONObject): List<JSONObject> {
        val found = mutableListOf<JSONObject>()
        fun walk(node: Any?) {
            when (node) {
                is JSONObject -> if (node.has("book_id") && node.has("book_name")) found += node
                    else node.keys().forEach { walk(node.opt(it)) }
                is JSONArray -> for (i in 0 until node.length()) walk(node.opt(i))
            }
        }
        walk(root)
        return found
    }

    private fun JSONArray?.objects(): List<JSONObject> = this?.let { array -> (0 until array.length()).mapNotNull { array.optJSONObject(it) } }.orEmpty()
    private fun JSONObject.text(key: String): String = if (isNull(key)) "" else optString(key).trim()
    private fun JSONObject.strings(key: String): List<String> =
        optJSONArray(key)?.let { array -> (0 until array.length()).mapNotNull { array.optString(it).trim().takeIf(String::isNotEmpty) } }.orEmpty()
}
