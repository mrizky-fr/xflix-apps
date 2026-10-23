package com.xflix.app.data

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.xflix.app.R

object Catalog {

    private fun ep(n: Int, name: String, min: Int, synopsis: String, @DrawableRes still: Int) =
        Episode(n, name, min, synopsis, still)

    // Durasi belum diketahui
    private fun yt(n: Int, name: String, videoId: String, @DrawableRes still: Int) =
        Episode(n, name, 0, "", still, youtubeId = videoId)

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
        Title(
            id = "sains_rumah_editor", name = "Sains Rumah Editor", kind = Kind.Series, year = 2026, rating = "SU",
            genres = setOf(Genre.Documentary),
            moods = listOf("Sains", "Edukasi", "Matematika"),
            synopsis = "Serial sains dari Rumah Editor.",
            cast = emptyList(), creator = "Rumah Editor",
            poster = R.drawable.poster_sains_rumah_editor, backdrop = R.drawable.backdrop_sains_rumah_editor,
            tint = Color(0xFF173A7A), style = TitleStyle.Serif,
            top10 = 2,
            seasons = listOf(
                Season(
                    1, listOf(
                        ep(
                            1, "MENGAPA PARA ILMUWAN MEMPERCAYAI SESUATU \"YANG TAK TERLIHAT\"??", 19,
                            "Atom-atom yang katanya adalah materi penyusun alam ini ternyata hanyalah 5% dari seluruh materi. Sisanya adalah materi \"tak kasatmata\" yang disebut para ilmuwan \"materi gelap\". Tapi bagaimana mereka yakin materi ini ada?",
                            R.drawable.still_sains_rumah_editor_1,
                        ),
                        ep(
                            2, "TIDAK TERJAWAB SELAMA RIBUAN TAHUN!! INILAH ALASAN MENGAPA SARANG LEBAH BERBENTUK HEKSAGONAL!!", 17,
                            "Mengapa sarang lebah berbentuk heksagonal? Ternyata alasannya sangat matematis, dan baru dibuktikan seorang matematikawan pada tahun 1999 setelah ribuan tahun tidak terjawab.",
                            R.drawable.still_sains_rumah_editor_2,
                        ),
                        ep(3, "ADA KEBENARAN YANG TIDAK BISA DIBUKTIKAN SECARA LOGIKA!!", 24, "", R.drawable.still_sains_rumah_editor_3),
                        ep(4, "1000 TAHUN YANG LALU ILMUWAN INI MENGUKUR BESARNYA BUMI, HASILNYA…", 21, "", R.drawable.still_sains_rumah_editor_4),
                    )
                ),
            ),
        ),
        Title(
            id = "buruk_sangka", name = "Buruk Sangka", kind = Kind.Series, year = 2026, rating = "18+",
            genres = setOf(Genre.Comedy),
            moods = listOf("Komedi", "MLI"),
            synopsis = "Buruk Sangka dari kanal MLI, diputar langsung dari YouTube.",
            cast = emptyList(), creator = "MLI",
            poster = R.drawable.poster_buruk_sangka, backdrop = R.drawable.backdrop_buruk_sangka,
            tint = Color(0xFF7A2E1E), style = TitleStyle.Condensed,
            seasons = listOf(
                Season(1, listOf(yt(1, "Teletabis L*BT!", "q1UgIuICMKE", R.drawable.still_buruk_sangka_1))),
            ),
        ),
        Title(
            id = "why_dakwah_tretan_vol1", name = "Why Dakwah Tretan Vol. 1", kind = Kind.Series, year = 2026, rating = "18+",
            genres = setOf(Genre.Comedy),
            moods = listOf("Komedi", "Tretan Muslim"),
            synopsis = "Kompilasi dakwah Tretan Muslim dari kanal Sarjana Deathcore, diputar langsung dari YouTube.",
            cast = emptyList(), creator = "Sarjana Deathcore",
            poster = R.drawable.poster_why_dakwah_tretan_vol1, backdrop = R.drawable.backdrop_why_dakwah_tretan_vol1,
            tint = Color(0xFF2E3A40), style = TitleStyle.Wide,
            seasons = listOf(
                Season(
                    1, listOf(
                        yt(1, "KOMPILASI TRETAN MUSLIM DAKWAH MODE ULTIMATE VER.", "9T05KaRWWow", R.drawable.still_why_dakwah_tretan_vol1_1),
                    )
                ),
            ),
        ),
        Title(
            id = "why_dakwah_tretan_vol2", name = "Why Dakwah Tretan Vol. 2", kind = Kind.Series, year = 2026, rating = "18+",
            genres = setOf(Genre.Comedy),
            moods = listOf("Komedi", "Coki Pardede"),
            synopsis = "Kompilasi dakwah Coki dari kanal Sarjana Deathcore, diputar langsung dari YouTube.",
            cast = emptyList(), creator = "Sarjana Deathcore",
            poster = R.drawable.poster_why_dakwah_tretan_vol2, backdrop = R.drawable.backdrop_why_dakwah_tretan_vol2,
            tint = Color(0xFF8A3A12), style = TitleStyle.Wide,
            seasons = listOf(
                Season(
                    1, listOf(
                        yt(1, "KOMPILASI DAKWAH COKI ULTIMATE MORAL BREAKER VER", "TMi24hO4qQ0", R.drawable.still_why_dakwah_tretan_vol2_1),
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
        Notice("sains_rumah_editor", "Baru Ditambahkan", "Sains Rumah Editor, 4 episode sudah tersedia.", "Hari ini"),
        Notice("sekolah_normal", "Baru Ditambahkan", "Sekolah Normal, 4 episode sudah tersedia.", "Hari ini"),
    )
}
