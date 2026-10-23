package com.xflix.app.ui.my

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xflix.app.R
import com.xflix.app.XflixViewModel
import com.xflix.app.data.Catalog
import com.xflix.app.data.Kind
import com.xflix.app.data.Title
import com.xflix.app.ui.components.BarIcon
import com.xflix.app.ui.components.DownloadRing
import com.xflix.app.ui.components.TextAction
import com.xflix.app.ui.components.XButton
import com.xflix.app.ui.components.XButtonStyle
import com.xflix.app.ui.components.XImage
import com.xflix.app.ui.icons.XIcons
import com.xflix.app.ui.theme.Dimens
import com.xflix.app.ui.theme.Motion
import com.xflix.app.ui.theme.Space
import com.xflix.app.ui.theme.XColors
import com.xflix.app.ui.theme.XType
import java.util.Locale

private const val MbPerMinute = 6.8f
private val Indonesian = Locale.forLanguageTag("id-ID")

private data class DownloadGroup(val title: Title, val keys: List<String>, val progress: Float, val minutes: Int)

private fun group(downloads: Map<String, Float>): List<DownloadGroup> =
    downloads.keys.groupBy { it.substringBefore('/') }.map { (id, keys) ->
        val title = Catalog.title(id)
        val minutes = if (title.kind == Kind.Movie) title.minutes else keys.sumOf { key ->
            // Kunci episode: id/s{musim}/e{nomor}
            val parts = key.split('/')
            val season = parts.getOrNull(1)?.drop(1)?.toIntOrNull() ?: 1
            val number = parts.getOrNull(2)?.drop(1)?.toIntOrNull() ?: 1
            title.seasons.getOrNull(season - 1)?.episodes?.getOrNull(number - 1)?.minutes ?: 0
        }
        DownloadGroup(title, keys.sorted(), keys.map { downloads.getValue(it) }.average().toFloat(), minutes)
    }.sortedBy { Catalog.titles.indexOf(it.title) }

private fun sizeLabel(minutes: Int): String {
    val mb = (minutes * MbPerMinute).toInt()
    return if (mb >= 1000) String.format(Indonesian, "%.1f GB", mb / 1000f) else "$mb MB"
}

@Composable
fun DownloadsScreen(
    vm: XflixViewModel,
    onBack: () -> Unit,
    onOpenTitle: (Title) -> Unit,
    onPlay: (Title) -> Unit,
    onFindMore: () -> Unit,
) {
    var editing by rememberSaveable { mutableStateOf(false) }
    val groups = group(vm.downloads)
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

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
                .padding(start = Space.xs, end = Space.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BarIcon(XIcons.Back, stringResource(R.string.action_back), onBack)
            Text(stringResource(R.string.nav_downloads), style = XType.Title, modifier = Modifier.weight(1f))
            if (groups.isNotEmpty()) {
                AnimatedContent(
                    targetState = editing,
                    transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(120)) },
                    label = "edit",
                ) { isEditing ->
                    if (isEditing) {
                        TextAction(stringResource(R.string.action_done), { editing = false })
                    } else {
                        BarIcon(XIcons.Edit, stringResource(R.string.action_edit), { editing = true })
                    }
                }
            }
        }
        SmartDownloadsRow(enabled = vm.smartDownloads) { vm.smartDownloads = !vm.smartDownloads }

        if (groups.isEmpty()) {
            EmptyDownloads(onFindMore)
        } else {
            LazyColumn(contentPadding = PaddingValues(top = Space.s, bottom = bottomInset + Space.l)) {
                items(groups, key = { it.title.id }) { group ->
                    DownloadRow(
                        group = group,
                        editing = editing,
                        onClick = { if (group.progress >= 1f) onPlay(group.title) else onOpenTitle(group.title) },
                        onDelete = { group.keys.forEach(vm::removeDownload) },
                        modifier = Modifier.animateItem(),
                    )
                }
                item {
                    XButton(
                        text = stringResource(R.string.downloads_find_more),
                        onClick = onFindMore,
                        style = XButtonStyle.Gray,
                        modifier = Modifier
                            .padding(horizontal = Dimens.Gutter, vertical = Space.xl)
                            .fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun SmartDownloadsRow(enabled: Boolean, onToggle: () -> Unit) {
    val tint by animateColorAsState(if (enabled) XColors.Download else XColors.TextMuted, tween(Motion.Medium), label = "smart-tint")
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = Dimens.Gutter, vertical = Space.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(XIcons.Zap, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(Space.s))
        Text(stringResource(R.string.downloads_smart), style = XType.Caption.copy(color = XColors.TextSecondary))
        Spacer(Modifier.width(Space.xs))
        Text(
            stringResource(if (enabled) R.string.smart_downloads_on else R.string.smart_downloads_off),
            style = XType.Caption.copy(color = tint),
        )
    }
}

@Composable
private fun DownloadRow(
    group: DownloadGroup,
    editing: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = group.title
    val done = group.progress >= 1f
    // Geser sedikit saat edit
    val shift by animateDpAsState(if (editing) 4.dp else 0.dp, tween(Motion.Medium, easing = Motion.Emphasized), label = "edit-shift")
    Row(
        modifier
            .fillMaxWidth()
            .clickable(enabled = !editing, onClick = onClick)
            .padding(horizontal = Dimens.Gutter, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .offset { IntOffset(shift.roundToPx(), 0) }
                .width(124.dp)
                .height(70.dp)
                .clip(RoundedCornerShape(Dimens.Radius)),
            contentAlignment = Alignment.Center,
        ) {
            XImage(title.backdrop, Modifier.fillMaxSize(), displayWidth = 124.dp)
            if (done) {
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(XIcons.Play, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                }
            }
        }
        Spacer(Modifier.width(Space.m))
        Column(
            Modifier
                .weight(1f)
                .offset { IntOffset(shift.roundToPx(), 0) }
        ) {
            Text(title.name, style = XType.Label.copy(fontSize = 14.sp), maxLines = 1)
            Spacer(Modifier.height(2.dp))
            val meta = if (title.kind == Kind.Series) {
                stringResource(R.string.downloads_episodes_meta, group.keys.size, sizeLabel(group.minutes))
            } else {
                stringResource(R.string.downloads_movie_meta, title.rating, sizeLabel(group.minutes))
            }
            Text(
                if (done) meta else stringResource(R.string.downloading_percent, (group.progress * 100).toInt()),
                style = XType.Caption.copy(color = if (done) XColors.TextSecondary else XColors.Download),
            )
        }
        AnimatedContent(
            targetState = editing,
            transitionSpec = { (scaleIn(initialScale = 0.5f) + fadeIn()) togetherWith (scaleOut(targetScale = 0.5f) + fadeOut()) },
            label = "row-trailing",
        ) { isEditing ->
            when {
                isEditing -> BarIcon(XIcons.Trash, stringResource(R.string.action_delete), onDelete, tint = XColors.Red)
                done -> Icon(XIcons.ChevronRight, contentDescription = null, tint = XColors.TextSecondary, modifier = Modifier.size(20.dp))
                else -> DownloadRing(group.progress, 0f, Modifier.size(26.dp))
            }
        }
    }
}

@Composable
private fun EmptyDownloads(onFindMore: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Space.xxl, vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(XColors.SurfaceRaised),
            contentAlignment = Alignment.Center,
        ) {
            Icon(XIcons.Download, contentDescription = null, tint = XColors.TextMuted, modifier = Modifier.size(52.dp))
        }
        Spacer(Modifier.height(Space.xl))
        Text(stringResource(R.string.downloads_empty), style = XType.Body.copy(color = XColors.TextSecondary), textAlign = TextAlign.Center)
        Spacer(Modifier.height(Space.xl))
        XButton(stringResource(R.string.downloads_find), onFindMore)
    }
}
