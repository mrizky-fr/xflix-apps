package com.xflix.app.ui.auth

import com.xflix.app.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidationTest {

    @Test
    fun emailValid() {
        assertNull(identityError("raka@xflix.id"))
        assertNull(identityError("  nadia.k@mail.co.id  "))
    }

    @Test
    fun emailInvalid() {
        assertEquals(R.string.error_email, identityError("raka@xflix"))
        assertEquals(R.string.error_email, identityError("raka xflix.id"))
        assertEquals(R.string.error_identity_empty, identityError("   "))
    }

    @Test
    fun phoneLength() {
        assertNull(identityError("0812 3456 7890"))
        assertNull(identityError("+62 812-3456-789"))
        assertEquals(R.string.error_phone, identityError("08123"))
        assertEquals(R.string.error_phone, identityError("0812345678901234"))
    }

    @Test
    fun signInPasswordBounds() {
        assertEquals(R.string.error_password, signInPasswordError("abc"))
        assertNull(signInPasswordError("abcd"))
        assertNull(signInPasswordError("a".repeat(60)))
        assertEquals(R.string.error_password, signInPasswordError("a".repeat(61)))
    }

    @Test
    fun signUpPasswordNeedsEight() {
        assertEquals(R.string.error_password_short, signUpPasswordError("1234567"))
        assertNull(signUpPasswordError("12345678"))
    }

    @Test
    fun strengthGrows() {
        assertEquals(0, passwordStrength(""))
        assertEquals(1, passwordStrength("abcdefgh"))
        assertEquals(2, passwordStrength("abcdefg1"))
        assertEquals(3, passwordStrength("Abcdefg1"))
    }
}
