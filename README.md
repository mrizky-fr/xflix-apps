<div align="center">

<img src="https://capsule-render.vercel.app/api?type=waving&color=0:FF7A45,100:E50914&height=200&section=header&text=Xflix&fontColor=ffffff&fontSize=56&fontAlignY=36&desc=Streaming%20Film%2C%20Serial%20dan%20Anime%20%C2%B7%20NOVA%20Play&descSize=18&descAlignY=58" alt="Xflix — Streaming Film, Serial dan Anime · NOVA Play" width="100%" />

<img src="https://readme-typing-svg.demolab.com?font=Fira+Code&size=18&duration=3000&pause=1000&color=E50914&center=true&vCenter=true&width=640&lines=Film+%C2%B7+Serial+%C2%B7+Anime+%C2%B7+Donghua+%C2%B7+Dracin;Pemutar+lengkap%3A+kunci%2C+kecepatan%2C+episode%2C+server;Kotlin+%C2%B7+Jetpack+Compose+%C2%B7+Media3+%C2%B7+WebView" alt="Film · Serial · Anime · Donghua · Dracin" />

[![Versi](https://img.shields.io/badge/versi-1.0.3-E50914?style=flat-square)](#build)
[![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=flat-square&logo=android&logoColor=white)](#build)
[![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://developer.android.com/compose)
[![NOVA Play](https://img.shields.io/badge/API-NOVA%20Play-141414?style=flat-square&logo=github)](https://github.com/Lukman0521/nova-play)
[![Last commit](https://img.shields.io/github/last-commit/mrizky-fr/xflix-apps?style=flat-square&color=141414&label=update)](https://github.com/mrizky-fr/xflix-apps/commits/main)

</div>

## Tentang

**Xflix - Novaplay** adalah aplikasi streaming Android dengan tampilan sinematis yang bersih. Katalog dan pemutarnya diambil langsung dari API publik [NOVA Play](https://github.com/Lukman0521/nova-play), jadi APK tetap ringan (±3,7 MB) karena tidak membundel satu pun file video.

Fokusnya ada di pengalaman menonton. Apa pun sumber videonya, entah embed pihak ketiga atau MP4/HLS langsung, penonton selalu memakai kontrol yang sama dan rapi milik Xflix, bukan kontrol bawaan masing-masing situs.

- 🎬 Katalog film, serial, anime, donghua, dan dracin langsung dari NOVA Play
- 🏠 Beranda sinematis: hero, Top 10, filter Serial · Film · Anime · Donghua, dan kategori
- 🔎 Pencarian, Baru & Populer, Daftar Saya, serta pilihan profil penonton
- ▶️ Pemutar landscape layar penuh dengan kontrol Xflix untuk semua sumber
- 🔒 Kunci layar, kecepatan putar, maju/mundur 10 detik, dan ketuk dua kali untuk seek
- 📺 Ganti episode dan server tanpa keluar dari pemutar, lanjut di detik yang sama

## Pemutar

Pemutar dirancang supaya nyaman dipakai: satu ketukan hanya memunculkan atau menyembunyikan kontrol, dan video baru berhenti kalau tombol tengah ditekan.

| Aksi | Hasil |
| --- | --- |
| **Ketuk sekali** | Tampilkan / sembunyikan kontrol, video tetap berjalan |
| **Tombol tengah** | Jeda atau putar |
| **Ketuk dua kali** kiri / kanan | Mundur / maju 10 detik |
| **Geser bilah merah** | Lompat ke menit tertentu, area abu-abu menandai buffer |
| **Kunci** | Sentuhan dan tombol Kembali diabaikan sampai kunci dibuka |
| **Kecepatan** | 0.5x · 0.75x · 1x · 1.25x · 1.5x |
| **Episode** / **Episode Berikutnya** | Pilih episode dari panel; urutan daftar yang menurun ikut terdeteksi |
| **Server** | Pindah sumber dan lanjut dari detik terakhir |

Kontrol hilang sendiri setelah 3,5 detik saat video berjalan dan tetap tampil saat dijeda.

### Cara kerja embed

Sebagian besar sumber NOVA Play berupa embed (Vidhide, Dailymotion, Morencius, YouTube) yang punya kontrolnya sendiri. Agar tidak ada dua lapis kontrol yang bentrok:

1. [`player_bridge.js`](app/src/main/assets/player_bridge.js) disuntikkan ke semua frame lewat `androidx.webkit` (`addDocumentStartJavaScript` + `addWebMessageListener`).
2. Skrip memilih elemen `<video>` utama, melaporkan posisi, durasi, buffer, dan status putar ke aplikasi, lalu menjalankan perintah putar, jeda, seek, dan kecepatan.
3. Saat video mulai, UI bawaan sumber dan popup iklan disembunyikan. Yang tersisa hanya video dan subtitle-nya.
4. Lapisan sentuh Compose menutup WebView, jadi semua ketukan ditangani Xflix.

Bila sumber tidak bisa dikendalikan dalam 10 detik (25 detik jika pemutarnya sudah terdeteksi), layar beralih ke kontrol bawaan sumber. Bar atas berisi Kembali, Server, dan Episode tetap tersedia, dan kontrol Xflix aktif lagi begitu video berjalan.

## Tech Stack

<p align="center">
  <img src="https://skillicons.dev/icons?i=kotlin" alt="Kotlin" title="Kotlin" width="48" height="48" />&nbsp;
  <img src="https://skillicons.dev/icons?i=androidstudio" alt="Android Studio" title="Android Studio" width="48" height="48" />&nbsp;
  <img src="https://skillicons.dev/icons?i=gradle" alt="Gradle" title="Gradle" width="48" height="48" />&nbsp;
  <img src="https://skillicons.dev/icons?i=js" alt="JavaScript" title="JavaScript" width="48" height="48" />
</p>

| Bagian | Teknologi |
| --- | --- |
| **UI** | Jetpack Compose, Material 3, Navigation Compose, font Inter |
| **Pemutar native** | Media3 ExoPlayer + HLS untuk URL MP4/HLS langsung |
| **Pemutar embed** | WebView + `androidx.webkit` dengan jembatan JavaScript |
| **Data** | `HttpURLConnection` + `org.json` ke API NOVA Play |
| **Build** | Gradle, Android SDK 36, minSdk 26, R8 untuk rilis |

## Sumber Data

Base URL produksi ada di `NovaApi.BASE` dalam [`Catalog.kt`](app/src/main/java/com/xflix/app/data/Catalog.kt).

| Endpoint | Kegunaan |
| --- | --- |
| `GET /api/catalog` | Katalog utama; Anime dan Donghua dipisah menurut field `kind` |
| `GET /api/playback/{id}` | Film dan serial, tambah `?episode={id}` untuk episode serial |
| `GET /api/anime/{id}` | Detail dan daftar episode anime, donghua, dan dracin |
| `GET /api/anime/{id}/{episode}` | Daftar server untuk satu episode |

## Struktur Proyek

| Path | Isi |
| --- | --- |
| [`data/`](app/src/main/java/com/xflix/app/data) | Klien API NOVA Play dan model katalog |
| [`ui/home`](app/src/main/java/com/xflix/app/ui/home), [`detail`](app/src/main/java/com/xflix/app/ui/detail), [`search`](app/src/main/java/com/xflix/app/ui/search), [`newhot`](app/src/main/java/com/xflix/app/ui/newhot), [`my`](app/src/main/java/com/xflix/app/ui/my), [`profiles`](app/src/main/java/com/xflix/app/ui/profiles) | Layar-layar aplikasi |
| [`ui/player/PlayerScreen.kt`](app/src/main/java/com/xflix/app/ui/player/PlayerScreen.kt) | Layar pemutar: gesture, kunci, auto-hide, panel episode & server |
| [`ui/player/PlayerControls.kt`](app/src/main/java/com/xflix/app/ui/player/PlayerControls.kt) | Komponen kontrol pemutar |
| [`ui/player/StreamEngine.kt`](app/src/main/java/com/xflix/app/ui/player/StreamEngine.kt) | Mesin Media3 dan WebView beserta jembatannya |
| [`assets/player_bridge.js`](app/src/main/assets/player_bridge.js) | Skrip pengendali video di dalam iframe |
| [`ui/theme`](app/src/main/java/com/xflix/app/ui/theme) | Warna, tipografi, dan motion |

## Build

Gunakan JDK bawaan Android Studio:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:assembleDebug     # APK debug (com.xflix.app.debug)
.\gradlew.bat :app:assembleRelease   # APK rilis
```

APK rilis dibuat di `app/build/outputs/apk/release/`. Versi yang siap dibagikan ditempatkan di `../dist/Xflix-1.0.3.apk`.

Untuk menandatangani APK rilis, salin [`keystore.properties.example`](keystore.properties.example) menjadi `keystore.properties` lalu isi password-nya, atau set env `XFLIX_KEYSTORE_FILE`, `XFLIX_KEYSTORE_PASSWORD`, `XFLIX_KEY_ALIAS`, dan `XFLIX_KEY_PASSWORD`. File keystore dan `keystore.properties` sudah di-gitignore. Gunakan keystore yang sama di setiap rilis supaya aplikasi yang sudah terpasang bisa di-update.

## Catatan

- Xflix tidak menyimpan atau meng-hosting video. Ketersediaan video, iklan, dan subtitle mengikuti sumber yang disediakan NOVA Play.
- Iklan pre-roll yang diputar di dalam video embed tetap tampil karena bagian dari sumbernya.

## Pembuat

Dibuat oleh **M Rizky** (Kyy).

[![GitHub](https://img.shields.io/badge/GitHub-mrizky--fr-0f172a?style=for-the-badge&logo=github&logoColor=white)](https://github.com/mrizky-fr)
[![Repo](https://img.shields.io/badge/Repo-xflix--apps-E50914?style=for-the-badge&logo=github&logoColor=white)](https://github.com/mrizky-fr/xflix-apps)

<img src="https://capsule-render.vercel.app/api?type=waving&color=0:E50914,100:FF7A45&height=120&section=footer" alt="" width="100%" />
