package com.xflix.app.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.xflix.app.R
import java.time.LocalDate

enum class Kind { Movie, Series }

// Gaya tipografi judul di poster
enum class TitleStyle { Condensed, Serif, SerifItalic, Wide }

enum class Genre(@param:StringRes val label: Int) {
    Action(R.string.genre_action),
    Horror(R.string.genre_horror),
    Thriller(R.string.genre_thriller),
    Romance(R.string.genre_romance),
    SciFi(R.string.genre_scifi),
    Documentary(R.string.genre_documentary),
    Mystery(R.string.genre_mystery),
    Drama(R.string.genre_drama),
    Crime(R.string.genre_crime),
    Comedy(R.string.genre_comedy),
}

@Immutable
data class Episode(
    val number: Int,
    val name: String,
    val minutes: Int,
    val synopsis: String,
    @param:DrawableRes val still: Int,
    val remoteId: String? = null,
    val imageUrl: String? = null,
)

@Immutable
data class Season(val number: Int, val episodes: List<Episode>)

@Immutable
data class Title(
    val id: String,
    val name: String,
    val kind: Kind,
    val year: Int,
    val rating: String,
    val genres: Set<Genre>,
    val moods: List<String>,
    val synopsis: String,
    val cast: List<String>,
    val creator: String,
    @param:DrawableRes val poster: Int,
    @param:DrawableRes val backdrop: Int,
    val tint: Color,
    val style: TitleStyle,
    val minutes: Int = 0,
    val seasons: List<Season> = emptyList(),
    val original: Boolean = false,
    val top10: Int? = null,
    val newEpisodes: Boolean = false,
    val premiere: LocalDate? = null,
    val imageUrl: String? = null,
    val remoteSource: String? = null,
) {
    val isUpcoming: Boolean get() = premiere != null
    val streaming: Boolean get() = remoteSource != null
}

enum class Face { Smile, Grin, Wink, Calm, Kids }

@Immutable
data class Profile(val id: String, val name: String, val color: Color, val face: Face)

@Immutable
data class WatchProgress(
    val titleId: String,
    val fraction: Float,
    val minutesLeft: Int,
    val season: Int = 1,
    val episode: Int = 1,
)

@Immutable
data class Notice(val titleId: String, val headline: String, val body: String, val age: String)
