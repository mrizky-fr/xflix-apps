package com.xflix.app.data

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.xflix.app.R

object Catalog {

    private fun ep(n: Int, name: String, min: Int, synopsis: String, @DrawableRes still: Int) =
        Episode(n, name, min, synopsis, still)

    val titles: List<Title> = listOf(
        Title(
            id = "sekolah_normal", name = "Sekolah Normal", kind = Kind.Series, year = 2026, rating = "13+",
            genres = setOf(Genre.Comedy),
            moods = listOf("Komedi", "Sekolah", "Santai"),
            synopsis = "Serial komedi sekolah dari Tretan Universe.",
            cast = emptyList(), creator = "Tretan Universe",
            poster = R.drawable.poster_sekolah_normal, backdrop = R.drawable.backdrop_sekolah_normal,
            tint = Color(0xFF2F5A2B), style = TitleStyle.Condensed,
            top10 = 1, newEpisodes = true,
            seasons = listOf(
                Season(
                    1, listOf(
                        ep(1, "Guru Keanu VS Murid Plenger⁉️", 17, "", R.drawable.still_sekolah_normal_1),
                        ep(2, "Kesabaran Guru Keanu Sudah Habis⁉️", 21, "", R.drawable.still_sekolah_normal_2),
                        ep(3, "Sule Pusing Ngajarin Sugeng & Opik", 19, "", R.drawable.still_sekolah_normal_3),
                        ep(4, "Sule Hilang Kesabaran!", 21, "", R.drawable.still_sekolah_normal_4),
                    )
                ),
            ),
        ),
    )

    private val byId = titles.associateBy { it.id }

    fun title(id: String): Title = byId.getValue(id)

    val available: List<Title> = titles.filterNot { it.isUpcoming }

    val upcoming: List<Title> = titles.filter { it.isUpcoming }.sortedBy { it.premiere }

    val top10: List<Title> = available.filter { it.top10 != null }.sortedBy { it.top10 }

    fun similar(to: Title, limit: Int = 9): List<Title> = available
        .filter { it.id != to.id }
        .sortedByDescending { (it.genres intersect to.genres).size }
        .take(limit)

    fun search(query: String): List<Title> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        return titles.filter { t ->
            t.name.lowercase().contains(q) ||
                t.moods.any { it.lowercase().contains(q) } ||
                t.cast.any { it.lowercase().contains(q) }
        }
    }

    val profiles = listOf(
        Profile("raka", "Raka", Color(0xFF2F6FED), Face.Smile),
        Profile("nadia", "Nadia", Color(0xFFF2B33D), Face.Grin),
        Profile("bima", "Bima", Color(0xFFE5604D), Face.Wink),
        Profile("sekar", "Sekar", Color(0xFF2BA36B), Face.Calm),
        Profile("anak", "Anak-anak", Color(0xFF1B1B1B), Face.Kids),
    )

    val continueWatching = emptyList<WatchProgress>()

    val notices = listOf(
        Notice("sekolah_normal", "Baru Ditambahkan", "Sekolah Normal, 4 episode sudah tersedia.", "Hari ini"),
    )
}
