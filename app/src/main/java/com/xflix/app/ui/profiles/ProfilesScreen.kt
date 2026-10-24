package com.xflix.app.ui.profiles

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.xflix.app.R
import com.xflix.app.XflixViewModel
import com.xflix.app.data.Catalog
import com.xflix.app.data.Profile
import com.xflix.app.ui.components.ProfileAvatar
import com.xflix.app.ui.components.XSpinner
import com.xflix.app.ui.components.XflixWordmark
import com.xflix.app.ui.components.animatedPressScale
import com.xflix.app.ui.theme.Motion
import com.xflix.app.ui.theme.Space
import com.xflix.app.ui.theme.XColors
import com.xflix.app.ui.theme.XType
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

private val AvatarSize = 120.dp
private const val MinSpinMs = 1100L

@Composable
fun ProfilesScreen(vm: XflixViewModel, onReady: () -> Unit) {
    val ready by rememberUpdatedState(onReady)

    var chosen by remember { mutableStateOf<Profile?>(null) }
    var origin by remember { mutableStateOf(Offset.Zero) }
    val tiles = remember { mutableStateMapOf<String, Rect>() }
    val move = remember { Animatable(0f) }
    val gridAlpha by animateFloatAsState(if (chosen == null) 1f else 0f, tween(220), label = "grid-alpha")

    LaunchedEffect(chosen) {
        val profile = chosen ?: return@LaunchedEffect
        val started = System.currentTimeMillis()
        vm.selectProfile(profile)
        move.animateTo(1f, tween(480, easing = Motion.Emphasized))
        snapshotFlow { vm.homeReady }.first { it }
        val elapsed = System.currentTimeMillis() - started
        if (elapsed < MinSpinMs) delay(MinSpinMs - elapsed)
        ready()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onGloballyPositioned { origin = it.positionInWindow() }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .graphicsLayer { alpha = gridAlpha },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = Space.xs),
            ) {
                XflixWordmark(Modifier.align(Alignment.Center), height = 24.dp)
            }
            Spacer(Modifier.height(56.dp))
            Text(
                stringResource(R.string.profiles_title),
                style = XType.Headline,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(Space.xl))
            Row(horizontalArrangement = Arrangement.spacedBy(Space.xl)) {
                Catalog.profiles.forEachIndexed { index, profile ->
                    ProfileTile(
                        profile = profile,
                        index = index,
                        hidden = chosen?.id == profile.id,
                        onPlaced = { tiles[profile.id] = it },
                        onClick = { if (chosen == null) chosen = profile },
                    )
                }
            }
        }

        val picked = chosen
        val start = picked?.let { tiles[it.id] }
        if (picked != null && start != null) {
            FlyingAvatar(picked, start.translate(-origin), move.value)
        }
    }
}

/** Avatar meluncur, lalu spinner */
@Composable
private fun FlyingAvatar(profile: Profile, start: Rect, progress: Float) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val width = with(density) { maxWidth.toPx() }
        val centerY = with(density) { maxHeight.toPx() } * 0.42f
        val endSize = start.width * 1.2f
        val size = lerp(start.width, endSize, progress)
        val x = lerp(start.left, (width - endSize) / 2f, progress)
        val y = lerp(start.top, centerY - endSize / 2f, progress)
        Box(
            Modifier
                .offset { IntOffset(x.roundToInt(), y.roundToInt()) }
                .size(with(density) { size.toDp() })
        ) {
            ProfileAvatar(profile, Modifier.fillMaxSize())
        }
        AnimatedVisibility(
            visible = progress >= 1f,
            enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.8f),
            modifier = Modifier.offset {
                val spinner = 48.dp.toPx()
                IntOffset(((width - spinner) / 2f).roundToInt(), (centerY + endSize / 2f + 36.dp.toPx()).roundToInt())
            },
        ) {
            XSpinner(size = 48.dp)
        }
    }
}

@Composable
private fun ProfileTile(
    profile: Profile,
    index: Int,
    hidden: Boolean,
    onPlaced: (Rect) -> Unit,
    onClick: () -> Unit,
) {
    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(70L * index)
        enter.animateTo(1f, tween(380, easing = Motion.EmphasizedDecelerate))
    }
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Column(
        Modifier
            .width(AvatarSize)
            .graphicsLayer {
                alpha = enter.value
                translationY = (1f - enter.value) * 16.dp.toPx()
            }
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(AvatarSize)
                .animatedPressScale(pressed, 0.94f)
                .onGloballyPositioned { onPlaced(it.boundsInWindow()) }
                .graphicsLayer { alpha = if (hidden) 0f else 1f },
            contentAlignment = Alignment.Center,
        ) {
            ProfileAvatar(profile, Modifier.fillMaxSize())
        }
        Spacer(Modifier.height(Space.s))
        Text(profile.name, style = XType.Body.copy(color = XColors.TextSecondary), maxLines = 1)
    }
}
