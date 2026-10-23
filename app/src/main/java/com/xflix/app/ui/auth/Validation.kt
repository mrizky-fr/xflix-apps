package com.xflix.app.ui.auth

import androidx.annotation.StringRes
import com.xflix.app.R

private val EmailPattern = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$")

/** Email atau HP 10–14 digit */
@StringRes
fun identityError(input: String): Int? {
    val value = input.trim()
    if (value.isEmpty()) return R.string.error_identity_empty
    val looksLikePhone = value.all { it.isDigit() || it == '+' || it == ' ' || it == '-' }
    if (looksLikePhone) {
        val digits = value.count { it.isDigit() }
        return if (digits in 10..14) null else R.string.error_phone
    }
    return if (EmailPattern.matches(value)) null else R.string.error_email
}

@StringRes
fun signInPasswordError(password: String): Int? =
    if (password.length in 4..60) null else R.string.error_password

@StringRes
fun signUpPasswordError(password: String): Int? = when {
    password.length < 8 -> R.string.error_password_short
    password.length > 60 -> R.string.error_password
    else -> null
}

/** Skor kekuatan sandi 0..3 */
fun passwordStrength(password: String): Int {
    if (password.isEmpty()) return 0
    var score = 0
    if (password.length >= 8) score++
    if (password.any { it.isDigit() } && password.any { it.isLetter() }) score++
    if (password.length >= 12 || password.any { !it.isLetterOrDigit() } || password.any { it.isUpperCase() }) score++
    return score
}
