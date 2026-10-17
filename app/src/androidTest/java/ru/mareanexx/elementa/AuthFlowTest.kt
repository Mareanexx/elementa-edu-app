package ru.mareanexx.elementa

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.graphics.asAndroidBitmap
import android.graphics.Bitmap
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import java.io.File
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Проверяет навигацию, Keystore/DataStore, Retrofit и демо-транспорт на устройстве. */
class AuthFlowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    private fun button(text: String) = rule.onNode(hasText(text, substring = false) and hasClickAction())
    private fun field(label: String) = rule.onNode(hasText(label, substring = false) and hasSetTextAction())
    private fun waitFor(text: String) {
        rule.waitUntil(15_000) { rule.onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty() }
    }
    private fun capture(name: String) {
        val file = File(rule.activity.filesDir, "qa/$name.png")
        file.parentFile!!.mkdirs()
        file.outputStream().use { rule.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
    @Before fun guest() {
        rule.waitUntil(15_000) {
            rule.onAllNodesWithText("Зарегистрироваться").fetchSemanticsNodes().isNotEmpty() ||
                rule.onAllNodesWithText("Выйти").fetchSemanticsNodes().isNotEmpty()
        }
        if (rule.onAllNodesWithText("Выйти").fetchSemanticsNodes().isNotEmpty()) {
            button("Выйти").performClick()
            waitFor("Зарегистрироваться")
        }
    }

    @Test fun blankRegistrationShowsCaptions() {
        button("Зарегистрироваться").performClick()
        capture("registration-form")
        button("Зарегистрироваться").performScrollTo().performClick()
        rule.onAllNodesWithText("Заполни это поле").assertCountEquals(4)
        capture("registration-errors")
        rule.onNodeWithText("Уже есть аккаунт?").assertHasNoClickAction()
        button("Войти").performScrollTo().performClick()
        rule.onNodeWithText("С возвращением! Продолжим изучать химию?").assertIsDisplayed()
    }
    @Test fun invalidCredentialsKeepUserOnLogin() {
        button("Войти").performClick()
        capture("login-form")
        field("Имя пользователя").performTextInput("chemist")
        field("Пароль").performTextInput("Incorrect123")
        button("Войти").performScrollTo().performClick()
        waitFor("Неверное имя пользователя")
        rule.onNodeWithText("Неверное имя пользователя", substring = true).assertExists()
        rule.onAllNodesWithText("Главная").assertCountEquals(0)
        capture("login-error")
        rule.onNodeWithText("Нет аккаунта?").assertHasNoClickAction()
        button("Зарегистрироваться").performScrollTo().performClick()
        rule.onNodeWithText("Создать аккаунт").assertIsDisplayed()
    }
    @Test fun loginSucceedsAndLogoutReturnsToWelcome() {
        button("Войти").performClick()
        field("Имя пользователя").performTextInput("chemist")
        field("Пароль").performTextInput("Elementa123")
        button("Войти").performScrollTo().performClick()
        waitFor("Главная")
        rule.onNodeWithText("Главная").assertIsDisplayed()
        button("Выйти").performClick()
        waitFor("Твой химический ридер")
    }
    @Test fun registerThenLoginWithNewAccount() {
        val username = "qa_${System.nanoTime()}"
        button("Зарегистрироваться").performClick()
        field("Имя и фамилия").performTextInput("Elementa Chemist")
        field("Имя пользователя").performTextInput(username)
        field("Пароль").performTextInput("Elementa123")
        field("Подтвердите пароль").performScrollTo().performTextInput("Elementa123")
        button("Зарегистрироваться").performScrollTo().performClick()
        waitFor("Главная")
        button("Выйти").performClick()
        waitFor("Твой химический ридер")
        button("Войти").performClick()
        field("Имя пользователя").performTextInput(username)
        field("Пароль").performTextInput("Elementa123")
        button("Войти").performScrollTo().performClick()
        waitFor("Главная")
        button("Выйти").performClick()
        waitFor("Твой химический ридер")
    }
    @Test fun offlineErrorCanBeCorrectedAndRetried() {
        button("Войти").performClick()
        field("Имя пользователя").performTextInput("offline_user")
        field("Пароль").performTextInput("Elementa123")
        button("Войти").performScrollTo().performClick()
        waitFor("Не удалось подключиться")
        field("Имя пользователя").performTextReplacement("chemist")
        button("Войти").performScrollTo().performClick()
        waitFor("Главная")
        button("Выйти").performClick()
        waitFor("Твой химический ридер")
    }
    @Test fun persistSessionForRelaunch() {
        button("Войти").performClick()
        field("Имя пользователя").performTextInput("chemist")
        field("Пароль").performTextInput("Elementa123")
        button("Войти").performScrollTo().performClick()
        waitFor("Главная")
    }
    @Test fun leaveWelcomeForManualReview() {
        waitFor("Твой химический ридер")
    }
    @Test fun screensSlideForwardAndBackWithSixteenDpMargins() {
        try {
            rule.mainClock.autoAdvance = false
            button("Войти").performClick()
            rule.mainClock.advanceTimeByFrame()
            rule.mainClock.advanceTimeBy(80)
            val screenWidth = rule.onRoot().fetchSemanticsNode().boundsInRoot.width
            assertTrue(field("Имя пользователя").fetchSemanticsNode().boundsInRoot.left > screenWidth / 10)
            capture("login-slide-forward")
            rule.mainClock.advanceTimeBy(400)
            val inset = with(rule.density) { 16.dp.toPx() }
            assertEquals(inset, field("Имя пользователя").fetchSemanticsNode().boundsInRoot.left, 1f)

            rule.onNodeWithContentDescription("Назад").performClick()
            rule.mainClock.advanceTimeByFrame()
            rule.mainClock.advanceTimeBy(80)
            assertTrue(field("Имя пользователя").fetchSemanticsNode().boundsInRoot.left > screenWidth / 10)
            capture("login-slide-back")
            rule.mainClock.advanceTimeBy(400)
        } finally {
            rule.mainClock.autoAdvance = true
        }
        waitFor("Твой химический ридер")
    }
}
