package com.xflix.app.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

// Grid 24, stroke 1.9
private fun outline(name: String, vararg paths: String): ImageVector =
    ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
        paths.forEach { d ->
            addPath(
                pathData = addPathNodes(d),
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }.build()

private fun solid(name: String, vararg paths: String): ImageVector =
    ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
        paths.forEach { d ->
            addPath(
                pathData = addPathNodes(d),
                pathFillType = PathFillType.EvenOdd,
                fill = SolidColor(Color.White),
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.2f,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }.build()

object XIcons {
    val Home = outline(
        "home",
        "M15 21v-7a1 1 0 0 0-1-1h-4a1 1 0 0 0-1 1v7",
        "M3 10a2 2 0 0 1 0.709-1.528l7-5.999a2 2 0 0 1 2.582 0l7 5.999A2 2 0 0 1 21 10v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z",
    )
    val HomeFilled = solid(
        "home_filled",
        "M3 10a2 2 0 0 1 0.709-1.528l7-5.999a2 2 0 0 1 2.582 0l7 5.999A2 2 0 0 1 21 10v9a2 2 0 0 1-2 2h-5v-6a1 1 0 0 0-1-1h-2a1 1 0 0 0-1 1v6H5a2 2 0 0 1-2-2z",
    )
    val NewHot = outline(
        "new_hot",
        "M4 5h16a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2z",
        "M10 9.2v5.6l4.6-2.8z",
    )
    val NewHotFilled = solid(
        "new_hot_filled",
        "M4 5h16a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2zM10 9.2v5.6l4.6-2.8z",
    )
    val Search = outline("search", "M4 11a7 7 0 1 0 14 0a7 7 0 1 0-14 0", "M20 20l-3.9-3.9")
    val Download = outline("download", "M12 4v11", "M7 10.5l5 5 5-5", "M5 20h14")
    val Cast = outline(
        "cast",
        "M2 8V6a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2h-6",
        "M2 12a9 9 0 0 1 8 8",
        "M2 16a5 5 0 0 1 4 4",
        "M2 20h0.01",
    )
    val Bell = outline(
        "bell",
        "M10.268 21a2 2 0 0 0 3.464 0",
        "M3.262 15.326A1 1 0 0 0 4 17h16a1 1 0 0 0 0.74-1.673C19.41 13.956 18 12.499 18 8A6 6 0 0 0 6 8c0 4.499-1.411 5.956-2.738 7.326",
    )
    val Plus = outline("plus", "M12 5v14", "M5 12h14")
    val Check = outline("check", "M20 6L9 17l-5-5")
    val Close = outline("close", "M18 6L6 18", "M6 6l12 12")
    val Back = outline("back", "M19 12H5", "M12 19l-7-7 7-7")
    val ChevronDown = outline("chevron_down", "M6 9l6 6 6-6")
    val ChevronRight = outline("chevron_right", "M9 18l6-6-6-6")
    val Play = solid("play", "M6 4.6v14.8a1 1 0 0 0 1.5 0.86l12.3-7.4a1 1 0 0 0 0-1.72L7.5 3.74A1 1 0 0 0 6 4.6z")
    val Pause = solid("pause", "M6.5 4h3v16h-3z", "M14.5 4h3v16h-3z")
    val Info = outline("info", "M2 12a10 10 0 1 0 20 0a10 10 0 1 0-20 0", "M12 16v-4", "M12 8h0.01")
    val Share = outline(
        "share",
        "M14.536 21.686a0.5 0.5 0 0 0 0.937-0.024l6.5-19a0.496 0.496 0 0 0-0.635-0.635l-19 6.5a0.5 0.5 0 0 0-0.024 0.937l7.93 3.18a2 2 0 0 1 1.112 1.11z",
        "M21.854 2.147l-10.94 10.939",
    )
    val ThumbUp = outline(
        "thumb_up",
        "M7 10v12",
        "M15 5.88L14 10h5.83a2 2 0 0 1 1.92 2.56l-2.33 8A2 2 0 0 1 17.5 22H4a2 2 0 0 1-2-2v-8a2 2 0 0 1 2-2h2.76a2 2 0 0 0 1.79-1.11L12 2a3.13 3.13 0 0 1 3 3.88z",
    )
    val Rewind = outline("rewind", "M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8", "M3 3v5h5")
    val Forward = outline("forward", "M21 12a9 9 0 1 1-9-9 9.75 9.75 0 0 1 6.74 2.74L21 8", "M21 3v5h-5")
    val VolumeOn = outline("volume_on", "M11 5L6 9H3v6h3l5 4z", "M15.5 8.5a5 5 0 0 1 0 7", "M18.5 5.5a9.5 9.5 0 0 1 0 13")
    val VolumeOff = outline("volume_off", "M11 5L6 9H3v6h3l5 4z", "M22 9l-6 6", "M16 9l6 6")
    val Lock = outline(
        "lock",
        "M5 11h14a2 2 0 0 1 2 2v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2z",
        "M7 11V7a5 5 0 0 1 10 0v4",
    )
    val Episodes = outline(
        "episodes",
        "M8 4h11a2 2 0 0 1 2 2v9",
        "M5 8h11a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-8a2 2 0 0 1 2-2z",
    )
    val Subtitles = outline(
        "subtitles",
        "M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z",
        "M7 8.5h10",
        "M7 12.5h6",
    )
    val Speed = outline("speed", "M12 14l4-4", "M3.34 19a10 10 0 1 1 17.32 0")
    val NextEpisode = outline("next_episode", "M6 5l10 7-10 7z", "M19 5v14")
    val Edit = outline("edit", "M4 20l1-4.2L15.8 5a2.1 2.1 0 0 1 3 3L8 18.8z", "M13.5 7.5l3 3")
    val Menu = outline("menu", "M4 6h16", "M4 12h16", "M4 18h16")
    val Mic = outline("mic", "M12 19v3", "M19 10v2a7 7 0 0 1-14 0v-2", "M9 5a3 3 0 0 1 6 0v7a3 3 0 0 1-6 0z")
    val Help = outline("help", "M2 12a10 10 0 1 0 20 0a10 10 0 1 0-20 0", "M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3", "M12 17h0.01")
    val Logout = outline("logout", "M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4", "M16 17l5-5-5-5", "M21 12H9")
    val User = outline("user", "M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2", "M8 7a4 4 0 1 0 8 0a4 4 0 1 0-8 0")
    val Settings = outline(
        "settings",
        "M4 6h10", "M18 6h2", "M4 12h4", "M12 12h8", "M4 18h12", "M20 18h0.01",
        "M14 6a2 2 0 1 0 4 0a2 2 0 1 0-4 0", "M8 12a2 2 0 1 0 4 0a2 2 0 1 0-4 0",
    )
    val Trash = outline(
        "trash",
        "M3 6h18",
        "M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6",
        "M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2",
    )
    val Zap = outline(
        "zap",
        "M4 14a1 1 0 0 1-0.78-1.63l9.9-10.2a0.5 0.5 0 0 1 0.86 0.46l-1.92 6.02A1 1 0 0 0 13 10h7a1 1 0 0 1 0.78 1.63l-9.9 10.2a0.5 0.5 0 0 1-0.86-0.46l1.92-6.02A1 1 0 0 0 11 14z",
    )
    val Phone = outline("phone", "M7 2h10a2 2 0 0 1 2 2v16a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2z", "M12 18h0.01")
    val More = solid(
        "more",
        "M10.6 5a1.4 1.4 0 1 0 2.8 0a1.4 1.4 0 1 0-2.8 0z",
        "M10.6 12a1.4 1.4 0 1 0 2.8 0a1.4 1.4 0 1 0-2.8 0z",
        "M10.6 19a1.4 1.4 0 1 0 2.8 0a1.4 1.4 0 1 0-2.8 0z",
    )
}
