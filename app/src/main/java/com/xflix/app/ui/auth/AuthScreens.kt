package com.xflix.app.ui.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xflix.app.R
import com.xflix.app.XflixViewModel
import com.xflix.app.ui.components.BarIcon
import com.xflix.app.ui.components.LocalToaster
import com.xflix.app.ui.components.TextAction
import com.xflix.app.ui.components.XButton
import com.xflix.app.ui.components.XButtonStyle
import com.xflix.app.ui.components.XCheckbox
import com.xflix.app.ui.components.XTextField
import com.xflix.app.ui.components.XflixWordmark
import com.xflix.app.ui.icons.XIcons
import com.xflix.app.ui.theme.Motion
import com.xflix.app.ui.theme.Space
import com.xflix.app.ui.theme.XColors
import com.xflix.app.ui.theme.XType
import kotlinx.coroutines.launch

private suspend fun Animatable<Float, *>.shake(distance: Float) {
    animateTo(0f, keyframes {
        durationMillis = 420
        -distance at 50
        distance * 0.8f at 110
        -distance * 0.6f at 170
        distance * 0.4f at 230
        -distance * 0.2f at 300
        0f at 420
    })
}

@Composable
fun SignInScreen(
    vm: XflixViewModel,
    onBack: () -> Unit,
    onSignUp: () -> Unit,
    onSignedIn: () -> Unit,
) {
    var identity by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var reveal by rememberSaveable { mutableStateOf(false) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    val shake = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current
    val toaster = LocalToaster.current
    val shakePx = with(LocalDensity.current) { 12.dp.toPx() }
    val codeMessage = stringResource(R.string.toast_sign_in_code)
    val forgotMessage = stringResource(R.string.toast_forgot_password)

    // Error tampil setelah percobaan pertama
    val identityErr = if (submitted) identityError(identity) else null
    val passwordErr = if (submitted) signInPasswordError(password) else null

    fun submit() {
        submitted = true
        if (identityError(identity) != null || signInPasswordError(password) != null) {
            scope.launch { shake.shake(shakePx) }
            return
        }
        focus.clearFocus()
        loading = true
        scope.launch {
            vm.authenticate(identity.trim())
            loading = false
            onSignedIn()
        }
    }

    AuthScaffold(onBack = onBack) {
        Column(Modifier.graphicsLayer { translationX = shake.value }) {
            Text(stringResource(R.string.sign_in_title), style = XType.Headline.copy(fontSize = 26.sp, lineHeight = 32.sp))
            Spacer(Modifier.height(Space.xl))
            XTextField(
                value = identity,
                onValueChange = { identity = it },
                label = stringResource(R.string.field_identity),
                error = identityErr?.let { stringResource(it) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
            )
            Spacer(Modifier.height(Space.m))
            XTextField(
                value = password,
                onValueChange = { password = it },
                label = stringResource(R.string.field_password),
                password = !reveal,
                error = passwordErr?.let { stringResource(it) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                trailing = {
                    TextAction(
                        text = stringResource(if (reveal) R.string.action_hide else R.string.action_show),
                        onClick = { reveal = !reveal },
                        color = XColors.TextSecondary,
                    )
                },
            )
        }
        Spacer(Modifier.height(Space.xl))
        XButton(
            text = stringResource(R.string.action_sign_in),
            onClick = ::submit,
            style = XButtonStyle.Red,
            enabled = identity.isNotBlank() && password.isNotBlank(),
            loading = loading,
            height = 46.dp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(Space.l))
        Text(
            stringResource(R.string.label_or),
            style = XType.Caption.copy(letterSpacing = 1.sp),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(Space.l))
        XButton(
            text = stringResource(R.string.action_sign_in_code),
            onClick = { toaster.show(codeMessage) },
            style = XButtonStyle.Gray,
            height = 46.dp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(Space.l))
        TextAction(
            text = stringResource(R.string.action_forgot_password),
            onClick = { toaster.show(forgotMessage) },
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(Space.xl))
        FooterLink(
            prompt = stringResource(R.string.sign_up_prompt),
            action = stringResource(R.string.sign_up_link),
            onClick = onSignUp,
        )
    }
}

@Composable
fun SignUpScreen(
    vm: XflixViewModel,
    onBack: () -> Unit,
    onSignIn: () -> Unit,
    onSignedUp: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var reveal by rememberSaveable { mutableStateOf(false) }
    var optOut by rememberSaveable { mutableStateOf(false) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    val shake = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current
    val shakePx = with(LocalDensity.current) { 12.dp.toPx() }

    val emailErr = if (submitted) identityError(email) else null
    val passwordErr = if (submitted) signUpPasswordError(password) else null

    fun submit() {
        submitted = true
        if (identityError(email) != null || signUpPasswordError(password) != null) {
            scope.launch { shake.shake(shakePx) }
            return
        }
        focus.clearFocus()
        loading = true
        scope.launch {
            vm.authenticate(email.trim())
            loading = false
            onSignedUp()
        }
    }

    AuthScaffold(onBack = onBack, trailing = { TextAction(stringResource(R.string.action_sign_in_caps), onSignIn) }) {
        Text(stringResource(R.string.sign_up_step), style = XType.Overline.copy(color = XColors.TextSecondary))
        Spacer(Modifier.height(Space.s))
        Text(stringResource(R.string.sign_up_title), style = XType.Headline.copy(fontSize = 24.sp, lineHeight = 30.sp))
        Spacer(Modifier.height(Space.s))
        Text(stringResource(R.string.sign_up_body), style = XType.Body.copy(color = XColors.TextSecondary))
        Spacer(Modifier.height(Space.xl))
        Column(Modifier.graphicsLayer { translationX = shake.value }) {
            XTextField(
                value = email,
                onValueChange = { email = it },
                label = stringResource(R.string.field_email),
                error = emailErr?.let { stringResource(it) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
            )
            Spacer(Modifier.height(Space.m))
            XTextField(
                value = password,
                onValueChange = { password = it },
                label = stringResource(R.string.field_new_password),
                password = !reveal,
                error = passwordErr?.let { stringResource(it) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                trailing = {
                    TextAction(
                        text = stringResource(if (reveal) R.string.action_hide else R.string.action_show),
                        onClick = { reveal = !reveal },
                        color = XColors.TextSecondary,
                    )
                },
            )
        }
        Spacer(Modifier.height(10.dp))
        StrengthMeter(passwordStrength(password))
        Spacer(Modifier.height(Space.l))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(CircleShape)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { optOut = !optOut },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            XCheckbox(optOut)
            Spacer(Modifier.width(Space.m))
            Text(stringResource(R.string.sign_up_opt_out), style = XType.BodySmall)
        }
        Spacer(Modifier.height(Space.xl))
        XButton(
            text = stringResource(R.string.action_create_account),
            onClick = ::submit,
            style = XButtonStyle.Red,
            enabled = email.isNotBlank() && password.isNotBlank(),
            loading = loading,
            height = 46.dp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(Space.xl))
        FooterLink(
            prompt = stringResource(R.string.sign_in_prompt),
            action = stringResource(R.string.sign_in_link),
            onClick = onSignIn,
        )
    }
}

@Composable
private fun AuthScaffold(
    onBack: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = Space.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BarIcon(XIcons.Back, stringResource(R.string.action_back), onBack)
            Spacer(Modifier.width(Space.xs))
            XflixWordmark(height = 22.dp)
            Spacer(Modifier.weight(1f))
            trailing?.invoke()
        }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = Space.xl, bottom = Space.xl),
            content = content,
        )
    }
}

@Composable
private fun FooterLink(prompt: String, action: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Text(prompt, style = XType.Body.copy(color = XColors.TextSecondary))
        Text(
            action,
            style = XType.Body.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.clickable(onClick = onClick),
        )
    }
}

@Composable
private fun StrengthMeter(score: Int) {
    val labels = listOf(R.string.strength_empty, R.string.strength_weak, R.string.strength_medium, R.string.strength_strong)
    val tone = when (score) {
        0 -> XColors.TextDisabled
        1 -> XColors.Warning
        2 -> Color(0xFFE5C33B)
        else -> XColors.Positive
    }
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(3) { i ->
                val color by animateColorAsState(
                    targetValue = if (i < score) tone else XColors.SurfaceHigh,
                    animationSpec = tween(Motion.Medium, delayMillis = i * 40),
                    label = "strength",
                )
                Box(
                    Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(stringResource(labels[score]), style = XType.Caption.copy(color = if (score == 0) XColors.TextMuted else tone))
    }
}
