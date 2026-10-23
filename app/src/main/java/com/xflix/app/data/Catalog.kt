package com.xflix.app.data

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.xflix.app.R
import java.time.LocalDate

// Data contoh, ganti dengan API
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
        Title(
            id = "garis_api", name = "Garis Api", kind = Kind.Movie, year = 2026, rating = "18+",
            genres = setOf(Genre.Action, Genre.Thriller),
            moods = listOf("Menegangkan", "Penuh Aksi", "Balas Dendam"),
            synopsis = "Mantan pemadam kebakaran Laras menemukan bahwa kebakaran yang menewaskan adiknya disengaja. Satu malam, satu kota, dan semua orang yang ia percaya punya alasan untuk menyalakan korek.",
            cast = listOf("Laras Wibisono", "Arya Pradana", "Gilang Saputro"), creator = "Rendra Halim",
            poster = R.drawable.poster_garis_api, backdrop = R.drawable.backdrop_garis_api,
            tint = Color(0xFFB4410F), style = TitleStyle.Condensed,
            minutes = 118, original = true, top10 = 2,
        ),
        Title(
            id = "sunyi_di_lembah", name = "Sunyi di Lembah", kind = Kind.Movie, year = 2024, rating = "18+",
            genres = setOf(Genre.Horror),
            moods = listOf("Horor", "Atmosferik", "Folk"),
            synopsis = "Lima mahasiswa KKN tiba di desa yang warganya tak pernah bicara setelah matahari terbenam. Di malam ketiga, salah satu dari mereka mulai menjawab panggilan yang tak terdengar oleh yang lain.",
            cast = listOf("Sekar Ayu", "Reza Mahendra", "Dewi Larasati"), creator = "Wulan Pratiwi",
            poster = R.drawable.poster_sunyi_di_lembah, backdrop = R.drawable.backdrop_sunyi_di_lembah,
            tint = Color(0xFF7A5424), style = TitleStyle.Serif,
            minutes = 104, top10 = 3,
        ),
        Title(
            id = "orbit_terakhir", name = "Orbit Terakhir", kind = Kind.Movie, year = 2026, rating = "13+",
            genres = setOf(Genre.SciFi, Genre.Thriller),
            moods = listOf("Fiksi Ilmiah", "Menegangkan", "Visual Memukau"),
            synopsis = "Saat stasiun riset orbit milik Indonesia kehilangan kontak dengan Bumi, satu-satunya insinyur yang tersisa harus memilih: memperbaiki antena, atau menyelamatkan kru yang mungkin sudah tidak ada.",
            cast = listOf("Dimas Arditya", "Kirana Larasati", "Johan Siregar"), creator = "Andre Kusuma",
            poster = R.drawable.poster_orbit_terakhir, backdrop = R.drawable.backdrop_orbit_terakhir,
            tint = Color(0xFF4A1F8F), style = TitleStyle.Wide,
            minutes = 126, original = true, top10 = 4,
        ),
        Title(
            id = "tamu_tengah_malam", name = "Tamu Tengah Malam", kind = Kind.Movie, year = 2025, rating = "16+",
            genres = setOf(Genre.Thriller, Genre.Mystery),
            moods = listOf("Thriller", "Psikologis", "Satu Malam"),
            synopsis = "Saat badai memutus listrik di apartemennya, seorang penerjemah membukakan pintu untuk tetangga yang minta berteduh. Masalahnya, unit sebelah sudah kosong sejak bulan lalu.",
            cast = listOf("Kirana Larasati", "Johan Siregar"), creator = "Mira Santoso",
            poster = R.drawable.poster_tamu_tengah_malam, backdrop = R.drawable.backdrop_tamu_tengah_malam,
            tint = Color(0xFF3F4C5E), style = TitleStyle.Serif,
            minutes = 99, top10 = 5,
        ),
        Title(
            id = "jarak", name = "Jarak", kind = Kind.Movie, year = 2025, rating = "13+",
            genres = setOf(Genre.Romance, Genre.Drama),
            moods = listOf("Romantis", "Mengharukan", "Drama"),
            synopsis = "Dua orang yang dulu berjanji bertemu lagi sepuluh tahun kemudian di tempat yang sama. Hanya satu dari mereka yang masih ingat janji itu.",
            cast = listOf("Sekar Ayu", "Dimas Arditya"), creator = "Kinan Hapsari",
            poster = R.drawable.poster_jarak, backdrop = R.drawable.backdrop_jarak,
            tint = Color(0xFF1F4E5F), style = TitleStyle.SerifItalic,
            minutes = 97,
        ),
        Title(
            id = "simfoni_kelam", name = "Simfoni Kelam", kind = Kind.Movie, year = 2025, rating = "16+",
            genres = setOf(Genre.Drama),
            moods = listOf("Musik", "Drama", "Gelap"),
            synopsis = "Vokalis band indie yang sedang naik daun menerima tawaran label besar, dengan satu syarat: lagu yang membuatnya terkenal harus diakui sebagai milik orang lain.",
            cast = listOf("Bagas Prakoso", "Alya Maheswari"), creator = "Kinan Hapsari",
            poster = R.drawable.poster_simfoni_kelam, backdrop = R.drawable.backdrop_simfoni_kelam,
            tint = Color(0xFF3F3F46), style = TitleStyle.Serif,
            minutes = 112,
        ),
        Title(
            id = "senja_di_jogja", name = "Senja di Jogja", kind = Kind.Movie, year = 2024, rating = "13+",
            genres = setOf(Genre.Romance, Genre.Drama),
            moods = listOf("Romantis", "Hangat", "Komedi"),
            synopsis = "Arsitek Jakarta pulang ke Jogja untuk menjual rumah neneknya, lalu bertemu tetangga yang menolak pindah dan menolak berhenti mengomentari semua keputusannya.",
            cast = listOf("Alya Maheswari", "Gilang Saputro"), creator = "Kinan Hapsari",
            poster = R.drawable.poster_senja_di_jogja, backdrop = R.drawable.backdrop_senja_di_jogja,
            tint = Color(0xFFA0520C), style = TitleStyle.SerifItalic,
            minutes = 101,
        ),
        Title(
            id = "penjaga_mercusuar", name = "Penjaga Mercusuar", kind = Kind.Movie, year = 2023, rating = "16+",
            genres = setOf(Genre.Mystery, Genre.Thriller),
            moods = listOf("Misteri", "Psikologis", "Sunyi"),
            synopsis = "Ditugaskan menjaga mercusuar di pulau terluar selama enam bulan, Bayu mulai menerima sinyal lampu dari kapal yang tenggelam tiga puluh tahun lalu.",
            cast = listOf("Johan Siregar", "Hendra Lubis"), creator = "Andre Kusuma",
            poster = R.drawable.poster_penjaga_mercusuar, backdrop = R.drawable.backdrop_penjaga_mercusuar,
            tint = Color(0xFF404040), style = TitleStyle.Serif,
            minutes = 109,
        ),
        Title(
            id = "jalur_selatan", name = "Jalur Selatan", kind = Kind.Movie, year = 2025, rating = "13+",
            genres = setOf(Genre.Drama),
            moods = listOf("Petualangan", "Keluarga", "Road Trip"),
            synopsis = "Tiga saudara yang tak saling bicara selama lima tahun terpaksa menyetir mobil tua ayah mereka dari Bandung ke Banyuwangi untuk menabur abunya.",
            cast = listOf("Gilang Saputro", "Sekar Ayu", "Bagas Prakoso"), creator = "Rendra Halim",
            poster = R.drawable.poster_jalur_selatan, backdrop = R.drawable.backdrop_jalur_selatan,
            tint = Color(0xFF8A3A12), style = TitleStyle.Wide,
            minutes = 95,
        ),
        Title(
            id = "peron_terakhir", name = "Peron Terakhir", kind = Kind.Movie, year = 2024, rating = "18+",
            genres = setOf(Genre.Horror, Genre.Thriller),
            moods = listOf("Horor", "Menegangkan", "Urban"),
            synopsis = "Kereta terakhir malam itu berhenti di stasiun yang tidak ada di peta. Pintu terbuka, dan petugas di peron memanggil nama setiap penumpang.",
            cast = listOf("Reza Mahendra", "Dewi Larasati"), creator = "Wulan Pratiwi",
            poster = R.drawable.poster_peron_terakhir, backdrop = R.drawable.backdrop_peron_terakhir,
            tint = Color(0xFF2A2A2E), style = TitleStyle.Condensed,
            minutes = 92,
        ),
        Title(
            id = "raja_jalanan", name = "Raja Jalanan", kind = Kind.Movie, year = 2023, rating = "16+",
            genres = setOf(Genre.Action, Genre.Crime),
            moods = listOf("Aksi", "Balapan", "Adrenalin"),
            synopsis = "Kurir motor tercepat di Jakarta Selatan dipaksa menjadi sopir pelarian untuk geng yang menahan kakaknya. Ia punya satu malam dan satu tangki bensin.",
            cast = listOf("Arya Pradana", "Nadia Kirana"), creator = "Rendra Halim",
            poster = R.drawable.poster_raja_jalanan, backdrop = R.drawable.backdrop_raja_jalanan,
            tint = Color(0xFF7A1D1D), style = TitleStyle.Condensed,
            minutes = 108,
        ),
        Title(
            id = "bromo", name = "Bromo: Napas Gunung", kind = Kind.Movie, year = 2024, rating = "SU",
            genres = setOf(Genre.Documentary),
            moods = listOf("Dokumenter", "Alam", "Inspiratif"),
            synopsis = "Selama setahun, kamera mengikuti warga Tengger yang hidup berdampingan dengan kawah aktif, dari upacara Kasada hingga malam-malam ketika gunung mulai bergemuruh.",
            cast = listOf("Narator: Hendra Lubis"), creator = "Sari Widjaja",
            poster = R.drawable.poster_bromo, backdrop = R.drawable.backdrop_bromo,
            tint = Color(0xFF5E5238), style = TitleStyle.Wide,
            minutes = 82,
        ),
        // Segera tayang
        Title(
            id = "upeti", name = "Upeti", kind = Kind.Movie, year = 2026, rating = "18+",
            genres = setOf(Genre.Horror),
            moods = listOf("Horor", "Folk", "Ritual"),
            synopsis = "Desa di lereng Merapi punya satu aturan: setiap tujuh tahun, satu keluarga memberikan upeti. Tahun ini, keluarga yang terpilih menolak.",
            cast = listOf("Putri Anjani", "Arya Pradana"), creator = "Wulan Pratiwi",
            poster = R.drawable.poster_upeti, backdrop = R.drawable.backdrop_upeti,
            tint = Color(0xFF6E1414), style = TitleStyle.Condensed,
            minutes = 101, original = true, premiere = LocalDate.of(2026, 10, 31),
        ),
        Title(
            id = "langit_ketujuh", name = "Langit Ketujuh", kind = Kind.Movie, year = 2026, rating = "13+",
            genres = setOf(Genre.SciFi, Genre.Drama),
            moods = listOf("Fiksi Ilmiah", "Epik", "Visual Memukau"),
            synopsis = "Astronom di Observatorium Bosscha menemukan bintang yang padam tepat sesuai catatan kakeknya, ditulis tahun 1965. Catatan berikutnya menyebut Bumi.",
            cast = listOf("Kirana Larasati", "Dimas Arditya"), creator = "Andre Kusuma",
            poster = R.drawable.poster_langit_ketujuh, backdrop = R.drawable.backdrop_langit_ketujuh,
            tint = Color(0xFF4F1A7A), style = TitleStyle.Wide,
            minutes = 121, original = true, premiere = LocalDate.of(2026, 11, 7),
        ),
        Title(
            id = "payung_merah", name = "Payung Merah", kind = Kind.Movie, year = 2026, rating = "13+",
            genres = setOf(Genre.Romance, Genre.Drama),
            moods = listOf("Romantis", "Musim Hujan", "Drama"),
            synopsis = "Setiap kali hujan turun di Bogor, pemilik toko payung meminjamkan satu payung merah kepada orang asing. Hanya satu orang yang selalu mengembalikannya.",
            cast = listOf("Alya Maheswari", "Bagas Prakoso"), creator = "Kinan Hapsari",
            poster = R.drawable.poster_payung_merah, backdrop = R.drawable.backdrop_payung_merah,
            tint = Color(0xFF8A1717), style = TitleStyle.SerifItalic,
            minutes = 103, premiere = LocalDate.of(2026, 11, 14),
        ),
        Title(
            id = "pulang", name = "Pulang", kind = Kind.Movie, year = 2026, rating = "13+",
            genres = setOf(Genre.Drama),
            moods = listOf("Drama", "Keluarga", "Mengharukan"),
            synopsis = "Setelah dua belas tahun bekerja di luar negeri, Galih pulang ke kampung halaman yang tak lagi mengenalnya, termasuk putrinya sendiri.",
            cast = listOf("Gilang Saputro", "Dewi Larasati"), creator = "Rendra Halim",
            poster = R.drawable.poster_pulang, backdrop = R.drawable.backdrop_pulang,
            tint = Color(0xFF55504A), style = TitleStyle.Serif,
            minutes = 114, premiere = LocalDate.of(2026, 11, 21),
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

    val continueWatching = listOf(
        WatchProgress("garis_api", 0.68f, 38),
    )

    val notices = listOf(
        Notice("sekolah_normal", "Baru Ditambahkan", "Sekolah Normal, 4 episode sudah tersedia.", "Hari ini"),
        Notice("upeti", "Segera Tayang", "Upeti tayang 31 Oktober. Mau diingatkan?", "1 hari"),
    )
}
