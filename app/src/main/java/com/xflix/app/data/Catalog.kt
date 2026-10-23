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

    // Satu video Kamar Film, satu serial
    private fun kamarFilm(
        id: String,
        name: String,
        videoId: String,
        episode: String,
        genres: Set<Genre>,
        moods: List<String>,
        @DrawableRes poster: Int,
        @DrawableRes backdrop: Int,
        @DrawableRes still: Int,
        tint: Color,
        style: TitleStyle,
    ) = Title(
        id = id, name = name, kind = Kind.Series, year = 2026, rating = "13+",
        genres = genres, moods = moods,
        synopsis = "Dari kanal Kamar Film, diputar langsung dari YouTube.",
        cast = emptyList(), creator = "Kamar Film",
        poster = poster, backdrop = backdrop, tint = tint, style = style,
        seasons = listOf(Season(1, listOf(yt(1, episode, videoId, still)))),
    )

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
        kamarFilm(
            id = "kf_reinkarnasi", name = "Teori Reinkarnasi", videoId = "RMaYd-hYgJ0",
            episode = "SECARA SAINS APAKAH TEORI REINKARNASI BISA DIBUKTIKAN?",
            genres = setOf(Genre.Documentary), moods = listOf("Sains", "Misteri"),
            poster = R.drawable.poster_kf_reinkarnasi, backdrop = R.drawable.backdrop_kf_reinkarnasi, still = R.drawable.still_kf_reinkarnasi_1,
            tint = Color(0xFF482624), style = TitleStyle.Serif,
        ),
        kamarFilm(
            id = "kf_sutan_sjahrir", name = "Sutan Sjahrir", videoId = "qZHEHzkNsGo",
            episode = "Demokrat Sejati yang Dihabisi Penguasa: Mengapa Sejarah Sengaja Mengaburkan Sutan Sjahrir?",
            genres = setOf(Genre.Documentary), moods = listOf("Sejarah", "Politik"),
            poster = R.drawable.poster_kf_sutan_sjahrir, backdrop = R.drawable.backdrop_kf_sutan_sjahrir, still = R.drawable.still_kf_sutan_sjahrir_1,
            tint = Color(0xFF54312F), style = TitleStyle.Condensed,
        ),
        kamarFilm(
            id = "kf_alam_semesta", name = "Sendirian di Alam Semesta?", videoId = "GmQ3Q6xJqBg",
            episode = "Benarkah Kita Sendirian di Alam Semesta yang Seluas Ini? Mari Menghitung Kemungkinannya!!!",
            genres = setOf(Genre.Documentary), moods = listOf("Sains", "Antariksa"),
            poster = R.drawable.poster_kf_alam_semesta, backdrop = R.drawable.backdrop_kf_alam_semesta, still = R.drawable.still_kf_alam_semesta_1,
            tint = Color(0xFF4E5357), style = TitleStyle.Serif,
        ),
        kamarFilm(
            id = "kf_area_51", name = "Rahasia Area 51", videoId = "XWEwrV3nOaY",
            episode = "Kenapa Pemerintah Amerika Mati-Matian Sembunyikan Apa yang Sebenarnya Ada di Area 51?",
            genres = setOf(Genre.Documentary, Genre.Mystery), moods = listOf("Misteri", "Konspirasi"),
            poster = R.drawable.poster_kf_area_51, backdrop = R.drawable.backdrop_kf_area_51, still = R.drawable.still_kf_area_51_1,
            tint = Color(0xFF5B4B4B), style = TitleStyle.Condensed,
        ),
        kamarFilm(
            id = "kf_napas_bukti", name = "Napas Jadi Bukti", videoId = "LHvEKZU_Ksc",
            episode = "TIDAK AKAN LAGI KASUS TAK TERPECAHKAN - NAFAS PUN BISA MENJADI BUKTI SEKARANG",
            genres = setOf(Genre.Documentary, Genre.Crime), moods = listOf("Forensik", "Sains"),
            poster = R.drawable.poster_kf_napas_bukti, backdrop = R.drawable.backdrop_kf_napas_bukti, still = R.drawable.still_kf_napas_bukti_1,
            tint = Color(0xFF5F5A56), style = TitleStyle.Wide,
        ),
        kamarFilm(
            id = "kf_omuamua", name = "Misteri Omuamua", videoId = "kYfN9xT_Ld8",
            episode = "600.000 Tahun Perjalanan Menuju Bumi! Siapa Sebenarnya yang Mengirim Omuamua?",
            genres = setOf(Genre.Documentary, Genre.Mystery), moods = listOf("Antariksa", "Misteri"),
            poster = R.drawable.poster_kf_omuamua, backdrop = R.drawable.backdrop_kf_omuamua, still = R.drawable.still_kf_omuamua_1,
            tint = Color(0xFF1A1718), style = TitleStyle.Wide,
        ),
        kamarFilm(
            id = "kf_fbi_trump", name = "Ditangkap FBI, Dibebaskan Trump", videoId = "lhwf8J-payY",
            episode = "DITANGKAP FBI - DIPENJARA SEUMUR HIDUP - LALU DIBEBASKAN OLEH TRUMP - SIAPA ANAK MUDA INI?",
            genres = setOf(Genre.Documentary, Genre.Crime), moods = listOf("Kriminal", "Kisah Nyata"),
            poster = R.drawable.poster_kf_fbi_trump, backdrop = R.drawable.backdrop_kf_fbi_trump, still = R.drawable.still_kf_fbi_trump_1,
            tint = Color(0xFF5F5553), style = TitleStyle.Condensed,
        ),
        kamarFilm(
            id = "kf_gempa_besar", name = "Gempa Besar", videoId = "2wkhktYASdE",
            episode = "SEBERAPA DEKAT GEMPA BESAR ITU AKAN DATANG?",
            genres = setOf(Genre.Documentary), moods = listOf("Sains", "Bencana"),
            poster = R.drawable.poster_kf_gempa_besar, backdrop = R.drawable.backdrop_kf_gempa_besar, still = R.drawable.still_kf_gempa_besar_1,
            tint = Color(0xFF373E42), style = TitleStyle.Condensed,
        ),
        kamarFilm(
            id = "kf_perampokan", name = "Perampokan Paling Mulus", videoId = "dYlu1jUchzo",
            episode = "PERAMPOKAN BESAR PALING MULUS INI JUSTRU BERAKHIR TRAGIS DENGAN CARA SEPELE. APA YANG TERJADI?",
            genres = setOf(Genre.Documentary, Genre.Crime), moods = listOf("Kriminal", "Kisah Nyata"),
            poster = R.drawable.poster_kf_perampokan, backdrop = R.drawable.backdrop_kf_perampokan, still = R.drawable.still_kf_perampokan_1,
            tint = Color(0xFF4A3030), style = TitleStyle.Serif,
        ),
        kamarFilm(
            id = "kf_pemimpin_sekte", name = "Pemimpin Sekte", videoId = "VPlMTCds6QA",
            episode = "Pemimpin Sekte ini Memutus Logika Pengikutnya dari Realitas dengan cara yang Mengerikan",
            genres = setOf(Genre.Documentary, Genre.Crime), moods = listOf("Psikologi", "Kisah Nyata"),
            poster = R.drawable.poster_kf_pemimpin_sekte, backdrop = R.drawable.backdrop_kf_pemimpin_sekte, still = R.drawable.still_kf_pemimpin_sekte_1,
            tint = Color(0xFF4D474A), style = TitleStyle.Serif,
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
        Notice("kf_reinkarnasi", "Baru Ditambahkan", "10 video terbaru Kamar Film sudah bisa ditonton online.", "Hari ini"),
        Notice("sains_rumah_editor", "Baru Ditambahkan", "Sains Rumah Editor, 4 episode sudah tersedia.", "Hari ini"),
        Notice("sekolah_normal", "Baru Ditambahkan", "Sekolah Normal, 4 episode sudah tersedia.", "Hari ini"),
    )
}
