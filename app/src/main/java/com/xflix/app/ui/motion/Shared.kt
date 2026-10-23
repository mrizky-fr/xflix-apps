package com.xflix.app.ui.motion

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xflix.app.ui.theme.Motion

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedScope = compositionLocalOf<SharedTransitionScope?> { null }

val LocalNavScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

@OptIn(ExperimentalSharedTransitionApi::class)
private val boundsTransform = BoundsTransform { _, _ ->
    tween(durationMillis = 420, easing = Motion.Emphasized)
}

/** Tanpa key, tidak berefek */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedTitle(key: String?, corner: Dp = 6.dp): Modifier {
    val shared = LocalSharedScope.current
    val nav = LocalNavScope.current
    if (key == null || shared == null || nav == null) return this
    return with(shared) {
        this@sharedTitle.sharedBounds(
            sharedContentState = rememberSharedContentState(key),
            animatedVisibilityScope = nav,
            enter = fadeIn(tween(260)),
            exit = fadeOut(tween(260)),
            boundsTransform = boundsTransform,
            clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(corner)),
        )
    }
}
