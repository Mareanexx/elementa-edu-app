package ru.mareanexx.elementa

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import java.io.File
import ru.mareanexx.auth_api.domain.model.AuthResult
import ru.mareanexx.auth_api.domain.model.AuthUser
import ru.mareanexx.user_profile_api.domain.model.AppSettings
import ru.mareanexx.user_profile_api.domain.model.ProfileResult

/** Проверяет реальные MVI, Retrofit, локальное сохранение и контракты выбора фото на устройстве. */
class ProfileFlowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val monitors = mutableListOf<Instrumentation.ActivityMonitor>()
    private lateinit var user: AuthUser
    private lateinit var account: String
    private val context: Context get() = instrumentation.targetContext
    private val container get() = (context.applicationContext as ElementaApplication).container
    private fun button(text: String) = rule.onNode(hasText(text, substring = false) and hasClickAction())
    private fun waitFor(text: String) {
        try {
            rule.waitUntil(15_000) { rule.onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty() }
        } catch (failure: Throwable) {
            capture("profile-timeout")
            File(context.filesDir, "qa/profile-timeout.txt").writeText(rule.onRoot().printToString())
            throw failure
        }
    }
    private fun SemanticsNodeInteraction.enabled(): Boolean = runCatching { !fetchSemanticsNode().config.contains(SemanticsProperties.Disabled) }.getOrDefault(false)
    private fun field(name: String) = rule.onNodeWithTag("profile_field_$name")
    private fun profile() = runBlocking { (container.profileRepository(user).getProfile() as ProfileResult.Success).value }
    private fun capture(name: String) {
        rule.waitForIdle()
        val file = File(context.filesDir, "qa/$name.png").apply { parentFile!!.mkdirs() }
        file.outputStream().use { instrumentation.uiAutomation.takeScreenshot().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Before fun setup() {
        context.getSharedPreferences("profile_mock_controls", Context.MODE_PRIVATE).edit().clear().commit()
        runBlocking { container.authRepository.clearSession() }
        waitFor("Зарегистрироваться")
        account = "qa_profile_${System.nanoTime()}"
        assertEquals(AuthResult.Success, runBlocking { container.authRepository.register("Мария Химик", account, "Elementa123") })
        user = runBlocking { container.authRepository.session.first()!!.user }
        val data = mapOf("id" to user.id, "full_name" to "Мария Химик", "username" to account, "description" to "Изучаю химию по книгам",
            "statistics" to mapOf("books_read" to 24, "notes_published" to 41, "tests_passed" to 8))
        context.getSharedPreferences("profile_demo_server", Context.MODE_PRIVATE).edit().putString("profile_${user.id}", Gson().toJson(data)).commit()
        waitFor("Главная")
        openProfile()
    }

    @After fun cleanup() {
        runCatching {
            capture("profile-at-cleanup")
            File(context.filesDir, "qa/profile-at-cleanup.txt").writeText(rule.onRoot().printToString())
        }
        monitors.forEach(instrumentation::removeMonitor)
        context.getSharedPreferences("profile_mock_controls", Context.MODE_PRIVATE).edit().clear().commit()
        if (::user.isInitialized) {
            runCatching { runBlocking { container.profileRepository(user).removeAvatar() } }
            runBlocking { container.authRepository.clearSession() }
            val accounts = context.getSharedPreferences("auth_demo_accounts", Context.MODE_PRIVATE)
            val editor = accounts.edit()
            accounts.all.forEach { (key, value) ->
                if (value is String && runCatching { JsonParser.parseString(value).asJsonObject.get("id").asString == user.id }.getOrDefault(false)) editor.remove(key)
            }
            editor.commit()
            context.getSharedPreferences("profile_demo_server", Context.MODE_PRIVATE).edit().remove("profile_${user.id}").remove("settings_${user.id}").commit()
            context.getSharedPreferences("elementa_settings", Context.MODE_PRIVATE).edit().remove("settings_${user.id}").commit()
        }
    }

    private fun openProfile() {
        button("Профиль").performClick()
        waitFor("Изучаю химию по книгам")
        rule.waitUntil(15_000) { rule.onAllNodesWithText("Достижения", substring = false).fetchSemanticsNodes().isNotEmpty() }
    }

    @Test fun profileShowsDataStatisticsAndBottomSheet() {
        rule.onNodeWithText("Мария Химик").assertIsDisplayed()
        rule.onNodeWithText("@$account").assertIsDisplayed()
        rule.onNodeWithText("24").assertIsDisplayed()
        rule.onNodeWithText("41").assertIsDisplayed()
        rule.onNodeWithContentDescription("Редактировать профиль").assertIsDisplayed()
        rule.onNodeWithContentDescription("Изменить фото").assertIsDisplayed()
        val profileTab = button("Профиль").fetchSemanticsNode().boundsInRoot
        assertTrue(profileTab.left > rule.onRoot().fetchSemanticsNode().boundsInRoot.width * 0.7f)
        val preview = rule.onNodeWithTag("achievements_preview")
        val bounds = preview.fetchSemanticsNode().boundsInRoot
        val cards = preview.onChildren().fetchSemanticsNodes()
        assertEquals(4, cards.size)
        cards.forEach { assertEquals(cards.first().boundsInRoot.width, it.boundsInRoot.width, 1f) }
        assertEquals(bounds.left, cards.first().boundsInRoot.left, 1f)
        assertEquals(bounds.right, cards.last().boundsInRoot.right, 1f)
        capture("profile-overview")
        assertSafeAreas(bottomColor = Color.rgb(251, 250, 254))
    }

    @Test fun editValidatesFieldsAndRetriesConflict() {
        rule.onNodeWithContentDescription("Редактировать профиль").performClick()
        rule.waitForIdle()
        assertSafeAreas(topColor = Color.WHITE, bottomColor = Color.WHITE)
        field("FullName").performTextClearance()
        button("Сохранить изменения").performScrollTo().performClick()
        rule.onNodeWithText("Заполни это поле").assertExists()
        field("FullName").performTextInput("Мария Химик QA")
        field("Username").performTextReplacement("taken_user")
        button("Сохранить изменения").performScrollTo().performClick()
        waitFor("Это имя пользователя уже занято")
        capture("profile-edit-error")
        field("Username").performTextReplacement(account)
        field("Description").performTextReplacement("Новый текст профиля")
        button("Сохранить изменения").performScrollTo().performClick()
        waitFor("Новый текст профиля")
        assertEquals("Мария Химик QA", profile().fullName)
        assertEquals("Новый текст профиля", profile().description)
    }

    @Test fun descriptionPasteIsLimitedTo250AndCanBeSaved() {
        rule.onNodeWithContentDescription("Редактировать профиль").performClick()
        val text = "Химия ".repeat(50).take(250)
        field("Description").performTextReplacement("Химия ".repeat(50))
        field("Description").assertTextContains(text)
        rule.onNodeWithText("250 / 250").performScrollTo().assertIsDisplayed()
        button("Сохранить изменения").performScrollTo().performClick()
        waitFor("Мой профиль")
        assertEquals(text, profile().description)
    }

    @Test fun unsavedEditsAskBeforeLeaving() {
        rule.onNodeWithContentDescription("Редактировать профиль").performClick()
        field("Description").performTextReplacement("Несохранённое изменение")
        rule.onNodeWithContentDescription("Назад").performClick()
        waitFor("Не сохранять изменения?")
        button("Продолжить").performClick()
        field("Description").assertTextContains("Несохранённое изменение")
        rule.onNodeWithContentDescription("Назад").performClick()
        button("Не сохранять").performClick()
        waitFor("Изучаю химию по книгам")
        assertEquals("Изучаю химию по книгам", profile().description)
    }

    @Test fun editedUsernameWorksForNextLogin() {
        val changed = "qa_renamed_${System.nanoTime()}"
        rule.onNodeWithContentDescription("Редактировать профиль").performClick()
        field("Username").performTextReplacement(changed)
        button("Сохранить изменения").performScrollTo().performClick()
        waitFor("@$changed")
        runBlocking { container.authRepository.clearSession() }
        waitFor("Зарегистрироваться")
        button("Войти").performClick()
        rule.onNode(hasText("Имя пользователя") and hasSetTextAction()).performTextInput(changed)
        rule.onNode(hasText("Пароль") and hasSetTextAction()).performTextInput("Elementa123")
        button("Войти").performScrollTo().performClick()
        waitFor("Главная")
        assertEquals(user.id, runBlocking { container.authRepository.session.first()!!.user.id })
    }

    @Test fun achievementsShowCategoriesProgressAndReceivedDetails() {
        button("Показать все").performClick()
        waitFor("Каждое открытие — шаг вперёд")
        capture("profile-achievements")
        val first = button("Первая глава").fetchSemanticsNode().boundsInRoot
        val second = button("Книжный химик").fetchSemanticsNode().boundsInRoot
        assertEquals(first.width, second.width, 1f)
        assertEquals(first.height, second.height, 1f)
        assertEquals(first.top, second.top, 1f)
        button("Чтение").performClick()
        rule.onNodeWithTag("achievements_grid").performScrollToNode(hasText("Библиотека знаний"))
        button("Библиотека знаний").performClick()
        rule.onNodeWithText("Пока не получено").assertIsDisplayed()
        rule.onNodeWithText("До цели осталось: 26").assertIsDisplayed()
        capture("profile-achievement-locked")
        button("Понятно").performClick()
        rule.onNodeWithTag("achievements_grid").performScrollToNode(hasText("Первая глава"))
        button("Первая глава").performClick()
        rule.onNodeWithText("Достижение получено").assertIsDisplayed()
        button("Понятно").performClick()
    }

    @Test fun bottomSheetSwipeOpensFullAchievementsScreen() {
        rule.onRoot().performTouchInput {
            swipe(Offset(centerX, height - with(rule.density) { 220.dp.toPx() }), Offset(centerX, 120f), 600)
        }
        waitFor("Каждое открытие — шаг вперёд")
        rule.onNodeWithContentDescription("Назад").performClick()
        waitFor("Мой профиль")
        rule.mainClock.advanceTimeBy(1_000)
        rule.waitForIdle()
        rule.onNodeWithText("Мой профиль").assertIsDisplayed()
        rule.onNodeWithText("Изучаю химию по книгам").assertIsDisplayed()
    }

    private fun assertSafeAreas(topColor: Int? = null, bottomColor: Int) {
        rule.waitForIdle()
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        val insets = ViewCompat.getRootWindowInsets(rule.activity.window.decorView)!!
            .getInsets(WindowInsetsCompat.Type.systemBars())
        if (topColor != null && insets.top > 0) assertEquals(topColor, bitmap.getPixel(5, insets.top / 2))
        if (insets.bottom > 0) assertEquals(bottomColor, bitmap.getPixel(5, bitmap.height - insets.bottom / 2))
    }

    @Test fun achievementPanelTransformsInBothDirectionsAndTabsSwipe() {
        val initialTop = rule.onNodeWithTag("achievements_sheet").fetchSemanticsNode().boundsInRoot.top
        rule.mainClock.autoAdvance = false
        try {
            button("Показать все").performClick()
            rule.waitForIdle()
            rule.mainClock.advanceTimeByFrame()
            rule.mainClock.advanceTimeBy(180)
            val middleTop = rule.onNodeWithTag("achievements_sheet").fetchSemanticsNode().boundsInRoot.top
            assertTrue("Панель должна заметно подняться в промежуточном кадре: $initialTop → $middleTop",
                middleTop > 0f && middleTop < initialTop - with(rule.density) { 64.dp.toPx() })
            capture("profile-sheet-expanding")
            rule.mainClock.advanceTimeBy(1_000)
            val expandedTop = rule.onNodeWithTag("achievements_sheet").fetchSemanticsNode().boundsInRoot.top
            assertTrue(expandedTop < middleTop)
            rule.onNodeWithContentDescription("Назад").performClick()
            rule.waitForIdle()
            rule.mainClock.advanceTimeByFrame()
            rule.mainClock.advanceTimeBy(180)
            val closingTop = rule.onNodeWithTag("achievements_sheet").fetchSemanticsNode().boundsInRoot.top
            assertTrue("Панель должна заметно опуститься в промежуточном кадре: $expandedTop → $closingTop",
                closingTop > expandedTop + with(rule.density) { 64.dp.toPx() } && closingTop < initialTop)
            capture("profile-sheet-collapsing")
            rule.mainClock.advanceTimeBy(1_000)
        } finally { rule.mainClock.autoAdvance = true }
        rule.onNodeWithText("Мой профиль").assertIsDisplayed()
        button("Показать все").performClick()
        button("Чтение").performClick()
        button("Чтение").assertIsSelected()
        rule.onNodeWithTag("achievements_pager").performTouchInput { swipeLeft() }
        button("Заметки").assertIsSelected()
        rule.onNodeWithTag("achievements_grid").performScrollToNode(hasText("Есть мысль"))
        button("Есть мысль").assertIsDisplayed()
    }

    @Test fun settingsApplyAndSurviveActivityRecreation() {
        rule.onNodeWithContentDescription("Настройки приложения").performClick()
        waitFor("Учись в своём ритме")
        rule.waitUntil(15_000) { rule.onNodeWithContentDescription("Уменьшение анимации").enabled() }
        capture("profile-settings")
        rule.onNodeWithContentDescription("Уменьшение анимации").performScrollTo().performClick()
        rule.waitUntil(15_000) { runBlocking { container.profileRepository(user).settings.first().reduceMotion } }
        rule.onNodeWithContentDescription("Не выключать экран").performScrollTo().performClick()
        rule.waitUntil(15_000) { runBlocking { container.profileRepository(user).settings.first().keepScreenOn } }
        rule.onNodeWithTag("profile_text_scale").performScrollTo().performSemanticsAction(SemanticsActions.SetProgress) { it(1.1f) }
        rule.waitUntil(15_000) { runBlocking { container.profileRepository(user).settings.first().textScale == 1.1f } }
        rule.runOnIdle {
            fun keptOn(view: android.view.View): Boolean = view.keepScreenOn ||
                (view is android.view.ViewGroup && (0 until view.childCount).any { keptOn(view.getChildAt(it)) })
            assertTrue(keptOn(rule.activity.window.decorView))
        }
        rule.activityRule.scenario.recreate()
        waitFor("Учись в своём ритме")
        rule.onNodeWithContentDescription("Уменьшение анимации").assertIsOn()
        rule.onNodeWithContentDescription("Не выключать экран").assertIsOn()
        assertEquals(1.1f, runBlocking { container.profileRepository(user).settings.first().textScale })
        rule.waitUntil(15_000) { button("Очистить кеш").enabled() }
        rule.onNodeWithText("Очистить кеш").performScrollTo().performClick()
        button("Очистить").performClick()
        waitFor("Кеш изображений очищен.")
        assertEquals("Мария Химик", profile().fullName)
    }

    @Test fun serverErrorKeepsEditsAndAllowsRetry() {
        rule.onNodeWithContentDescription("Редактировать профиль").performClick()
        field("Description").performTextReplacement("Текст после повтора")
        context.getSharedPreferences("profile_mock_controls", Context.MODE_PRIVATE).edit().putInt("next_status", 503).commit()
        button("Сохранить изменения").performScrollTo().performClick()
        waitFor("Сервис временно недоступен")
        field("Description").assertTextContains("Текст после повтора")
        button("Сохранить изменения").performScrollTo().performClick()
        waitFor("Текст после повтора")
    }

    @Test fun unauthorizedResponseReturnsToGuestScreens() {
        rule.onNodeWithContentDescription("Редактировать профиль").performClick()
        field("Description").performTextReplacement("Не должно сохраниться")
        context.getSharedPreferences("profile_mock_controls", Context.MODE_PRIVATE).edit().putInt("next_status", 401).commit()
        button("Сохранить изменения").performScrollTo().performClick()
        waitFor("Твой химический ридер")
        rule.onNodeWithText("Зарегистрироваться").assertIsDisplayed()
        assertEquals(AuthResult.Success, runBlocking { container.authRepository.login(account, "Elementa123") })
        waitFor("Главная")
        openProfile()
    }

    @Test fun galleryPhotoIsEditedUploadedAndRemoved() { photoFlow(camera = false) }
    @Test fun cameraPhotoUsesOutputUriAndCanBeUploaded() { photoFlow(camera = true) }

    private fun photoFlow(camera: Boolean) {
        val monitor = object : Instrumentation.ActivityMonitor() {
            override fun onStartActivity(intent: Intent): Instrumentation.ActivityResult? {
                return when (intent.action) {
                    MediaStore.ACTION_PICK_IMAGES -> {
                        val file = File(context.cacheDir, "avatar_capture/qa_gallery.png").apply { parentFile!!.mkdirs() }
                        file.outputStream().use { fixture().compress(Bitmap.CompressFormat.PNG, 100, it) }
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
                        Instrumentation.ActivityResult(Activity.RESULT_OK, Intent().setData(uri))
                    }
                    MediaStore.ACTION_IMAGE_CAPTURE -> {
                        val uri = intent.getParcelableExtra(MediaStore.EXTRA_OUTPUT, Uri::class.java)!!
                        context.contentResolver.openOutputStream(uri)!!.use { fixture().compress(Bitmap.CompressFormat.JPEG, 95, it) }
                        Instrumentation.ActivityResult(Activity.RESULT_OK, null)
                    }
                    else -> null
                }
            }
        }
        monitors += monitor
        instrumentation.addMonitor(monitor)
        rule.onNodeWithContentDescription("Изменить фото").performClick()
        button(if (camera) "Сделать фото" else "Выбрать из галереи").performClick()
        waitFor("Выбери лучший ракурс")
        rule.waitUntil(15_000) { button("Повернуть на 90°").enabled() }
        button("Повернуть на 90°").performClick()
        rule.waitUntil(15_000) { button("Повернуть на 90°").enabled() }
        rule.onNodeWithTag("avatar_zoom").performSemanticsAction(SemanticsActions.SetProgress) { it(1.5f) }
        capture(if (camera) "profile-camera-editor" else "profile-gallery-editor")
        button("Сохранить фото").performScrollTo().performClick()
        waitFor("Мой профиль")
        val stored = profile().avatarUrl!!
        val image = BitmapFactory.decodeFile(File(java.net.URI(stored)).absolutePath)
        assertEquals(512, image.width)
        assertEquals(512, image.height)
        capture("profile-with-photo")
        rule.onNodeWithContentDescription("Изменить фото").performClick()
        button("Выбрать из галереи").performClick()
        waitFor("Выбери лучший ракурс")
        rule.onNodeWithContentDescription("Назад").performClick()
        waitFor("Мой профиль")
        assertEquals(stored, profile().avatarUrl)
        rule.onNodeWithContentDescription("Изменить фото").performClick()
        button("Удалить фото").performClick()
        button("Удалить").performClick()
        rule.waitUntil(15_000) { profile().avatarUrl == null }
    }

    private fun fixture(): Bitmap = Bitmap.createBitmap(900, 600, Bitmap.Config.ARGB_8888).also { bitmap ->
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.rgb(231, 231, 241))
        val paint = Paint().apply { color = Color.rgb(90, 85, 215) }
        canvas.drawRect(0f, 0f, 300f, 600f, paint)
        paint.color = Color.rgb(230, 145, 155)
        canvas.drawCircle(600f, 300f, 180f, paint)
    }
}
