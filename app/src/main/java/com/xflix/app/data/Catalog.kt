package com.xflix.app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/** Public NOVA Play API. No video or catalog snapshot is packaged in the APK. */
object NovaApi {
    const val BASE = "https://novaplay.asia"

    suspend fun get(path: String): JSONObject = withContext(Dispatchers.IO) {
        require(path.startsWith("/api/"))
        val connection = (URL(BASE + path).openConnection() as HttpURLConnection).apply {
            connectTimeout = 12_000
            readTimeout = 20_000
            setRequestProperty("Accept", "application/json")
        }
        try {
            val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (connection.responseCode !in 200..299) {
                val message = runCatching { JSONObject(body).optString("error") }.getOrNull()
                error(message?.takeIf { it.isNotBlank() } ?: "Server merespons HTTP ${connection.responseCode}")
            }
            JSONObject(body)
        } finally {
            connection.disconnect()
        }
    }
}

private fun JSONArray.objects(): List<JSONObject> = (0 until length()).mapNotNull { optJSONObject(it) }
private fun JSONObject.words(key: String): List<String> = optJSONArray(key)?.let { array ->
    (0 until array.length()).mapNotNull { array.optString(it).takeIf(String::isNotBlank) }
}.orEmpty()

object Catalog {
    val titles = mutableStateListOf<Title>()
    val profiles = listOf(
        Profile("novaplay", "Novaplay", Color(0xFF7C3AED), Platform.Novaplay),
        Profile("xflix-yt", "Xflix-YT", Color.Black, Platform.XflixYT),
    )
    var platform by mutableStateOf(Platform.Novaplay)
    val continueWatching = emptyList<WatchProgress>()
    val notices = emptyList<Notice>()

    val available: List<Title> get() = titles.filter { it.platform == platform }
    val upcoming: List<Title> get() = emptyList()
    val top10: List<Title> get() = available.filter { it.top10 != null }.sortedBy { it.top10 }
    fun title(id: String): Title = titles.first { it.id == id }
    fun similar(to: Title, limit: Int = 9): List<Title> = available
        .asSequence().filter { it.id != to.id }
        .sortedByDescending { (it.genres intersect to.genres).size }
        .take(limit).toList()

    fun search(query: String): List<Title> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        return available.filter { it.name.lowercase().contains(q) || it.moods.any { m -> m.lowercase().contains(q) } }
    }

    /** Acak per sesi, stabil selama app terbuka */
    var seed = Random.nextInt()
        private set

    suspend fun refresh() {
        val data = NovaApi.get("/api/catalog")
        val youtube = withContext(Dispatchers.IO) { YouTubeCatalog.titles }
        val popular = buildList {
            data.optJSONArray("movies")?.objects()?.forEachIndexed { i, item -> add(fromRemote(item, "film", i)) }
            data.optJSONArray("dracin")?.objects()?.forEachIndexed { i, item -> add(fromRemote(item, "anime", i)) }
            data.optJSONArray("serials")?.objects()?.forEachIndexed { i, item -> add(fromRemote(item, "anime", i)) }
        }
        val library = buildList {
            data.optJSONArray("animeLibrary")?.objects()?.forEach { add(fromRemote(it, "anime", null)) }
            data.optJSONArray("donghuaLibrary")?.objects()?.forEach { add(fromRemote(it, "anime", null)) }
        }
        val all = (popular + library + youtube).distinctBy { it.id }
        if (all.isEmpty()) error("Katalog NOVA Play masih kosong.")
        seed = Random.nextInt()
        // Top 10 acak per platform
        val random = Random(seed)
        val rank = listOf(popular, youtube).flatMap { group ->
            group.map { it.id }.distinct().shuffled(random).take(10).mapIndexed { i, id -> id to i + 1 }
        }.toMap()
        titles.clear()
        titles.addAll(all.map { title -> title.copy(top10 = rank[title.id]) })
    }

    suspend fun ensureDetails(id: String): Title {
        val current = title(id)
        if (current.kind == Kind.Movie || current.seasons.firstOrNull()?.episodes?.firstOrNull()?.remoteId != null) return current
        val detail = NovaApi.get(detailPath(current))
        val episodes = detail.optJSONArray("episodes")?.objects()?.mapIndexed { index, item ->
            Episode(
                number = index + 1,
                name = item.optString("title", "Episode ${index + 1}"),
                minutes = 0,
                synopsis = "",
                still = 0,
                remoteId = item.optString("id").takeIf { it.isNotBlank() },
                imageUrl = current.imageUrl,
            )
        }.orEmpty()
        if (episodes.isEmpty()) return current
        val updated = current.copy(seasons = listOf(Season(1, episodes)))
        val position = titles.indexOfFirst { it.id == id }
        if (position >= 0) titles[position] = updated
        return updated
    }

    fun detailPath(title: Title): String = when (title.remoteSource) {
        "film" -> "/api/playback/${title.id}"
        else -> "/api/anime/${title.id}"
    }

    private fun fromRemote(item: JSONObject, source: String, rank: Int?): Title {
        val id = item.optString("id")
        val name = item.optString("title", "Tanpa judul")
        val isSeries = source != "film" || item.optBoolean("series")
        val provider = item.optString("provider").ifBlank { if (source == "film") "NOVA Play" else "Sumber video" }
        val rawGenres = item.words("genres")
        val mappedGenres = rawGenres.mapNotNull(::genreFor).toSet().ifEmpty { setOf(Genre.Drama) }
        val episodes = item.optJSONArray("episodes")?.objects()?.mapIndexed { index, ep ->
            Episode(index + 1, ep.optString("title", "Episode ${index + 1}"), 0, "", 0,
                remoteId = ep.optString("id").takeIf { it.isNotBlank() }, imageUrl = item.optString("image"))
        }.orEmpty()
        return Title(
            id = id,
            name = name,
            kind = if (isSeries) Kind.Series else Kind.Movie,
            year = item.optInt("year").takeIf { it in 1900..2100 } ?: LocalDate.now().year,
            rating = "13+",
            genres = mappedGenres,
            moods = rawGenres + listOfNotNull(item.optString("kind").takeIf { it.isNotBlank() }, provider),
            synopsis = item.optString("description").ifBlank { "Tonton ${item.optString("type").ifBlank { "video" }} dari katalog NOVA Play. Ketersediaan mengikuti sumber." },
            cast = emptyList(),
            creator = provider,
            poster = 0,
            backdrop = 0,
            tint = Color(0xFF252B36),
            style = TitleStyle.Condensed,
            seasons = if (isSeries) listOf(Season(1, episodes)) else emptyList(),
            newEpisodes = rank != null && source == "anime",
            imageUrl = item.optString("image").takeIf { it.startsWith("https://") },
            remoteSource = source,
            category = when (item.optString("kind").lowercase()) {
                "anime" -> ContentCategory.Anime
                "donghua" -> ContentCategory.Donghua
                else -> null
            },
        )
    }

    private fun genreFor(raw: String): Genre? = when {
        raw.contains("action", true) || raw.contains("adventure", true) -> Genre.Action
        raw.contains("horror", true) -> Genre.Horror
        raw.contains("thriller", true) -> Genre.Thriller
        raw.contains("romance", true) -> Genre.Romance
        raw.contains("sci", true) || raw.contains("fantasy", true) -> Genre.SciFi
        raw.contains("documentary", true) -> Genre.Documentary
        raw.contains("mystery", true) -> Genre.Mystery
        raw.contains("drama", true) -> Genre.Drama
        raw.contains("crime", true) -> Genre.Crime
        raw.contains("comedy", true) -> Genre.Comedy
        else -> null
    }
}
