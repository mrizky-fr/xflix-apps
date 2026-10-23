package com.xflix.app.ui.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xflix.app.R
import com.xflix.app.data.Catalog
import com.xflix.app.data.Title
import com.xflix.app.ui.components.BarIcon
import com.xflix.app.ui.components.LocalToaster
import com.xflix.app.ui.components.PosterCard
import com.xflix.app.ui.components.XImage
import com.xflix.app.ui.icons.XIcons
import com.xflix.app.ui.theme.Dimens
import com.xflix.app.ui.theme.Space
import com.xflix.app.ui.theme.XColors
import com.xflix.app.ui.theme.XType
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onOpenTitle: (Title, String?) -> Unit,
    onPlay: (Title) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val results = remember(query) { Catalog.search(query) }
    val focus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    LaunchedEffect(Unit) {
        // Tunggu transisi masuk selesai
        delay(320)
        focus.requestFocus()
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .imePadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(Dimens.TopBarHeight)
                .padding(start = Space.xs, end = Dimens.Gutter),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BarIcon(XIcons.Back, stringResource(R.string.action_back), onBack)
            Spacer(Modifier.width(Space.xs))
            SearchField(
                query = query,
                onQuery = { query = it },
                focus = focus,
                onSubmit = { focusManager.clearFocus() },
                modifier = Modifier.weight(1f),
            )
        }
        AnimatedContent(
            targetState = query.isBlank(),
            transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(150)) },
            label = "search-mode",
        ) { idle ->
            if (idle) {
                TopSearches(bottomInset, onOpenTitle, onPlay)
            } else if (results.isEmpty()) {
                NoResults()
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(start = Dimens.Gutter, end = Dimens.Gutter, top = Space.s, bottom = bottomInset + Space.l),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.PosterGap),
                    verticalArrangement = Arrangement.spacedBy(Dimens.PosterGap),
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(stringResource(R.string.search_results), style = XType.Section, modifier = Modifier.padding(bottom = Space.xs))
                    }
                    items(results, key = { it.id }) { title ->
                        val key = "search/${title.id}"
                        PosterCard(title, { onOpenTitle(title, key) }, Modifier.animateItem(), width = null, sharedKey = key)
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQuery: (String) -> Unit,
    focus: FocusRequester,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val toaster = LocalToaster.current
    val voiceMessage = stringResource(R.string.toast_voice)
    Row(
        modifier
            .height(40.dp)
            .clip(RoundedCornerShape(Dimens.Radius))
            .background(XColors.SurfaceHigh)
            .padding(start = Space.m, end = Space.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(XIcons.Search, contentDescription = null, tint = XColors.TextSecondary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(Space.s))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) {
                Text(stringResource(R.string.search_hint), style = XType.Body.copy(color = XColors.TextMuted), maxLines = 1)
            }
            BasicTextField(
                value = query,
                onValueChange = onQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focus),
                singleLine = true,
                textStyle = XType.Body.copy(color = Color.White),
                cursorBrush = SolidColor(Color.White),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
            )
        }
        AnimatedContent(
            targetState = query.isEmpty(),
            transitionSpec = { (scaleIn(initialScale = 0.6f) + fadeIn()) togetherWith (scaleOut(targetScale = 0.6f) + fadeOut()) },
            label = "search-trailing",
        ) { empty ->
            if (empty) {
                BarIcon(XIcons.Mic, stringResource(R.string.action_voice), { toaster.show(voiceMessage) }, tint = XColors.TextSecondary)
            } else {
                BarIcon(XIcons.Close, stringResource(R.string.action_clear), { onQuery("") }, tint = XColors.TextSecondary)
            }
        }
    }
}

@Composable
private fun TopSearches(bottomInset: Dp, onOpenTitle: (Title, String?) -> Unit, onPlay: (Title) -> Unit) {
    val titles = remember { Catalog.available.sortedBy { it.top10 ?: 50 }.take(12) }
    LazyColumn(contentPadding = PaddingValues(bottom = bottomInset + Space.l)) {
        item {
            Text(
                stringResource(R.string.search_top),
                style = XType.Section,
                modifier = Modifier.padding(start = Dimens.Gutter, top = Space.m, bottom = Space.s),
            )
        }
        itemsIndexed(titles, key = { _, t -> t.id }) { _, title ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onOpenTitle(title, null) }
                    .padding(horizontal = Dimens.Gutter, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                XImage(
                    title.backdrop,
                    Modifier
                        .width(128.dp)
                        .height(72.dp)
                        .clip(RoundedCornerShape(Dimens.Radius)),
                    displayWidth = 128.dp,
                )
                Spacer(Modifier.width(Space.m))
                Text(title.name, style = XType.Label.copy(fontSize = 14.sp), modifier = Modifier.weight(1f), maxLines = 2)
                Box(
                    Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Color.White, CircleShape)
                        .clickable { onPlay(title) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(XIcons.Play, contentDescription = stringResource(R.string.action_play), tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
private fun NoResults() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Space.xxl, vertical = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.search_empty_title), style = XType.Title, textAlign = TextAlign.Center)
        Spacer(Modifier.height(Space.s))
        Text(stringResource(R.string.search_empty_body), style = XType.BodySmall, textAlign = TextAlign.Center)
    }
}
