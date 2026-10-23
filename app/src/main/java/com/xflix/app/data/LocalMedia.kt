package com.xflix.app.data

import android.content.Context
import android.net.Uri
import java.util.Locale

/** Video ikut di dalam APK */
object LocalMedia {

    private const val ROOT = "media"

    fun fileName(title: Title, season: Int, episode: Int): String =
        if (title.kind == Kind.Series) String.format(Locale.ROOT, "s%02de%02d.mp4", season, episode) else "film.mp4"

    fun find(context: Context, title: Title, season: Int, episode: Int): Uri? {
        val name = fileName(title, season, episode)
        val bundled = context.assets.list("$ROOT/${title.id}").orEmpty()
        return if (name in bundled) Uri.parse("asset:///$ROOT/${title.id}/$name") else null
    }
}
