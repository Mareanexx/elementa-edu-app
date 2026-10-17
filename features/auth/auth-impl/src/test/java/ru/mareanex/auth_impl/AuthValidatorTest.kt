package ru.mareanex.auth_impl

import org.junit.Assert.*
import org.junit.Test
import ru.mareanexx.auth_api.domain.model.*
import ru.mareanexx.auth_api.domain.validation.AuthValidator
import ru.mareanex.auth_impl.presentation.mvi.*

class AuthValidatorTest {
    private val valid = AuthForm("Мария Химик", "maria_chem", "Elementa123", "Elementa123")

    @Test fun `valid registration accepts Cyrillic name`() {
        assertTrue(AuthValidator.validate(valid, AuthMode.Register).isEmpty())
    }
    @Test fun `empty registration reports every required field`() {
        assertEquals(AuthField.entries.toSet(), AuthValidator.validate(AuthForm(), AuthMode.Register).keys)
    }
    @Test fun `login does not impose new registration password rules`() {
        assertTrue(AuthValidator.validate(AuthForm(username = "old", password = "abc"), AuthMode.Login).isEmpty())
    }
    @Test fun `registration rejects invalid usernames and weak passwords`() {
        for (username in listOf("ab", "1abc", "химик", "abc def", "x".repeat(33))) {
            assertEquals(AuthError.InvalidUsername, AuthValidator.validate(valid.copy(username = username), AuthMode.Register)[AuthField.Username])
        }
        for (password in listOf("abcdefghi", "123456789", "Abc12", "a1".repeat(65))) {
            assertEquals(AuthError.WeakPassword, AuthValidator.validate(valid.copy(password = password), AuthMode.Register)[AuthField.Password])
        }
    }
    @Test fun `password edit revalidates previously touched confirmation`() {
        val initial = AuthUiState(AuthMode.Register, form = valid, touched = setOf(AuthField.ConfirmPassword))
        val next = AuthReducer.changeField(initial, AuthField.Password, "OtherPass123")
        assertEquals(AuthError.PasswordMismatch, next.errors[AuthField.ConfirmPassword])
    }
    @Test fun `editing username clears conflict but keeps unrelated field errors`() {
        val initial = AuthUiState(AuthMode.Register, form = valid,
            errors = mapOf(AuthField.Username to AuthError.UsernameTaken, AuthField.FullName to AuthError.InvalidName))
        val next = AuthReducer.changeField(initial, AuthField.Username, "new_chemist")
        assertNull(next.errors[AuthField.Username])
        assertEquals(AuthError.InvalidName, next.errors[AuthField.FullName])
    }
}
