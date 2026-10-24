package com.xflix.app.data

import androidx.compose.ui.graphics.Color

/** Playlist PLLQoIst01CaXoNrLogHiHncjpxvmb7bbQ */
internal object PzdCatalog {
    const val ID = "pzd-pada-zaman-dahulu"

    private fun episode(number: Int, id: String, name: String) = Episode(
        number = number,
        name = "Episode $number - $name",
        minutes = 0,
        synopsis = "",
        still = 0,
        remoteId = id,
        imageUrl = "https://i.ytimg.com/vi/$id/hqdefault.jpg",
    )

    val title = Title(
        id = ID,
        name = "Pada Zaman Dahulu (PZD)",
        kind = Kind.Series,
        year = 2023,
        rating = "SU",
        genres = setOf(Genre.Comedy),
        moods = listOf("PZD", "animasi", "anak", "cerita rakyat", "YouTube"),
        synopsis = "Sang Kancil dan cerita rakyat lainnya dalam enam musim dari playlist Pada Zaman Dahulu.",
        cast = emptyList(),
        creator = "Ardi Kusnadi (YouTube)",
        poster = 0,
        backdrop = 0,
        tint = Color(0xFF244A36),
        style = TitleStyle.Condensed,
        seasons = listOf(
            Season(1, listOf(
                episode(1, "YU-Rit_VN04", "Sang Kancil & Kerbau"),
                episode(2, "EXM0uTrM4WY", "Sang Kancil Mengira Buaya"),
                episode(3, "XoCULOzgqE4", "Sang Kancil & Perigi Buta"),
                episode(4, "7wWAjrqevNs", "Sang Kancil & Gergasi"),
            )),
            Season(2, listOf(
                episode(1, "AY6G0h25Iy4", "Sang Kancil & Tali Pinggang Hikmat"),
                episode(2, "BtU4PrFXY7Y", "Sang Kancil & Pemburu"),
                episode(3, "02d1bfnXUzg", "Sang Kancil Berlumba dengan Siput"),
                episode(4, "HKsrY12gYH4", "Sang Kancil & Harimau"),
                episode(5, "Zm-2LOC6woU", "Sang Kancil & Gong Raja Sulaiman"),
                episode(6, "HpQwJrFsiv4", "Sang Kancil Menjadi Hakim"),
                episode(7, "I6kXOQtAnZY", "Kura kura dan Monyet"),
                episode(8, "9xxG0X_o5oU", "Sang Kancil & Penghuni Hutan Menentang Gergasi"),
            )),
            Season(3, listOf(
                episode(1, "L-cN7zezrX8", "Sang Kancil & Singa"),
                episode(2, "UbkTHbqiyMU", "Ayam dan Helang"),
                episode(3, "_mZMhL5uB7Q", "Pekasam Mata Harimau"),
                episode(4, "OLaIpdNtCFc", "Semut, Merpati dan Gajah"),
                episode(5, "gL9poZX6c9Y", "Musang dan Ayam"),
                episode(6, "GsyT-rKqS4k", "Belalang & Semut"),
                episode(7, "bzd9hA5979Y", "Kisah Bangau"),
                episode(8, "VzoA4NJpV3U", "Anjing dan Bayang Bayang"),
                episode(9, "Exv4Vv5iSvU", "Kambing dan Buaya"),
                episode(10, "UUPb7LMO0aQ", "Cawi dan Harimau"),
                episode(11, "0Vw5s5DLMY8", "Kura kura dan Itik"),
                episode(12, "aLJYQY8trl8", "Singa dan Tikus"),
            )),
            Season(4, listOf(
                episode(1, "_85jJhRpbuo", "Monyet Raja Rimba"),
                episode(2, "KTYDIwKIBWs", "Jangan Berkawan Dengan Kera"),
                episode(3, "fdb1iW99JyU", "Sang Kancil dan Serigala"),
                episode(4, "T_au6ZXVrlY", "Sereban Tidak Sebulu"),
                episode(5, "UDL4Yii6AVU", "Tempua Bersarang Rendah"),
                episode(6, "2N1p-5cJoS0", "Gagak dan Kendi"),
                episode(7, "IlljG4wpJQM", "Puyuh dan Tempua"),
                episode(8, "XK-0Aem_CWk", "Monyet yang nakal"),
                episode(9, "r7iLhQGpUQM", "Kisah Duri Landak"),
                episode(10, "oh-vWPV6C6o", "Makan Telur Busuk"),
                episode(11, "p_EwrJr7ztA", "Kenapa Kerengga Pinggang Ramping"),
                episode(12, "VSZOxey5_Ik", "Kawan Biar Seribu"),
            )),
            Season(5, listOf(
                episode(1, "dTU3zmrkekc", "Katak Bawah Tempurung"),
                episode(2, "48XTY_8WYzU", "Ayam dan Semut"),
                episode(3, "M0rtzkxJcv4", "Kenapa Bangau Kurus"),
                episode(4, "ZMuyo7UjWgU", "Hadiah Duri Landak"),
                episode(5, "KacATjjgmCM", "Ayam Jantan dan Merpati"),
                episode(6, "S1lXEF9CWH0", "Rusa dan Tanduknya"),
                episode(7, "hHgfvL93Cv8", "Beruang dan Lebah"),
                episode(8, "aIMUjYt9T6M", "Kucing dan Harimau"),
                episode(9, "MXDelBFZngs", "Musang yang Bodoh Sombong"),
                episode(10, "cVl-lj3fmE4", "Asal Mula Kelelawar Keluar Malam"),
                episode(11, "ySKQPXjHqZ0", "Legenda Cengkerang Siput"),
                episode(12, "cDsfrdhGtR4", "Singa Dan Serigala"),
            )),
            Season(6, listOf(
                episode(1, "eh6zpJ4PtTI", "Tikus Desa dan Tikus Bandar"),
                episode(2, "3JxMkj2ynok", "Sang Gagak dan Merak"),
                episode(3, "x_Po0gObJ-o", "Belalang dan Burung Hantu"),
                episode(4, "SSJUmq46xTU", "Semut dan Rama-Rama"),
            )),
        ),
        imageUrl = "https://i.ytimg.com/vi/YU-Rit_VN04/hqdefault.jpg",
        remoteSource = "youtube",
    )
}
