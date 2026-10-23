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
            id = "garis_api", name = "Garis Api", kind = Kind.Movie, year = 2026, rating = "18+",
            genres = setOf(Genre.Action, Genre.Thriller),
            moods = listOf("Menegangkan", "Penuh Aksi", "Balas Dendam"),
            synopsis = "Mantan pemadam kebakaran Laras menemukan bahwa kebakaran yang menewaskan adiknya disengaja. Satu malam, satu kota, dan semua orang yang ia percaya punya alasan untuk menyalakan korek.",
            cast = listOf("Laras Wibisono", "Arya Pradana", "Gilang Saputro"), creator = "Rendra Halim",
            poster = R.drawable.poster_garis_api, backdrop = R.drawable.backdrop_garis_api,
            tint = Color(0xFFB4410F), style = TitleStyle.Condensed,
            minutes = 118, original = true, top10 = 1,
        ),
        Title(
            id = "kota_tanpa_tidur", name = "Kota Tanpa Tidur", kind = Kind.Series, year = 2025, rating = "18+",
            genres = setOf(Genre.Crime, Genre.Thriller, Genre.Drama),
            moods = listOf("Gelap", "Kriminal", "Slow-burn"),
            synopsis = "Penyidik muda Raka Sasmita ditugaskan ke unit malam Jakarta, tempat kasus tak pernah selesai sebelum subuh. Setiap petunjuk membawanya lebih dekat ke jaringan yang menggaji atasannya sendiri.",
            cast = listOf("Bima Satria", "Nadia Kirana", "Hendra Lubis"), creator = "Yosef Anggara",
            poster = R.drawable.poster_kota_tanpa_tidur, backdrop = R.drawable.backdrop_kota_tanpa_tidur,
            tint = Color(0xFF1D3B8F), style = TitleStyle.Condensed,
            original = true, top10 = 2, newEpisodes = true,
            seasons = listOf(
                Season(
                    1, listOf(
                        ep(1, "Unit Malam", 52, "Hari pertama Raka di unit malam berakhir dengan mayat di kolong jembatan Semanggi dan saksi yang menolak menyebut namanya.", R.drawable.still_84),
                        ep(2, "Hujan di Kaca", 48, "Rekaman CCTV sebuah minimarket memperlihatkan korban masih hidup dua jam setelah waktu kematiannya.", R.drawable.still_171),
                        ep(3, "Jalan Tol", 50, "Nadia menemukan pola di laporan kecelakaan tol dalam kota. Atasan Raka meminta kasus itu ditutup sebelum Jumat.", R.drawable.still_220),
                        ep(4, "Tangga Darurat", 47, "Pengejaran di rusun Tanah Abang memaksa Raka memilih antara tersangka dan rekannya sendiri.", R.drawable.still_221),
                        ep(5, "Lampu Jalan", 51, "Seorang informan lama muncul dengan harga yang terlalu tinggi dan informasi yang terlalu tepat.", R.drawable.still_232),
                        ep(6, "Subuh", 58, "Penggerebekan yang direncanakan berbulan-bulan bocor satu jam sebelum dimulai. Seseorang di unit malam bekerja untuk pihak lain.", R.drawable.still_242),
                    )
                ),
                Season(
                    2, listOf(
                        ep(1, "Musim Baru", 54, "Enam bulan setelah penggerebekan gagal, Raka dipindahkan ke bagian arsip. Sebuah berkas lama memanggilnya kembali.", R.drawable.still_351),
                        ep(2, "Langit Merah", 49, "Kebakaran gudang di Cilincing menghapus satu-satunya bukti fisik. Nadia curiga apinya dinyalakan dari dalam.", R.drawable.still_857),
                        ep(3, "Garis Tepi", 52, "Raka menyusup ke pesta seorang pengusaha dan menemukan wajah yang seharusnya sudah dimakamkan.", R.drawable.still_1078),
                        ep(4, "Kota yang Terjaga", 61, "Semua jalur kasus bertemu di satu gedung. Malam ini, unit malam harus memilih siapa yang masih bisa dipercaya.", R.drawable.still_1047),
                    )
                ),
            ),
        ),
        Title(
            id = "rumah_13", name = "Rumah Nomor 13", kind = Kind.Series, year = 2026, rating = "18+",
            genres = setOf(Genre.Horror, Genre.Mystery),
            moods = listOf("Horor", "Misteri", "Keluarga"),
            synopsis = "Keluarga Wirawan membeli rumah tua di Bandung dengan harga yang terlalu murah. Si bungsu mulai menggambar orang-orang yang tak pernah ia temui, dan semuanya pernah tinggal di sana.",
            cast = listOf("Putri Anjani", "Reza Mahendra", "Alya Maheswari"), creator = "Wulan Pratiwi",
            poster = R.drawable.poster_rumah_13, backdrop = R.drawable.backdrop_rumah_13,
            tint = Color(0xFF1F4A2C), style = TitleStyle.Serif,
            original = true, top10 = 3,
            seasons = listOf(
                Season(
                    1, listOf(
                        ep(1, "Harga Miring", 46, "Keluarga Wirawan pindah ke rumah peninggalan Belanda di Dago. Agen properti menolak masuk melewati pagar.", R.drawable.still_556),
                        ep(2, "Gambar Ketiga", 44, "Laila, si bungsu, menggambar seorang perempuan berkebaya di setiap halaman buku sekolahnya.", R.drawable.still_1008),
                        ep(3, "Lorong", 49, "Ayah menemukan pintu yang tidak ada di denah rumah. Di baliknya, foto-foto keluarga yang tidak ia kenal.", R.drawable.still_546),
                        ep(4, "Anak Tangga", 45, "Ibu mulai terbangun di tangga setiap pukul tiga pagi, menghitung anak tangga yang jumlahnya selalu berbeda.", R.drawable.still_276),
                        ep(5, "Penghuni Lama", 50, "Mantan pembantu rumah itu bersedia bicara, dengan syarat keluarga Wirawan pergi sebelum purnama.", R.drawable.still_1054),
                        ep(6, "Rumah Nomor 13", 57, "Rumah itu tidak ingin mereka pergi. Laila sudah tahu sejak hari pertama.", R.drawable.still_782),
                    )
                ),
            ),
        ),
        Title(
            id = "sunyi_di_lembah", name = "Sunyi di Lembah", kind = Kind.Movie, year = 2024, rating = "18+",
            genres = setOf(Genre.Horror),
            moods = listOf("Horor", "Atmosferik", "Folk"),
            synopsis = "Lima mahasiswa KKN tiba di desa yang warganya tak pernah bicara setelah matahari terbenam. Di malam ketiga, salah satu dari mereka mulai menjawab panggilan yang tak terdengar oleh yang lain.",
            cast = listOf("Sekar Ayu", "Reza Mahendra", "Dewi Larasati"), creator = "Wulan Pratiwi",
            poster = R.drawable.poster_sunyi_di_lembah, backdrop = R.drawable.backdrop_sunyi_di_lembah,
            tint = Color(0xFF7A5424), style = TitleStyle.Serif,
            minutes = 104, top10 = 4,
        ),
        Title(
            id = "orbit_terakhir", name = "Orbit Terakhir", kind = Kind.Movie, year = 2026, rating = "13+",
            genres = setOf(Genre.SciFi, Genre.Thriller),
            moods = listOf("Fiksi Ilmiah", "Menegangkan", "Visual Memukau"),
            synopsis = "Saat stasiun riset orbit milik Indonesia kehilangan kontak dengan Bumi, satu-satunya insinyur yang tersisa harus memilih: memperbaiki antena, atau menyelamatkan kru yang mungkin sudah tidak ada.",
            cast = listOf("Dimas Arditya", "Kirana Larasati", "Johan Siregar"), creator = "Andre Kusuma",
            poster = R.drawable.poster_orbit_terakhir, backdrop = R.drawable.backdrop_orbit_terakhir,
            tint = Color(0xFF4A1F8F), style = TitleStyle.Wide,
            minutes = 126, original = true, top10 = 5,
        ),
        Title(
            id = "kabut", name = "Kabut", kind = Kind.Series, year = 2025, rating = "16+",
            genres = setOf(Genre.Mystery, Genre.Drama),
            moods = listOf("Misteri", "Psikologis", "Atmosferik"),
            synopsis = "Seorang perempuan terbangun di tepi Danau Toba tanpa ingatan, matanya tertutup kain. Warga desa mengenalnya, dan mereka sepakat bahwa ia sudah meninggal tahun lalu.",
            cast = listOf("Ruth Simanjuntak", "Daniel Hutapea", "Maria Sitorus"), creator = "Lamhot Nainggolan",
            poster = R.drawable.poster_kabut, backdrop = R.drawable.backdrop_kabut,
            tint = Color(0xFF4E6E6B), style = TitleStyle.SerifItalic,
            original = true, top10 = 6,
            seasons = listOf(
                Season(
                    1, listOf(
                        ep(1, "Tepi Danau", 47, "Seorang perempuan terbangun di tepi Danau Toba dengan mata tertutup kain. Warga memanggilnya Ruth.", R.drawable.still_338),
                        ep(2, "Nama yang Salah", 45, "Ruth tidak mengenali satu pun foto di rumah yang katanya miliknya.", R.drawable.still_473),
                        ep(3, "Padang", 48, "Seorang anak menuntun Ruth ke padang di atas desa, tempat ia ditemukan tak bernyawa tahun lalu.", R.drawable.still_291),
                        ep(4, "Dua Wajah", 46, "Saudari kembar Ruth tiba dari Medan dan bersumpah ia tak pernah punya saudari.", R.drawable.still_497),
                        ep(5, "Hutan Pinus", 50, "Jejak di hutan mengarah ke pondok yang menyimpan buku harian dengan tulisan tangan Ruth.", R.drawable.still_1021),
                        ep(6, "Kabut Turun", 56, "Desa menggelar peringatan setahun kematian Ruth, dan Ruth datang.", R.drawable.still_877),
                    )
                ),
            ),
        ),
        Title(
            id = "sinyal", name = "Sinyal", kind = Kind.Series, year = 2026, rating = "13+",
            genres = setOf(Genre.SciFi, Genre.Mystery),
            moods = listOf("Fiksi Ilmiah", "Misteri", "Cerdas"),
            synopsis = "Teknisi radio amatir di Makassar menangkap siaran dari dirinya sendiri, bertanggal tujuh hari ke depan. Siaran itu berisi peringatan, dan daftar nama.",
            cast = listOf("Arif Rahman", "Andi Tenri", "Fadli Hamzah"), creator = "Andre Kusuma",
            poster = R.drawable.poster_sinyal, backdrop = R.drawable.backdrop_sinyal,
            tint = Color(0xFF6B3A0E), style = TitleStyle.Wide,
            original = true, top10 = 7, newEpisodes = true,
            seasons = listOf(
                Season(
                    1, listOf(
                        ep(1, "Frekuensi 7", 49, "Arif menangkap siaran radio dengan suaranya sendiri, menyebut tanggal tujuh hari ke depan.", R.drawable.still_679),
                        ep(2, "Daftar Nama", 47, "Siaran kedua membacakan nama-nama warga Makassar. Nama pertama meninggal keesokan harinya.", R.drawable.still_1042),
                        ep(3, "Jejak Bintang", 50, "Seorang dosen astronomi menjelaskan mengapa sinyal itu datang dari arah yang mustahil.", R.drawable.still_683),
                        ep(4, "Cahaya Utara", 48, "Sinyal yang sama pernah tercatat di stasiun riset Norwegia, tiga puluh tahun lalu.", R.drawable.still_724),
                        ep(5, "Terowongan", 46, "Arif melacak pemancar ke terowongan bekas pabrik. Seseorang sudah menunggunya.", R.drawable.still_209),
                        ep(6, "Hari Ketujuh", 58, "Nama terakhir di daftar adalah nama Arif. Siaran berikutnya hanya berisi hitungan mundur.", R.drawable.still_137),
                    )
                ),
            ),
        ),
        Title(
            id = "tamu_tengah_malam", name = "Tamu Tengah Malam", kind = Kind.Movie, year = 2025, rating = "16+",
            genres = setOf(Genre.Thriller, Genre.Mystery),
            moods = listOf("Thriller", "Psikologis", "Satu Malam"),
            synopsis = "Saat badai memutus listrik di apartemennya, seorang penerjemah membukakan pintu untuk tetangga yang minta berteduh. Masalahnya, unit sebelah sudah kosong sejak bulan lalu.",
            cast = listOf("Kirana Larasati", "Johan Siregar"), creator = "Mira Santoso",
            poster = R.drawable.poster_tamu_tengah_malam, backdrop = R.drawable.backdrop_tamu_tengah_malam,
            tint = Color(0xFF3F4C5E), style = TitleStyle.Serif,
            minutes = 99, top10 = 8,
        ),
        Title(
            id = "lintas_batas", name = "Lintas Batas", kind = Kind.Series, year = 2025, rating = "16+",
            genres = setOf(Genre.Crime, Genre.Action),
            moods = listOf("Perampokan", "Cerdas", "Seru"),
            synopsis = "Seorang montir mobil klasik di Surabaya direkrut untuk satu pekerjaan: mencuri koleksi seorang pejabat sebelum lelang. Masalahnya, separuh timnya adalah penyidik yang menyamar.",
            cast = listOf("Dimas Arditya", "Nadia Kirana", "Bagas Prakoso"), creator = "Yosef Anggara",
            poster = R.drawable.poster_lintas_batas, backdrop = R.drawable.backdrop_lintas_batas,
            tint = Color(0xFF8F6A06), style = TitleStyle.Condensed,
            original = true,
            seasons = listOf(
                Season(
                    1, listOf(
                        ep(1, "Montir", 50, "Dimas memperbaiki Jaguar tua milik tamu misterius yang membayar tunai dan meninggalkan kartu nama tanpa nomor.", R.drawable.still_1033),
                        ep(2, "Tim", 48, "Lima orang, lima keahlian, satu gudang di Tanjung Perak. Tidak ada yang memakai nama asli.", R.drawable.still_331),
                        ep(3, "Rute", 47, "Mereka butuh denah gedung lelang. Satu-satunya salinan ada di kantor yang dijaga lebih ketat dari bank.", R.drawable.still_22),
                        ep(4, "Kereta Malam", 52, "Pengalihan di Stasiun Gubeng berjalan sempurna, kecuali satu penumpang yang mengenali Dimas.", R.drawable.still_345),
                        ep(5, "Penyamar", 49, "Dimas menemukan lencana di jok mobil rekannya. Lelang tinggal dua hari.", R.drawable.still_111),
                        ep(6, "Lelang", 63, "Palu diketuk pukul delapan malam. Pada pukul delapan lewat satu, semua rencana berubah.", R.drawable.still_1071),
                    )
                ),
            ),
        ),
        Title(
            id = "arus_bawah", name = "Arus Bawah", kind = Kind.Series, year = 2026, rating = "16+",
            genres = setOf(Genre.Thriller, Genre.Crime),
            moods = listOf("Thriller", "Menegangkan", "Bawah Laut"),
            synopsis = "Penyelam forensik Maya Lestari menemukan jasad di bangkai kapal yang secara resmi tidak pernah ada. Siapa pun yang mendaftarkan kapal itu ingin ia berhenti menyelam.",
            cast = listOf("Maya Lestari", "Fadli Hamzah", "Hendra Lubis"), creator = "Mira Santoso",
            poster = R.drawable.poster_arus_bawah, backdrop = R.drawable.backdrop_arus_bawah,
            tint = Color(0xFF243447), style = TitleStyle.Condensed,
            original = true, newEpisodes = true,
            seasons = listOf(
                Season(
                    1, listOf(
                        ep(1, "Kedalaman 40", 51, "Maya menemukan jasad di bangkai kapal tanpa nama di perairan Wakatobi. Laporannya hilang dari sistem esok paginya.", R.drawable.still_446),
                        ep(2, "Tanpa Bendera", 48, "Nomor lambung kapal itu milik kapal lain yang masih beroperasi di Surabaya.", R.drawable.still_541),
                        ep(3, "Badai", 50, "Cuaca buruk memaksa tim bertahan di pulau kecil bersama nelayan yang terlalu banyak tahu.", R.drawable.still_149),
                        ep(4, "Batas Napas", 46, "Penyelaman kedua berubah menjadi sabotase. Tabung oksigen Maya hanya terisi setengah.", R.drawable.still_969),
                        ep(5, "Permukaan", 59, "Maya membawa bukti ke darat dan harus memutuskan kepada siapa ia menyerahkannya.", R.drawable.still_1019),
                    )
                ),
            ),
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
        Title(
            id = "rimba", name = "Rimba", kind = Kind.Series, year = 2025, rating = "SU",
            genres = setOf(Genre.Documentary),
            moods = listOf("Dokumenter", "Alam", "Satwa Liar"),
            synopsis = "Serial dokumenter tentang hutan hujan terakhir Sumatra, diceritakan dari sudut pandang penghuninya: harimau, orangutan, dan para jagawana yang menjaga mereka.",
            cast = listOf("Narator: Maria Sitorus"), creator = "Sari Widjaja",
            poster = R.drawable.poster_rimba, backdrop = R.drawable.backdrop_rimba,
            tint = Color(0xFF3C5A16), style = TitleStyle.Wide,
            original = true,
            seasons = listOf(
                Season(
                    1, listOf(
                        ep(1, "Penjaga Hutan", 52, "Tim jagawana Leuser berpatroli di jalur yang sama dengan pemburu liar, hanya berselisih satu hari.", R.drawable.still_944),
                        ep(2, "Air Terjun", 49, "Kemarau mengubah sungai menjadi satu-satunya titik temu bagi semua penghuni hutan.", R.drawable.still_1039),
                        ep(3, "Sungai", 50, "Seekor induk orangutan mengajari anaknya menyeberang sungai untuk pertama kali.", R.drawable.still_466),
                        ep(4, "Kanopi", 55, "Di puncak pohon setinggi 60 meter, kehidupan berjalan tanpa pernah menyentuh tanah.", R.drawable.still_1023),
                    )
                ),
            ),
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
        WatchProgress("kota_tanpa_tidur", 0.42f, 30, season = 2, episode = 3),
        WatchProgress("garis_api", 0.68f, 38),
        WatchProgress("kabut", 0.15f, 39, episode = 4),
        WatchProgress("sinyal", 0.33f, 33, episode = 1),
        WatchProgress("rimba", 0.8f, 10, episode = 2),
    )

    val notices = listOf(
        Notice("kota_tanpa_tidur", "Episode Baru", "Kota Tanpa Tidur musim 2 episode 4 sudah tayang.", "2 jam"),
        Notice("upeti", "Segera Tayang", "Upeti tayang 31 Oktober. Mau diingatkan?", "1 hari"),
        Notice("arus_bawah", "Baru Ditambahkan", "Arus Bawah, thriller bawah laut dari Wakatobi.", "3 hari"),
    )
}
