# Xflix

Aplikasi Android ini mengambil katalog dan pemutar dari API publik [NOVA Play](https://github.com/Lukman0521/nova-play). Base URL produksi dikonfigurasi di `NovaApi.BASE` dalam `app/src/main/java/com/xflix/app/data/Catalog.kt`.

- Katalog: `GET /api/catalog`
- Film/serial: `GET /api/playback/{id}` dengan `?episode={id}` untuk serial
- Anime/donghua/dracin: `GET /api/anime/{id}` dan `GET /api/anime/{id}/{episode}`

APK tidak membundel file video. Embed ditampilkan dalam WebView; URL MP4/HLS langsung diputar dengan Media3. Ketersediaan video, iklan, dan subtitle mengikuti sumber yang disediakan NOVA Play.

Build dengan JDK bawaan Android Studio:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:assembleRelease
```

Gradle membuat APK rilis di `app/build/outputs/apk/release/`. APK yang siap dibagikan ditempatkan di `../dist/Xflix-1.0.1.apk`.
