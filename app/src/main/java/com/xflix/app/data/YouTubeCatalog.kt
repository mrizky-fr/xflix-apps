package com.xflix.app.data

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import org.json.JSONArray
import org.json.JSONObject

/** Dibuat oleh tools/youtube_catalog.py */
internal object YouTubeCatalog {
    private const val FILE = "/youtube_catalog.json"

    val titles: List<Title> by lazy {
        val json = YouTubeCatalog::class.java.getResourceAsStream(FILE)?.bufferedReader()?.use { it.readText() }
        json?.let { runCatching { parse(JSONObject(it)) }.getOrNull() }.orEmpty()
    }

    private fun thumbnail(videoId: String) = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"

    private fun parse(root: JSONObject): List<Title> = root.getJSONArray("titles").objects().map { item ->
        val seasons = item.getJSONArray("seasons").objects().map { season ->
            Season(
                number = season.getInt("number"),
                episodes = season.getJSONArray("episodes").objects().mapIndexed { index, ep ->
                    val videoId = ep.getString("id")
                    Episode(index + 1, ep.getString("name"), 0, "", 0, remoteId = videoId, imageUrl = thumbnail(videoId))
                },
            )
        }
        Title(
            id = item.getString("id"),
            name = item.getString("name"),
            kind = Kind.Series,
            year = item.optInt("year").takeIf { it in 1900..2100 } ?: LocalDate.now().year,
            rating = "SU",
            genres = item.strings("genres").mapNotNull { name -> Genre.entries.firstOrNull { it.name == name } }.toSet()
                .ifEmpty { setOf(Genre.Comedy) },
            moods = item.strings("moods"),
            synopsis = item.optString("synopsis"),
            cast = emptyList(),
            creator = item.optString("creator"),
            poster = 0,
            backdrop = 0,
            tint = Color(0xFF244A36),
            style = TitleStyle.Condensed,
            seasons = seasons,
            imageUrl = thumbnail(item.getString("poster")),
            remoteSource = "youtube",
            category = ContentCategory.entries.firstOrNull { it.name == item.optString("category") },
        )
    }

    private fun JSONArray.objects(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }
    private fun JSONObject.strings(key: String): List<String> =
        optJSONArray(key)?.let { array -> (0 until array.length()).map { array.getString(it) } }.orEmpty()
}
