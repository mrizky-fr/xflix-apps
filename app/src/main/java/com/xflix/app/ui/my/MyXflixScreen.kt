package com.xflix.app.ui.my

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xflix.app.R
import com.xflix.app.XflixViewModel
import com.xflix.app.data.Catalog
import com.xflix.app.data.Notice
import com.xflix.app.data.Title
import com.xflix.app.ui.components.BarIcon
import com.xflix.app.ui.components.LocalToaster
import com.xflix.app.ui.components.PosterRow
import com.xflix.app.ui.components.ProfileAvatar
import com.xflix.app.ui.components.SectionHeader
import com.xflix.app.ui.components.XImage
import com.xflix.app.ui.icons.XIcons
import com.xflix.app.ui.theme.Dimens
import com.xflix.app.ui.theme.Space
import com.xflix.app.ui.theme.XColors
import com.xflix.app.ui.theme.XType

@Composable
fun MyXflixScreen(
    vm: XflixViewModel,
    bottomPadding: Dp,
    onSwitchProfile: () -> Unit,
    onOpenTitle: (Title, String?) -> Unit,
    onDownloads: () -> Unit,
    onSearch: () -> Unit,
    onSignOut: () -> Unit,
) {
    val toaster = LocalToaster.current
    val soonMessage = stringResource(R.string.toast_coming_soon)
    val context = LocalContext.current
    val version = remember(context) { context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty() }
    val downloadedTitles = vm.downloads.keys.map { it.substringBefore('/') }.distinct().size

    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = bottomPadding + Space.l),
    ) {
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(Dimens.TopBarHeight)
                    .padding(start = Dimens.Gutter, end = Space.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.nav_my), style = XType.Headline, modifier = Modifier.weight(1f))
                BarIcon(XIcons.Search, stringResource(R.string.action_search), onSearch)
            }
        }
        item {
            vm.profile?.let { profile ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = Space.m),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ProfileAvatar(profile, Modifier.size(68.dp))
                    Spacer(Modifier.height(Space.s))
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(Dimens.Radius))
                            .clickable(onClick = onSwitchProfile)
                            .padding(horizontal = Space.s, vertical = Space.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(profile.name, style = XType.Title)
                        Spacer(Modifier.width(Space.xs))
                        Icon(XIcons.ChevronDown, contentDescription = stringResource(R.string.action_switch_profile), tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
        item {
            MenuRow(
                icon = XIcons.Bell,
                iconBackground = XColors.Red,
                label = stringResource(R.string.my_notifications),
                badge = Catalog.notices.size,
                onClick = { toaster.show(soonMessage) },
            )
        }
        items(Catalog.notices.size) { i ->
            NoticeRow(Catalog.notices[i]) { onOpenTitle(Catalog.title(Catalog.notices[i].titleId), null) }
        }
        item {
            MenuRow(
                icon = XIcons.Download,
                iconBackground = XColors.Download,
                label = stringResource(R.string.nav_downloads),
                subtitle = stringResource(R.string.my_downloads_count, downloadedTitles),
                onClick = onDownloads,
            )
        }
        item {
            Spacer(Modifier.height(Space.l))
            SectionHeader(stringResource(R.string.my_liked))
            PosterRow(vm.liked.map { Catalog.title(it) }, "liked", onOpenTitle)
        }
        item {
            Spacer(Modifier.height(Dimens.RowGap))
            SectionHeader(stringResource(R.string.row_my_list))
            AnimatedContent(
                targetState = vm.myList.isEmpty(),
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(150)) },
                label = "my-list",
            ) { empty ->
                if (empty) {
                    EmptyListCard()
                } else {
                    PosterRow(vm.myList.map { Catalog.title(it) }, "mylist-my", onOpenTitle)
                }
            }
        }
        item {
            Spacer(Modifier.height(Space.xl))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(XColors.Divider)
            )
            SettingsRow(XIcons.Edit, stringResource(R.string.my_manage_profiles), onSwitchProfile)
            SettingsRow(XIcons.Settings, stringResource(R.string.my_app_settings), { toaster.show(soonMessage) })
            SettingsRow(XIcons.User, stringResource(R.string.my_account), { toaster.show(soonMessage) })
            SettingsRow(XIcons.Help, stringResource(R.string.my_help), { toaster.show(soonMessage) })
            SettingsRow(XIcons.Logout, stringResource(R.string.my_sign_out), onSignOut, chevron = false)
            Text(
                stringResource(R.string.my_version, version),
                style = XType.Caption.copy(color = XColors.TextMuted),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Space.l),
            )
        }
    }
}

@Composable
private fun MenuRow(
    icon: ImageVector,
    iconBackground: Color,
    label: String,
    onClick: () -> Unit,
    subtitle: String? = null,
    badge: Int = 0,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.Gutter, vertical = Space.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(21.dp))
        }
        Spacer(Modifier.width(Space.m))
        Column(Modifier.weight(1f)) {
            Text(label, style = XType.Section)
            subtitle?.let { Text(it, style = XType.Caption) }
        }
        if (badge > 0) {
            Text(
                badge.toString(),
                style = XType.Caption.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp),
                modifier = Modifier
                    .background(XColors.Red, CircleShape)
                    .padding(horizontal = 7.dp, vertical = 1.dp),
            )
            Spacer(Modifier.width(Space.s))
        }
        Icon(XIcons.ChevronRight, contentDescription = null, tint = XColors.TextSecondary, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun NoticeRow(notice: Notice, onClick: () -> Unit) {
    val title = Catalog.title(notice.titleId)
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 64.dp, end = Dimens.Gutter, top = Space.xs, bottom = Space.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        XImage(
            title.backdrop,
            Modifier
                .width(104.dp)
                .height(58.dp)
                .clip(RoundedCornerShape(Dimens.Radius)),
            displayWidth = 104.dp,
        )
        Spacer(Modifier.width(Space.m))
        Column(Modifier.weight(1f)) {
            Text(notice.headline, style = XType.Caption.copy(color = XColors.TextMuted))
            Text(notice.body, style = XType.Label.copy(fontWeight = FontWeight.Medium), maxLines = 2)
            Text(notice.age, style = XType.Caption.copy(color = XColors.TextMuted))
        }
    }
}

@Composable
private fun EmptyListCard() {
    Row(
        Modifier
            .padding(horizontal = Dimens.Gutter)
            .fillMaxWidth()
            .border(1.dp, XColors.Divider, RoundedCornerShape(Dimens.CardRadius))
            .padding(Space.l),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.m),
    ) {
        Icon(XIcons.Plus, contentDescription = null, tint = XColors.TextSecondary, modifier = Modifier.size(24.dp))
        Text(stringResource(R.string.my_list_empty), style = XType.BodySmall)
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, label: String, onClick: () -> Unit, chevron: Boolean = true) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.Gutter, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = XColors.TextSecondary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(Space.l))
        Text(label, style = XType.Body, modifier = Modifier.weight(1f))
        if (chevron) Icon(XIcons.ChevronRight, contentDescription = null, tint = XColors.TextMuted, modifier = Modifier.size(18.dp))
    }
}
