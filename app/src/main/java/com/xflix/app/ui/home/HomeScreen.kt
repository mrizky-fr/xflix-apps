package com.xflix.app.ui.home

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xflix.app.HomeFilter
import com.xflix.app.R
import com.xflix.app.XflixViewModel
import com.xflix.app.data.Catalog
import com.xflix.app.data.Genre
import com.xflix.app.data.Kind
import com.xflix.app.data.Title
import com.xflix.app.ui.components.ArtSize
import com.xflix.app.ui.components.BarIcon
import com.xflix.app.ui.components.ContinueCard
import com.xflix.app.ui.components.LocalToaster
import com.xflix.app.ui.components.MoodLine
import com.xflix.app.ui.components.PickerOverlay
import com.xflix.app.ui.components.PosterRow
import com.xflix.app.ui.components.SectionHeader
import com.xflix.app.ui.components.SkeletonBlock
import com.xflix.app.ui.components.TitleArt
import com.xflix.app.ui.components.Top10Card
import com.xflix.app.ui.components.XButton
import com.xflix.app.ui.components.XButtonStyle
import com.xflix.app.ui.components.XImage
import com.xflix.app.ui.components.XMark
import com.xflix.app.ui.icons.XIcons
import com.xflix.app.ui.motion.sharedTitle
import com.xflix.app.ui.theme.Dimens
import com.xflix.app.ui.theme.Motion
import com.xflix.app.ui.theme.Space
import com.xflix.app.ui.theme.XColors
import com.xflix.app.ui.theme.XType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class RowType { Continue, Top10, Poster, Tall }

private data class HomeRow(val key: String, @param:StringRes val label: Int, val type: RowType, val titles: List<Title>)

private val TitleBarHeight = 48.dp
private val ChipsHeight = 44.dp

private fun buildRows(filter: HomeFilter, myList: List<String>): List<HomeRow> {
    val match = { t: Title -> (filter.kind == null || t.kind == filter.kind) && (filter.genre == null || filter.genre in t.genres) }
    val pool = Catalog.available.filter(match)
    fun genres(vararg g: Genre) = pool.filter { t -> g.any { it in t.genres } }
    val top10Label = when (filter.kind) {
        Kind.Series -> R.string.row_top10_series
        Kind.Movie -> R.string.row_top10_movies
        null -> R.string.row_top10
    }
    return listOf(
        HomeRow("continue", R.string.row_continue, RowType.Continue, Catalog.continueWatching.map { Catalog.title(it.titleId) }.filter(match)),
        HomeRow("mylist", R.string.row_my_list, RowType.Poster, myList.map { Catalog.title(it) }.filter(match)),
        HomeRow("top10", top10Label, RowType.Top10, Catalog.top10.filter(match)),
        HomeRow("originals", R.string.row_originals, RowType.Tall, pool.filter { it.original }),
        HomeRow("new", R.string.row_new, RowType.Poster, pool.sortedByDescending { it.year }.take(10)),
        HomeRow("horror", R.string.row_horror, RowType.Poster, genres(Genre.Horror)),
        HomeRow("thriller", R.string.row_thriller, RowType.Poster, genres(Genre.Thriller, Genre.Mystery)),
        HomeRow("romance", R.string.row_romance, RowType.Poster, genres(Genre.Romance, Genre.Drama)),
        HomeRow("docs", R.string.row_docs, RowType.Poster, genres(Genre.Documentary)),
        HomeRow("action", R.string.row_action, RowType.Poster, genres(Genre.Action, Genre.Crime)),
    ).filter { it.titles.isNotEmpty() }
}

@Composable
fun HomeScreen(
    vm: XflixViewModel,
    bottomPadding: Dp,
    onOpenTitle: (Title, String?) -> Unit,
    onPlay: (Title) -> Unit,
    onSearch: () -> Unit,
    onDownloads: () -> Unit,
) {
    val filter = vm.homeFilter
    val density = LocalDensity.current
    val toaster = LocalToaster.current
    val castMessage = stringResource(R.string.toast_cast)
    val listState = rememberLazyListState()
    var categoriesOpen by rememberSaveable { mutableStateOf(false) }

    val rows = remember(filter, vm.myList.toList()) { buildRows(filter, vm.myList) }
    val hero = remember(filter) {
        Catalog.available
            .filter { (filter.kind == null || it.kind == filter.kind) && (filter.genre == null || filter.genre in it.genres) }
            .minByOrNull { it.top10 ?: 99 } ?: Catalog.available.first()
    }

    // Chip sembunyi saat gulir turun
    val chipsPx = with(density) { ChipsHeight.toPx() }
    var chipsOffset by remember { mutableFloatStateOf(0f) }
    val collapse = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                chipsOffset = (chipsOffset + available.y).coerceIn(-chipsPx, 0f)
                return Offset.Zero
            }
        }
    }
    val heroScroll by remember {
        derivedStateOf { if (listState.firstVisibleItemIndex == 0) listState.firstVisibleItemScrollOffset.toFloat() else Float.MAX_VALUE }
    }
    val barFill by remember {
        derivedStateOf { (heroScroll / with(density) { 180.dp.toPx() }).coerceIn(0f, 1f) }
    }
    val tint by animateColorAsState(hero.tint, tween(600), label = "hero-tint")
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topInset = statusTop + TitleBarHeight + ChipsHeight

    val scope = rememberCoroutineScope()
    // Ke atas hanya saat filter
    val applyFilter: (HomeFilter) -> Unit = { next ->
        vm.homeFilter = next
        scope.launch { listState.scrollToItem(0) }
    }
    // Sembunyikan bottom bar saat overlay
    LaunchedEffect(categoriesOpen) { vm.overlayOpen = categoriesOpen }
    DisposableEffect(Unit) { onDispose { vm.overlayOpen = false } }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .nestedScroll(collapse)
    ) {
        // Tint warna hero di belakang
        Box(
            Modifier
                .fillMaxWidth()
                .height(620.dp)
                .graphicsLayer {
                    alpha = 1f - barFill * 0.7f
                    translationY = -heroScroll.coerceAtMost(2000f) * 0.4f
                }
                .background(Brush.verticalGradient(0f to tint.copy(alpha = 0.75f), 0.55f to tint.copy(alpha = 0.25f), 1f to Color.Black))
        )

        AnimatedContent(
            targetState = vm.homeReady,
            transitionSpec = { fadeIn(tween(320)) togetherWith fadeOut(tween(200)) },
            label = "home-ready",
        ) { ready ->
            if (!ready) {
                HomeSkeleton(topInset)
            } else {
                val revealed = remember { mutableSetOf<String>() }
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = topInset + Space.xs, bottom = bottomPadding + Space.l),
                ) {
                    item(key = "hero") {
                        AnimatedContent(
                            targetState = hero,
                            transitionSpec = { fadeIn(tween(380, 120)) togetherWith fadeOut(tween(200)) },
                            label = "hero-swap",
                        ) { current ->
                            HeroCard(
                                title = current,
                                inList = current.id in vm.myList,
                                scroll = { heroScroll.coerceAtMost(3000f) },
                                onOpen = { onOpenTitle(current, "hero/${current.id}") },
                                onPlay = { onPlay(current) },
                                onToggleList = { vm.toggleMyList(current.id) },
                            )
                        }
                    }
                    itemsIndexed(rows, key = { _, row -> row.key }) { index, row ->
                        Column(
                            Modifier
                                .animateItem()
                                .revealOnce(row.key, index, revealed)
                                .padding(top = Dimens.RowGap)
                        ) {
                            val label = if (row.type == RowType.Continue) {
                                stringResource(row.label, vm.profile?.name.orEmpty())
                            } else {
                                stringResource(row.label)
                            }
                            SectionHeader(label)
                            HomeRowContent(row, onOpenTitle, onPlay)
                        }
                    }
                }
            }
        }

        HomeTopBar(
            name = vm.profile?.name.orEmpty(),
            filter = filter,
            fill = { barFill },
            chipsOffset = { chipsOffset },
            chipsPx = chipsPx,
            onFilter = applyFilter,
            onCategories = { categoriesOpen = true },
            onCast = { toaster.show(castMessage) },
            onDownloads = onDownloads,
            onSearch = onSearch,
        )

        // Hanya genre yang ada isinya
        val genres = remember { Genre.entries.filter { g -> Catalog.available.any { g in it.genres } } }
        val genreLabels = genres.map { stringResource(it.label) }
        PickerOverlay(
            visible = categoriesOpen,
            options = listOf(stringResource(R.string.category_home)) + genreLabels,
            selected = filter.genre?.let { genres.indexOf(it) + 1 } ?: 0,
            onPick = { index ->
                applyFilter(filter.copy(genre = if (index == 0) null else genres[index - 1]))
                categoriesOpen = false
            },
            onDismiss = { categoriesOpen = false },
        )
    }
}

@Composable
private fun HomeRowContent(row: HomeRow, onOpenTitle: (Title, String?) -> Unit, onPlay: (Title) -> Unit) {
    when (row.type) {
        RowType.Poster -> PosterRow(row.titles, row.key, onOpenTitle)
        RowType.Tall -> PosterRow(row.titles, row.key, onOpenTitle, width = 128.dp, aspect = 0.56f, art = ArtSize.Detail)
        RowType.Top10 -> LazyRow(
            contentPadding = PaddingValues(horizontal = Dimens.Gutter),
            horizontalArrangement = Arrangement.spacedBy(Dimens.PosterGap),
        ) {
            itemsIndexed(row.titles, key = { _, t -> t.id }) { i, title ->
                val key = "top10/${title.id}"
                Top10Card(i + 1, title, { onOpenTitle(title, key) }, key)
            }
        }
        RowType.Continue -> {
            val progress = Catalog.continueWatching.associateBy { it.titleId }
            LazyRow(
                contentPadding = PaddingValues(horizontal = Dimens.Gutter),
                horizontalArrangement = Arrangement.spacedBy(Dimens.PosterGap),
            ) {
                items(row.titles, key = { it.id }) { title ->
                    ContinueCard(
                        progress = progress.getValue(title.id),
                        title = title,
                        onPlay = { onPlay(title) },
                        onInfo = { onOpenTitle(title, null) },
                    )
                }
            }
        }
    }
}

/** Baris muncul bertahap, sekali */
@Composable
private fun Modifier.revealOnce(key: String, index: Int, revealed: MutableSet<String>): Modifier {
    val fresh = remember(key) { key !in revealed }
    val progress = remember(key) { Animatable(if (fresh) 0f else 1f) }
    LaunchedEffect(key) {
        if (!fresh) return@LaunchedEffect
        revealed += key
        delay(60L * index.coerceAtMost(5))
        progress.animateTo(1f, tween(460, easing = Motion.EmphasizedDecelerate))
    }
    return this.graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * 28.dp.toPx()
    }
}

@Composable
private fun HeroCard(
    title: Title,
    inList: Boolean,
    scroll: () -> Float,
    onOpen: () -> Unit,
    onPlay: () -> Unit,
    onToggleList: () -> Unit,
) {
    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) { enter.animateTo(1f, tween(1100, easing = Motion.EmphasizedDecelerate)) }
    val shape = RoundedCornerShape(10.dp)
    Box(
        Modifier
            .padding(horizontal = Dimens.Gutter)
            .fillMaxWidth()
            .aspectRatio(0.78f)
            .shadow(24.dp, shape, ambientColor = title.tint, spotColor = title.tint)
            .sharedTitle("hero/${title.id}", 10.dp)
            .clip(shape)
            .border(1.dp, Color.White.copy(alpha = 0.14f), shape)
            .clickable(onClick = onOpen)
    ) {
        XImage(
            id = title.poster,
            displayWidth = 400.dp,
            eager = true,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Ken Burns dan parallax
                    val s = 1.1f - 0.1f * enter.value
                    scaleX = s
                    scaleY = s
                    translationY = scroll() * 0.2f
                },
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(0.42f to Color.Transparent, 0.78f to Color.Black.copy(alpha = 0.7f), 1f to Color.Black.copy(alpha = 0.92f)))
        )
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(14.dp)
                .graphicsLayer {
                    alpha = enter.value
                    translationY = (1f - enter.value) * 24.dp.toPx()
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (title.original) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    XMark(13.dp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(if (title.kind == Kind.Series) R.string.kind_series_caps else R.string.kind_movie_caps),
                        style = XType.Overline.copy(letterSpacing = 3.sp, color = XColors.TextSecondary),
                    )
                }
                Spacer(Modifier.height(6.dp))
            }
            TitleArt(title, ArtSize.Hero)
            Spacer(Modifier.height(Space.s))
            MoodLine(title, color = Color.White.copy(alpha = 0.85f))
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(Space.s)) {
                XButton(
                    text = stringResource(R.string.action_play),
                    onClick = onPlay,
                    icon = XIcons.Play,
                    modifier = Modifier.weight(1f),
                )
                XButton(
                    text = stringResource(R.string.action_my_list),
                    onClick = onToggleList,
                    style = XButtonStyle.Gray,
                    modifier = Modifier.weight(1f),
                    leading = { ListIcon(inList) },
                )
            }
        }
    }
}

/** Plus berubah jadi centang */
@Composable
fun ListIcon(inList: Boolean, size: Dp = 20.dp) {
    AnimatedContent(
        targetState = inList,
        transitionSpec = {
            (scaleIn(tween(240), initialScale = 0.4f) + fadeIn(tween(160))) togetherWith
                (scaleOut(tween(160), targetScale = 0.4f) + fadeOut(tween(120)))
        },
        label = "list-icon",
    ) { added ->
        Icon(
            if (added) XIcons.Check else XIcons.Plus,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(size),
        )
    }
}

@Composable
private fun HomeTopBar(
    name: String,
    filter: HomeFilter,
    fill: () -> Float,
    chipsOffset: () -> Float,
    chipsPx: Float,
    onFilter: (HomeFilter) -> Unit,
    onCategories: () -> Unit,
    onCast: () -> Unit,
    onDownloads: () -> Unit,
    onSearch: () -> Unit,
) {
    val density = LocalDensity.current
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val visibleChips = with(density) { (chipsPx + chipsOffset()).toDp() }
    Column(
        Modifier
            .fillMaxWidth()
            .height(statusTop + TitleBarHeight + visibleChips)
            .graphicsLayer { clip = true }
            .background(Color.Black.copy(alpha = fill()))
    ) {
        Spacer(Modifier.height(statusTop))
        Row(
            Modifier
                .fillMaxWidth()
                .height(TitleBarHeight)
                .padding(start = Dimens.Gutter, end = Space.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            XMark(22.dp)
            Spacer(Modifier.width(10.dp))
            Text(
                stringResource(R.string.home_for, name),
                style = XType.Title.copy(fontSize = 19.sp),
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            BarIcon(XIcons.Cast, stringResource(R.string.action_cast), onCast)
            BarIcon(XIcons.Download, stringResource(R.string.nav_downloads), onDownloads)
            BarIcon(XIcons.Search, stringResource(R.string.action_search), onSearch)
        }
        Box(
            Modifier
                .height(ChipsHeight)
                .graphicsLayer {
                    translationY = chipsOffset()
                    alpha = 1f + chipsOffset() / chipsPx
                },
            contentAlignment = Alignment.CenterStart,
        ) {
            FilterChips(filter, onFilter, onCategories)
        }
    }
}

@Composable
private fun FilterChips(filter: HomeFilter, onFilter: (HomeFilter) -> Unit, onCategories: () -> Unit) {
    Row(
        Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = Dimens.Gutter),
        horizontalArrangement = Arrangement.spacedBy(Space.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedVisibility(
            visible = filter.isActive,
            enter = expandHorizontally(tween(Motion.Medium)) + fadeIn(),
            exit = shrinkHorizontally(tween(Motion.Medium)) + fadeOut(),
        ) {
            Box(
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .border(1.dp, XColors.Outline, CircleShape)
                    .clickable { onFilter(HomeFilter()) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(XIcons.Close, contentDescription = stringResource(R.string.action_clear_filter), tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
        listOf(Kind.Series, Kind.Movie).filter { k -> Catalog.available.any { it.kind == k } }.forEach { kind ->
            AnimatedVisibility(
                visible = filter.kind == null || filter.kind == kind,
                enter = expandHorizontally(tween(Motion.Medium)) + fadeIn(),
                exit = shrinkHorizontally(tween(Motion.Medium)) + fadeOut(),
            ) {
                Chip(
                    label = stringResource(if (kind == Kind.Series) R.string.chip_series else R.string.chip_movies),
                    selected = filter.kind == kind,
                    onClick = { onFilter(filter.copy(kind = if (filter.kind == kind) null else kind)) },
                )
            }
        }
        Chip(
            label = filter.genre?.let { stringResource(it.label) } ?: stringResource(R.string.chip_categories),
            selected = filter.genre != null,
            chevron = true,
            onClick = onCategories,
        )
    }
}

@Composable
private fun Chip(label: String, selected: Boolean, onClick: () -> Unit, chevron: Boolean = false) {
    val background by animateColorAsState(
        if (selected) Color.White.copy(alpha = 0.22f) else Color.Transparent,
        tween(Motion.Short),
        label = "chip-bg",
    )
    Row(
        Modifier
            .height(32.dp)
            .clip(CircleShape)
            .background(background)
            .border(1.dp, if (selected) Color.White.copy(alpha = 0.7f) else XColors.Outline, CircleShape)
            .clickable(onClick = onClick)
            .padding(start = 14.dp, end = if (chevron) 10.dp else 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = XType.Label.copy(fontWeight = FontWeight.Medium))
        if (chevron) {
            Spacer(Modifier.width(4.dp))
            Icon(XIcons.ChevronDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun HomeSkeleton(topInset: Dp) {
    Column(Modifier.padding(top = topInset + Space.xs)) {
        SkeletonBlock(
            Modifier
                .padding(horizontal = Dimens.Gutter)
                .fillMaxWidth()
                .aspectRatio(0.78f),
            corner = 10.dp,
        )
        repeat(2) {
            Spacer(Modifier.height(Dimens.RowGap))
            SkeletonBlock(
                Modifier
                    .padding(start = Dimens.Gutter)
                    .width(150.dp)
                    .height(14.dp)
            )
            Spacer(Modifier.height(Space.s))
            Row(
                Modifier.padding(start = Dimens.Gutter),
                horizontalArrangement = Arrangement.spacedBy(Dimens.PosterGap),
            ) {
                repeat(4) {
                    SkeletonBlock(
                        Modifier
                            .width(Dimens.PosterWidth)
                            .aspectRatio(2f / 3f)
                    )
                }
            }
        }
    }
}
