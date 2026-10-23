package com.xflix.app

import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.xflix.app.data.Catalog
import com.xflix.app.data.Kind
import com.xflix.app.data.Profile
import com.xflix.app.data.Title
import com.xflix.app.ui.auth.OnboardingScreen
import com.xflix.app.ui.auth.SignInScreen
import com.xflix.app.ui.auth.SignUpScreen
import com.xflix.app.ui.components.LocalToaster
import com.xflix.app.ui.components.ProfileAvatar
import com.xflix.app.ui.components.ToastHost
import com.xflix.app.ui.components.Toaster
import com.xflix.app.ui.detail.DetailScreen
import com.xflix.app.ui.home.HomeScreen
import com.xflix.app.ui.icons.XIcons
import com.xflix.app.ui.intro.IntroScreen
import com.xflix.app.ui.motion.LocalNavScope
import com.xflix.app.ui.motion.LocalSharedScope
import com.xflix.app.ui.my.DownloadsScreen
import com.xflix.app.ui.my.MyXflixScreen
import com.xflix.app.ui.newhot.NewHotScreen
import com.xflix.app.ui.player.PlayerScreen
import com.xflix.app.ui.profiles.ProfilesScreen
import com.xflix.app.ui.search.SearchScreen
import com.xflix.app.ui.theme.Dimens
import com.xflix.app.ui.theme.Motion
import com.xflix.app.ui.theme.XType

object Routes {
    const val Intro = "intro"
    const val Onboarding = "onboarding"
    const val SignIn = "signin"
    const val SignUp = "signup"
    const val Profiles = "profiles"
    const val Home = "home"
    const val NewHot = "newhot"
    const val My = "my"
    const val Search = "search"
    const val Downloads = "downloads"
    const val Detail = "detail/{id}?key={key}"
    const val Player = "player/{id}?s={s}&e={e}"

    fun detail(id: String, key: String?) = if (key == null) "detail/$id" else "detail/$id?key=${Uri.encode(key)}"
    fun player(id: String, season: Int, episode: Int) = "player/$id?s=$season&e=$episode"
}

enum class Tab(val route: String, @param:StringRes val label: Int) {
    Home(Routes.Home, R.string.nav_home),
    NewHot(Routes.NewHot, R.string.nav_new_hot),
    My(Routes.My, R.string.nav_my),
}

private typealias Transition<T> = AnimatedContentTransitionScope<NavBackStackEntry>.() -> T

// Layar dorong, geser dari kanan
private val pushEnter: Transition<EnterTransition> = {
    slideInHorizontally(tween(Motion.Long, easing = Motion.EmphasizedDecelerate)) { it / 5 } + fadeIn(tween(Motion.Medium))
}
private val pushPopExit: Transition<ExitTransition> = {
    slideOutHorizontally(tween(Motion.Medium, easing = Motion.EmphasizedAccelerate)) { it / 5 } + fadeOut(tween(Motion.Short))
}

private fun NavGraphBuilder.screen(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    push: Boolean = false,
    content: @Composable (NavBackStackEntry) -> Unit,
) {
    composable(
        route = route,
        arguments = arguments,
        enterTransition = if (push) pushEnter else null,
        popExitTransition = if (push) pushPopExit else null,
    ) { entry ->
        CompositionLocalProvider(LocalNavScope provides this) { content(entry) }
    }
}

private fun NavHostController.resetTo(route: String) = navigate(route) {
    popUpTo(graph.id) { inclusive = true }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun XflixApp() {
    val vm: XflixViewModel = viewModel()
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()
    val toaster = remember { Toaster(scope) }
    val entry by nav.currentBackStackEntryAsState()
    val tab = Tab.entries.firstOrNull { it.route == entry?.destination?.route }
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomPadding = Dimens.BottomBarHeight + navBarBottom

    val openTitle: (Title, String?) -> Unit = { title, key -> nav.navigate(Routes.detail(title.id, key)) }
    // Serial lanjut dari episode terakhir
    val play: (Title) -> Unit = { title ->
        val resume = Catalog.continueWatching.firstOrNull { it.titleId == title.id }
        val season = if (title.kind == Kind.Series) resume?.season ?: 1 else 1
        val episode = if (title.kind == Kind.Series) resume?.episode ?: 1 else 1
        nav.navigate(Routes.player(title.id, season, episode))
    }

    CompositionLocalProvider(LocalToaster provides toaster) {
        SharedTransitionLayout(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            CompositionLocalProvider(LocalSharedScope provides this) {
                Box(Modifier.fillMaxSize()) {
                    NavHost(
                        navController = nav,
                        startDestination = Routes.Intro,
                        modifier = Modifier.fillMaxSize(),
                        enterTransition = { fadeIn(tween(Motion.Medium)) },
                        exitTransition = { fadeOut(tween(Motion.Short)) },
                        popEnterTransition = { fadeIn(tween(Motion.Medium)) },
                        popExitTransition = { fadeOut(tween(Motion.Short)) },
                    ) {
                        screen(Routes.Intro) {
                            IntroScreen(onFinished = {
                                nav.resetTo(if (vm.account == null) Routes.Onboarding else Routes.Profiles)
                            })
                        }
                        screen(Routes.Onboarding) {
                            OnboardingScreen(
                                onSignIn = { nav.navigate(Routes.SignIn) },
                                onGetStarted = { nav.navigate(Routes.SignUp) },
                            )
                        }
                        screen(Routes.SignIn, push = true) {
                            SignInScreen(
                                vm = vm,
                                onBack = { nav.popBackStack() },
                                onSignUp = { nav.navigate(Routes.SignUp) { popUpTo(Routes.Onboarding) } },
                                onSignedIn = { nav.resetTo(Routes.Profiles) },
                            )
                        }
                        screen(Routes.SignUp, push = true) {
                            SignUpScreen(
                                vm = vm,
                                onBack = { nav.popBackStack() },
                                onSignIn = { nav.navigate(Routes.SignIn) { popUpTo(Routes.Onboarding) } },
                                onSignedUp = { nav.resetTo(Routes.Profiles) },
                            )
                        }
                        screen(Routes.Profiles) {
                            ProfilesScreen(vm, onReady = { nav.resetTo(Routes.Home) })
                        }
                        screen(Routes.Home) {
                            HomeScreen(
                                vm = vm,
                                bottomPadding = bottomPadding,
                                onOpenTitle = openTitle,
                                onPlay = play,
                                onSearch = { nav.navigate(Routes.Search) },
                                onDownloads = { nav.navigate(Routes.Downloads) },
                            )
                        }
                        screen(Routes.NewHot) {
                            NewHotScreen(
                                vm = vm,
                                bottomPadding = bottomPadding,
                                onOpenTitle = { openTitle(it, null) },
                                onPlay = play,
                                onSearch = { nav.navigate(Routes.Search) },
                            )
                        }
                        screen(Routes.My) {
                            MyXflixScreen(
                                vm = vm,
                                bottomPadding = bottomPadding,
                                onSwitchProfile = { nav.navigate(Routes.Profiles) },
                                onOpenTitle = openTitle,
                                onDownloads = { nav.navigate(Routes.Downloads) },
                                onSearch = { nav.navigate(Routes.Search) },
                                onSignOut = {
                                    vm.signOut()
                                    nav.resetTo(Routes.Onboarding)
                                },
                            )
                        }
                        screen(Routes.Search, push = true) {
                            SearchScreen(
                                onBack = { nav.popBackStack() },
                                onOpenTitle = openTitle,
                                onPlay = play,
                            )
                        }
                        screen(Routes.Downloads, push = true) {
                            DownloadsScreen(
                                vm = vm,
                                onBack = { nav.popBackStack() },
                                onOpenTitle = { openTitle(it, null) },
                                onPlay = play,
                                onFindMore = { nav.popBackStack(Routes.Home, inclusive = false) },
                            )
                        }
                        screen(
                            route = Routes.Detail,
                            arguments = listOf(
                                navArgument("id") { type = NavType.StringType },
                                navArgument("key") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                },
                            ),
                        ) { e ->
                            DetailScreen(
                                id = e.arguments?.getString("id").orEmpty(),
                                sharedKey = e.arguments?.getString("key"),
                                vm = vm,
                                onBack = { nav.popBackStack() },
                                onPlay = { title, season, episode -> nav.navigate(Routes.player(title.id, season, episode)) },
                                onOpenTitle = openTitle,
                            )
                        }
                        screen(
                            route = Routes.Player,
                            arguments = listOf(
                                navArgument("id") { type = NavType.StringType },
                                navArgument("s") {
                                    type = NavType.IntType
                                    defaultValue = 1
                                },
                                navArgument("e") {
                                    type = NavType.IntType
                                    defaultValue = 1
                                },
                            ),
                        ) { e ->
                            PlayerScreen(
                                id = e.arguments?.getString("id").orEmpty(),
                                startSeason = e.arguments?.getInt("s") ?: 1,
                                startEpisode = e.arguments?.getInt("e") ?: 1,
                                onBack = { nav.popBackStack() },
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = tab != null && !vm.overlayOpen,
                        modifier = Modifier.align(Alignment.BottomCenter),
                        enter = slideInVertically(tween(Motion.Long, easing = Motion.EmphasizedDecelerate)) { it } + fadeIn(),
                        exit = slideOutVertically(tween(Motion.Medium)) { it } + fadeOut(),
                    ) {
                        XBottomBar(
                            current = tab ?: Tab.Home,
                            profile = vm.profile,
                            onSelect = { target ->
                                if (target != tab) {
                                    nav.navigate(target.route) {
                                        popUpTo(Routes.Home) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                        )
                    }

                    ToastHost(
                        toaster = toaster,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = if (tab != null) bottomPadding + 12.dp else navBarBottom + 24.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun XBottomBar(current: Tab, profile: Profile?, onSelect: (Tab) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color.Black)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(Dimens.BottomBarHeight)
        ) {
            Tab.entries.forEach { tab ->
                BottomItem(
                    tab = tab,
                    selected = tab == current,
                    profile = profile,
                    onClick = { onSelect(tab) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

@Composable
private fun BottomItem(tab: Tab, selected: Boolean, profile: Profile?, onClick: () -> Unit, modifier: Modifier) {
    // Ikon memantul kecil saat dipilih
    val bounce = remember { Animatable(1f) }
    LaunchedEffect(selected) {
        if (selected) {
            bounce.snapTo(0.82f)
            bounce.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 500f))
        }
    }
    val color by animateColorAsState(if (selected) Color.White else Color(0xFF8C8C8C), tween(Motion.Short), label = "tab-color")
    Column(
        modifier
            .fillMaxHeight()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .size(24.dp)
                .graphicsLayer {
                    scaleX = bounce.value
                    scaleY = bounce.value
                },
            contentAlignment = Alignment.Center,
        ) {
            when (tab) {
                Tab.Home -> Icon(if (selected) XIcons.HomeFilled else XIcons.Home, null, tint = color, modifier = Modifier.size(22.dp))
                Tab.NewHot -> Icon(if (selected) XIcons.NewHotFilled else XIcons.NewHot, null, tint = color, modifier = Modifier.size(22.dp))
                Tab.My -> if (profile != null) {
                    ProfileAvatar(
                        profile,
                        Modifier
                            .size(22.dp)
                            .border(1.5.dp, if (selected) Color.White else Color.Transparent, RoundedCornerShape(4.dp)),
                        corner = 4.dp,
                    )
                } else {
                    Icon(XIcons.User, null, tint = color, modifier = Modifier.size(22.dp))
                }
            }
        }
        Spacer(Modifier.height(3.dp))
        Text(
            stringResource(tab.label),
            style = XType.Caption.copy(
                fontSize = 10.5.sp,
                color = color,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            ),
        )
    }
}
