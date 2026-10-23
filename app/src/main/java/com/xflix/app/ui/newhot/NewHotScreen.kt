package com.xflix.app.ui.newhot

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xflix.app.R
import com.xflix.app.XflixViewModel
import com.xflix.app.data.Catalog
import com.xflix.app.data.Kind
import com.xflix.app.data.Title
import com.xflix.app.ui.components.ArtSize
import com.xflix.app.ui.components.BarIcon
import com.xflix.app.ui.components.IconAction
import com.xflix.app.ui.components.MoodLine
import com.xflix.app.ui.components.TitleArt
import com.xflix.app.ui.components.XImage
import com.xflix.app.ui.components.XMark
import com.xflix.app.ui.icons.XIcons
import com.xflix.app.ui.theme.Inter
import com.xflix.app.ui.theme.Dimens
import com.xflix.app.ui.theme.Motion
import com.xflix.app.ui.theme.Space
import com.xflix.app.ui.theme.XColors
import com.xflix.app.ui.theme.XType
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

private val Indonesian = Locale.forLanguageTag("id-ID")

private enum class Section(@param:StringRes val label: Int) {
    ComingSoon(R.string.newhot_coming),
    Everyone(R.string.newhot_everyone),
    TopSeries(R.string.newhot_top_series),
    TopMovies(R.string.newhot_top_movies),
}

private sealed interface Entry {
    val section: Section
    data class Header(override val section: Section) : Entry
    data class Item(override val section: Section, val title: Title, val rank: Int?) : Entry
}

private fun buildEntries(): List<Entry> {
    val everyone = Catalog.available.filter { it.newEpisodes || it.original }.take(5)
    val topSeries = Catalog.top10.filter { it.kind == Kind.Series }
    val topMovies = Catalog.top10.filter { it.kind == Kind.Movie }
    return buildList {
        add(Entry.Header(Section.ComingSoon))
        Catalog.upcoming.forEach { add(Entry.Item(Section.ComingSoon, it, null)) }
        add(Entry.Header(Section.Everyone))
        everyone.forEach { add(Entry.Item(Section.Everyone, it, null)) }
        add(Entry.Header(Section.TopSeries))
        topSeries.forEachIndexed { i, t -> add(Entry.Item(Section.TopSeries, t, i + 1)) }
        add(Entry.Header(Section.TopMovies))
        topMovies.forEachIndexed { i, t -> add(Entry.Item(Section.TopMovies, t, i + 1)) }
    }
}

@Composable
fun NewHotScreen(
    vm: XflixViewModel,
    bottomPadding: Dp,
    onOpenTitle: (Title) -> Unit,
    onPlay: (Title) -> Unit,
    onSearch: () -> Unit,
) {
    val entries = remember { buildEntries() }
    val headerIndex = remember(entries) {
        entries.withIndex().filter { it.value is Entry.Header }.associate { it.value.section to it.index }
    }
    val listState = rememberLazyListState()
    val chipState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    // Chip aktif mengikuti posisi gulir
    val current by remember {
        derivedStateOf { entries.getOrNull(listState.firstVisibleItemIndex)?.section ?: Section.ComingSoon }
    }
    LaunchedEffect(current) { chipState.animateScrollToItem(current.ordinal) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(Dimens.TopBarHeight)
                .padding(start = Dimens.Gutter, end = Space.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.nav_new_hot), style = XType.Headline, modifier = Modifier.weight(1f))
            BarIcon(XIcons.Search, stringResource(R.string.action_search), onSearch)
        }
        LazyRow(
            state = chipState,
            contentPadding = PaddingValues(horizontal = Dimens.Gutter, vertical = Space.s),
            horizontalArrangement = Arrangement.spacedBy(Space.s),
        ) {
            itemsIndexed(Section.entries) { _, section ->
                SectionChip(stringResource(section.label), selected = section == current) {
                    scope.launch { listState.animateScrollToItem(headerIndex.getValue(section)) }
                }
            }
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = bottomPadding + Space.l),
        ) {
            itemsIndexed(entries, key = { i, e -> if (e is Entry.Item) "${e.section}-${e.title.id}" else "h-$i" }) { _, entry ->
                when (entry) {
                    is Entry.Header -> Text(
                        stringResource(entry.section.label),
                        style = XType.Title,
                        modifier = Modifier.padding(start = Dimens.Gutter, top = Space.l, bottom = Space.m),
                    )
                    is Entry.Item -> NewHotItem(
                        entry = entry,
                        reminded = entry.title.id in vm.reminders,
                        inList = entry.title.id in vm.myList,
                        onRemind = { vm.toggleReminder(entry.title.id) },
                        onToggleList = { vm.toggleMyList(entry.title.id) },
                        onPlay = { onPlay(entry.title) },
                        onOpen = { onOpenTitle(entry.title) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(if (selected) Color.White else Color.Transparent, tween(Motion.Short), label = "chip-bg")
    val fg by animateColorAsState(if (selected) Color.Black else Color.White, tween(Motion.Short), label = "chip-fg")
    Text(
        label,
        style = XType.Label.copy(color = fg),
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, if (selected) Color.White else XColors.Outline, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    )
}

@Composable
private fun NewHotItem(
    entry: Entry.Item,
    reminded: Boolean,
    inList: Boolean,
    onRemind: () -> Unit,
    onToggleList: () -> Unit,
    onPlay: () -> Unit,
    onOpen: () -> Unit,
) {
    val title = entry.title
    val upcoming = entry.section == Section.ComingSoon
    Row(Modifier.padding(end = Dimens.Gutter, bottom = Space.xl)) {
        // Kolom kiri: tanggal atau peringkat
        Column(Modifier.width(56.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            when {
                upcoming && title.premiere != null -> {
                    Text(
                        title.premiere.format(DateTimeFormatter.ofPattern("MMM", Indonesian)).uppercase(),
                        style = XType.Caption.copy(fontWeight = FontWeight.SemiBold),
                    )
                    Text(
                        title.premiere.dayOfMonth.toString(),
                        style = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Black, fontSize = 28.sp, lineHeight = 32.sp, letterSpacing = (-0.8).sp, color = Color.White),
                    )
                }
                entry.rank != null -> Text(
                    entry.rank.toString(),
                    style = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Black, fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-1).sp, color = Color.White),
                )
            }
        }
        Column(Modifier.weight(1f)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(Dimens.CardRadius))
                    .clickable(onClick = onOpen)
            ) {
                XImage(title.backdrop, Modifier.fillMaxSize(), displayWidth = 340.dp)
                if (title.original) XMark(14.dp, Modifier.padding(Space.s))
            }
            Spacer(Modifier.height(Space.m))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TitleArt(
                    title,
                    ArtSize.Card,
                    modifier = Modifier.weight(1f),
                    align = TextAlign.Start,
                    maxLines = 1,
                )
                if (upcoming) {
                    RemindAction(reminded, onRemind)
                    IconAction(XIcons.Info, stringResource(R.string.action_info), onOpen)
                } else {
                    IconAction(XIcons.Play, stringResource(R.string.action_play), onPlay)
                    IconAction(if (inList) XIcons.Check else XIcons.Plus, stringResource(R.string.action_my_list), onToggleList)
                }
            }
            Spacer(Modifier.height(Space.s))
            if (upcoming && title.premiere != null) {
                Text(
                    stringResource(R.string.premieres_on, title.premiere.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Indonesian))),
                    style = XType.Caption.copy(color = Color.White.copy(alpha = 0.8f)),
                )
                Spacer(Modifier.height(Space.xs))
            }
            Text(title.name, style = XType.Title)
            Spacer(Modifier.height(Space.xs))
            Text(title.synopsis, style = XType.BodySmall, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(Space.s))
            MoodLine(title)
        }
    }
}

/** Lonceng berayun, lalu centang */
@Composable
private fun RemindAction(reminded: Boolean, onClick: () -> Unit) {
    val swing = remember { Animatable(0f) }
    var showCheck by remember { mutableStateOf(reminded) }
    LaunchedEffect(reminded) {
        if (!reminded) {
            showCheck = false
            return@LaunchedEffect
        }
        if (showCheck) return@LaunchedEffect
        swing.animateTo(0f, keyframes {
            durationMillis = 600
            -22f at 80
            18f at 180
            -12f at 280
            8f at 380
            -3f at 480
        })
        showCheck = true
    }
    IconAction(
        icon = if (showCheck) XIcons.Check else XIcons.Bell,
        label = stringResource(if (reminded) R.string.action_reminded_short else R.string.action_remind_me),
        onClick = onClick,
        iconModifier = Modifier.graphicsLayer {
            rotationZ = swing.value
            transformOrigin = TransformOrigin(0.5f, 0.1f)
        },
    )
}
