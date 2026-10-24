<div align="center">

<img src="https://capsule-render.vercel.app/api?type=waving&color=0:FF7A45,100:E50914&height=200&section=header&text=Xflix&fontColor=ffffff&fontSize=56&fontAlignY=36&desc=Streaming%20Video%20Gratis%20di%20Android&descSize=18&descAlignY=58" alt="Xflix — Streaming Video Gratis di Android" width="100%" />

<img src="https://readme-typing-svg.demolab.com?font=Fira+Code&size=18&duration=3000&pause=1000&color=E50914&center=true&vCenter=true&width=640&lines=Nonton+gratis%2C+tanpa+langganan;Ringan%2C+cepat%2C+dan+bersih;Pasang%2C+pilih%2C+langsung+tonton" alt="Nonton gratis, tanpa langganan" />

<a href="https://github.com/mrizky-fr/xflix-apps/raw/main/release/Xflix-Novaplay.apk"><img src="https://img.shields.io/badge/Download%20%26%20Install%20Now-APK%201.0.7-E50914?style=for-the-badge" alt="Download & Install Now" height="42" /></a>

<sub>Android 8.0+ · ±4,6 MB · Gratis</sub>

</div>

## Xflix - Novaplay

Mempersembahkan **Xflix**, aplikasi streaming video **gratis** untuk Android. Tidak perlu langganan, tidak perlu kartu kredit. Cukup pasang, pilih profil, dan tontonan favoritmu langsung tersaji.

Setiap judul hadir dalam tampilan sinematis yang bersih: poster besar, deretan Top 10 hari ini, dan pemutar layar penuh yang hanya menampilkan apa yang ingin kamu lihat.

- **Gratis**, tanpa langganan dan tanpa akun berbayar
- **Selalu segar**, katalog diperbarui otomatis
- **Pemutar layar penuh yang bersih**, tanpa tombol berantakan
- **Ringan**, hanya ±4,6 MB karena video diputar langsung secara streaming

## Pengalaman Menonton

<table>
  <tr>
    <td width="50%" valign="top">
      <h3>Beranda sinematis</h3>
      Hero besar, Top 10 hari ini, dan rak tontonan per kategori. Pilihan berikutnya selalu ada di depan mata.
    </td>
    <td width="50%" valign="top">
      <h3>Pemutar yang tenang</h3>
      Kontrol muncul saat dibutuhkan dan menghilang saat cerita dimulai. Yang tersisa hanya kamu dan tontonanmu.
    </td>
  </tr>
  <tr>
    <td width="50%" valign="top">
      <h3>Episode tanpa jeda</h3>
      Lanjut ke episode berikutnya atau lompat ke episode mana pun tanpa keluar dari layar tonton.
    </td>
    <td width="50%" valign="top">
      <h3>Banyak server</h3>
      Satu sumber sedang lambat? Pindah server dan tontonan berlanjut dari detik yang sama.
    </td>
  </tr>
  <tr>
    <td width="50%" valign="top">
      <h3>Kunci layar</h3>
      Tonton sambil rebahan tanpa takut tersentuh tidak sengaja.
    </td>
    <td width="50%" valign="top">
      <h3>Profil keluarga</h3>
      Setiap anggota keluarga punya profilnya sendiri, termasuk profil khusus anak-anak.
    </td>
  </tr>
</table>

## Install Sekarang

<div align="center">

<a href="https://github.com/mrizky-fr/xflix-apps/raw/main/release/Xflix-Novaplay.apk"><img src="https://img.shields.io/badge/Download%20%26%20Install%20Now-APK%201.0.7-E50914?style=for-the-badge" alt="Download & Install Now" height="42" /></a>

</div>

1. Unduh APK lewat tombol di atas.
2. Buka file yang terunduh. Bila diminta, izinkan pemasangan dari sumber ini.
3. Pasang, buka **Xflix - Novaplay**, dan selamat menonton.

## Dibangun Dengan

<p align="center">
  <img src="https://skillicons.dev/icons?i=kotlin" alt="Kotlin" title="Kotlin" width="48" height="48" />&nbsp;
  <img src="https://skillicons.dev/icons?i=androidstudio" alt="Android Studio" title="Android Studio" width="48" height="48" />&nbsp;
  <img src="https://skillicons.dev/icons?i=gradle" alt="Gradle" title="Gradle" width="48" height="48" />&nbsp;
  <img src="https://skillicons.dev/icons?i=js" alt="JavaScript" title="JavaScript" width="48" height="48" />
</p>

<p align="center"><sub>Kotlin · Jetpack Compose · Media3 · WebView · Katalog dari <a href="https://github.com/Lukman0521/nova-play">NOVA Play</a></sub></p>

<details>
<summary><b>Untuk developer</b></summary>

<br>

Build dengan JDK bawaan Android Studio:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:assembleRelease
```

APK rilis dibuat di `app/build/outputs/apk/release/`, lalu disalin ke [`release/Xflix-Novaplay.apk`](release/Xflix-Novaplay.apk) untuk tombol unduh. Signing memakai `keystore.properties` (lihat [`keystore.properties.example`](keystore.properties.example)) atau env `XFLIX_KEYSTORE_FILE`, `XFLIX_KEYSTORE_PASSWORD`, `XFLIX_KEY_ALIAS`, `XFLIX_KEY_PASSWORD`.

Katalog YouTube ada di [`youtube_catalog.json`](app/src/main/resources/youtube_catalog.json) dan dibuat ulang dengan `python tools/youtube_catalog.py`. Embed sumber lain dikendalikan oleh [`player_bridge.js`](app/src/main/assets/player_bridge.js).

</details>

## Catatan

Xflix tidak menyimpan atau meng-hosting video. Semua tontonan diputar dari sumber pihak ketiga, sehingga ketersediaan judul, iklan, dan subtitle mengikuti sumbernya.

## Pembuat

Dibuat oleh **M Rizky** (Kyy).

[![GitHub](https://img.shields.io/badge/GitHub-mrizky--fr-0f172a?style=for-the-badge)](https://github.com/mrizky-fr)
[![Repo](https://img.shields.io/badge/Repo-xflix--apps-E50914?style=for-the-badge)](https://github.com/mrizky-fr/xflix-apps)

<img src="https://capsule-render.vercel.app/api?type=waving&color=0:E50914,100:FF7A45&height=120&section=footer" alt="" width="100%" />
