package com.xflix.app.data

import androidx.compose.ui.graphics.Color
import com.xflix.app.R
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

    // Poster tanpa teks, dibingkai rapi
    private val art = mapOf(
        "yt-upin-ipin" to (R.drawable.poster_upin_ipin to R.drawable.backdrop_upin_ipin),
        "yt-upin-ipin-iqra" to (R.drawable.poster_upin_ipin_iqra to R.drawable.backdrop_upin_ipin_iqra),
        "yt-upin-ipin-puasa-raya" to (R.drawable.poster_upin_ipin_puasa_raya to R.drawable.backdrop_upin_ipin_puasa_raya),
        "yt-upin-ipin-kembara" to (R.drawable.poster_upin_ipin_kembara to R.drawable.backdrop_upin_ipin_kembara),
        "yt-upin-ipin-metrobot" to (R.drawable.poster_upin_ipin_metrobot to R.drawable.backdrop_upin_ipin_metrobot),
        "yt-upin-ipin-pin-pin-pom" to (R.drawable.poster_upin_ipin_pin_pin_pom to R.drawable.backdrop_upin_ipin_pin_pin_pom),
        "yt-upin-ipin-sebuah" to (R.drawable.poster_upin_ipin_sebuah to R.drawable.backdrop_upin_ipin_sebuah),
        "pzd-pada-zaman-dahulu" to (R.drawable.poster_pzd to R.drawable.backdrop_pzd),
    )

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
        val id = item.getString("id")
        val (poster, backdrop) = art[id] ?: (0 to 0)
        Title(
            id = id,
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
            poster = poster,
            backdrop = backdrop,
            tint = Color(0xFF244A36),
            style = TitleStyle.Condensed,
            seasons = seasons,
            imageUrl = if (poster != 0) null else thumbnail(item.getString("poster")),
            remoteSource = "youtube",
            category = ContentCategory.entries.firstOrNull { it.name == item.optString("category") },
        )
    }

    private fun JSONArray.objects(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }
    private fun JSONObject.strings(key: String): List<String> =
        optJSONArray(key)?.let { array -> (0 until array.length()).map { array.getString(it) } }.orEmpty()
}
