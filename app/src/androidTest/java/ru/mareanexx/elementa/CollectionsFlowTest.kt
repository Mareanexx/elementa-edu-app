package ru.mareanexx.elementa

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.provider.MediaStore
import androidx.core.content.FileProvider
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
import ru.mareanexx.collections_api.domain.model.*

/** Сценарии проходят через MVI и Retrofit; каждый тест использует отдельный аккаунт. */
class CollectionsFlowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context: Context get() = instrumentation.targetContext
    private val container get() = (context.applicationContext as ElementaApplication).container
    private val monitors = mutableListOf<Instrumentation.ActivityMonitor>()
    private lateinit var user: AuthUser
    private lateinit var account: String
    private val repository get() = container.collectionsRepository(user)
    private fun button(text: String) = rule.onNode(hasText(text, substring = false) and hasClickAction())
    private fun waitFor(text: String) {
        rule.waitUntil(15_000) { rule.onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty() }
    }
    private fun waitTag(tag: String) {
        try {
            rule.waitUntil(15_000) { rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }
        } catch (failure: Throwable) {
            capture("collections-tag-timeout")
            File(context.filesDir, "qa/collections-tag-timeout.txt").writeText(rule.onRoot().printToString())
            throw failure
        }
    }
    private fun capture(name: String) {
        rule.waitForIdle()
        val file = File(context.filesDir, "qa/$name.png").apply { parentFile!!.mkdirs() }
        file.outputStream().use { instrumentation.uiAutomation.takeScreenshot().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
    private fun fixture(count: Int = 8) {
        val rows: List<Map<String, Any>> = listOf(mapOf<String, Any>("id" to "favorites", "name" to "Избранное", "coverKey" to "chemistry", "favorite" to true, "bookIds" to listOf("book_1"))) +
            (1..count).map { index -> mapOf<String, Any>("id" to "c$index", "name" to "Подборка ${index.toString().padStart(2, '0')}", "coverKey" to "molecules", "favorite" to false,
                "bookIds" to if (index % 2 == 0) emptyList() else (1..minOf(index, 12)).map { "book_$it" }) }
        context.getSharedPreferences("collections_demo_server", Context.MODE_PRIVATE).edit().putString("collections_${user.id}", Gson().toJson(rows)).commit()
    }
    @Before fun setup() {
        context.getSharedPreferences("collections_mock_controls", Context.MODE_PRIVATE).edit().clear().commit()
        runBlocking { container.authRepository.clearSession() }
        waitFor("Зарегистрироваться")
        account = "qa_collections_${System.nanoTime()}"
        assertEquals(AuthResult.Success, runBlocking { container.authRepository.register("Химик QA", account, "Elementa123") })
        user = runBlocking { container.authRepository.session.first()!!.user }
        fixture()
        waitFor("Главная")
        button("Коллекции").performClick()
        waitTag("collection_c1")
    }
    @After fun cleanup() {
        monitors.forEach(instrumentation::removeMonitor)
        runCatching { capture("collections-at-cleanup"); File(context.filesDir, "qa/collections-at-cleanup.txt").writeText(rule.onRoot().printToString()) }
        context.getSharedPreferences("collections_mock_controls", Context.MODE_PRIVATE).edit().clear().commit()
        if (::user.isInitialized) {
            runBlocking { container.authRepository.clearSession() }
            val accounts = context.getSharedPreferences("auth_demo_accounts", Context.MODE_PRIVATE)
            val editor = accounts.edit()
            accounts.all.forEach { (key, value) ->
                if (value is String && key.startsWith("qa_collections_")) {
                    val id = runCatching { JsonParser.parseString(value).asJsonObject.get("id").asString }.getOrNull()
                    editor.remove(key)
                    if (id != null) {
                        File(context.filesDir, "demo_collection_covers/$id").deleteRecursively()
                        context.getSharedPreferences("collections_demo_server", Context.MODE_PRIVATE).edit().remove("collections_$id").remove("preferences_$id").commit()
                        context.getSharedPreferences("collections_preferences", Context.MODE_PRIVATE).edit().remove("preferences_$id").commit()
                    }
                }
            }
            editor.commit()
            context.getSharedPreferences("collections_demo_server", Context.MODE_PRIVATE).edit().remove("collections_${user.id}").remove("preferences_${user.id}").commit()
            context.getSharedPreferences("collections_preferences", Context.MODE_PRIVATE).edit().remove("preferences_${user.id}").commit()
        }
    }
    @Test fun gridHasTwoColumnsAndScrollsWithFavoriteHero() {
        val a = rule.onNodeWithTag("collection_c1").fetchSemanticsNode().boundsInRoot
        val b = rule.onNodeWithTag("collection_c2").fetchSemanticsNode().boundsInRoot
        assertEquals(a.top, b.top, 1f)
        assertEquals(a.width, b.width, 1f)
        assertTrue(b.left > a.right)
        val inset = with(rule.density) { androidx.compose.ui.unit.Dp(16f).toPx() }
        assertEquals(inset, a.left, 1f)
        button("Открыть").assertIsDisplayed()
        capture("collections-overview")
        rule.onNodeWithTag("collections_grid").performScrollToNode(hasTestTag("collection_c8"))
        rule.onNodeWithTag("collection_c8").assertIsDisplayed()
    }
    @Test fun createValidatesNameAndCanAddAndRemoveBooks() {
        rule.onNodeWithContentDescription("Создать коллекцию").performClick()
        button("Добавить").performClick()
        rule.onNodeWithText("Напиши название коллекции").assertExists()
        rule.onNodeWithTag("collection_name").performTextReplacement("Подборка 01")
        button("Добавить").performClick()
        waitFor("Коллекция с таким названием уже есть")
        rule.onNodeWithTag("collection_name").performTextReplacement("Новая химия")
        rule.onNodeWithContentDescription("Космос").performClick()
        button("Добавить").performClick()
        waitFor("Здесь будут твои книги")
        rule.onNodeWithText("Новая химия").assertExists()
        rule.onNodeWithTag("bottom_navigation").assertDoesNotExist()
        button("Добавить книги").performClick()
        waitTag("picker_book_book_1")
        rule.onNodeWithTag("picker_book_book_1").performClick()
        button("Добавить · 1").performClick()
        waitTag("collection_book_book_1")
        capture("collection-books")
        val created = (runBlocking { repository.getCollections() } as CollectionsResult.Success).value.first { it.name == "Новая химия" }
        assertEquals(CollectionCover.Space, created.cover)
        assertEquals(1, created.bookCount)
        rule.onNodeWithTag("collection_book_open_book_1").performClick()
        rule.onNodeWithText("Закрыть").assertDoesNotExist()
        rule.onNodeWithContentDescription("Действия с книгой «Органическая химия»").performClick()
        button("Убрать из коллекции").performClick()
        button("Убрать").performClick()
        waitFor("Здесь будут твои книги")
        assertEquals(12, (runBlocking { repository.getLibrary() } as CollectionsResult.Success).value.size)
    }
    @Test fun bookCardsHaveLargeCoversRatingAndScrollableTagsWithoutBackground() {
        rule.onNodeWithTag("collection_c1").performClick()
        waitTag("collection_book_book_1")
        val cover = rule.onNodeWithTag("collection_book_cover_book_1", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        assertEquals(with(rule.density) { androidx.compose.ui.unit.Dp(112f).toPx() }, cover.width, 1f)
        assertEquals(with(rule.density) { androidx.compose.ui.unit.Dp(168f).toPx() }, cover.height, 1f)
        val card = rule.onNodeWithTag("collection_book_book_1").fetchSemanticsNode().boundsInRoot
        assertTrue(card.height >= with(rule.density) { androidx.compose.ui.unit.Dp(210f).toPx() })
        rule.onNodeWithContentDescription("Рейтинг: 4,2 из 5. Оценок: 64", useUnmergedTree = true).assertIsDisplayed()
        val tags = rule.onNodeWithTag("collection_tags_book_1", useUnmergedTree = true)
        assertTrue(tags.fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.HorizontalScrollAxisRange].maxValue() > 0f)
        tags.performTouchInput { swipeLeft() }
        rule.onNodeWithText("Самостоятельное изучение", useUnmergedTree = true).assertIsDisplayed()
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        val y = (card.top + with(rule.density) { androidx.compose.ui.unit.Dp(4f).toPx() }).toInt()
        assertEquals(android.graphics.Color.rgb(251, 250, 254), bitmap.getPixel(card.center.x.toInt(), y))
        capture("collection-book-card-large")
    }
    @Test fun overflowRenamesAndDeletesWithoutRemovingLibraryBooks() {
        rule.onNodeWithContentDescription("Действия с коллекцией «Подборка 01»").performClick()
        button("Редактировать").performClick()
        rule.onNodeWithTag("collection_name").performTextReplacement("Атомы")
        button("Сохранить").performClick()
        waitFor("Коллекция обновлена")
        rule.onNodeWithContentDescription("Действия с коллекцией «Атомы»").performClick()
        button("Удалить").performClick()
        waitFor("Удалить коллекцию?")
        button("Удалить").performClick()
        waitFor("Коллекция удалена")
        rule.onNodeWithTag("collection_c1").assertDoesNotExist()
        assertEquals(12, (runBlocking { repository.getLibrary() } as CollectionsResult.Success).value.size)
    }
    @Test fun allScreenSortsAndSettingsAreSavedThroughBackend() {
        button("Показать все").performClick()
        waitTag("collection_favorites")
        rule.onNodeWithTag("bottom_navigation").assertDoesNotExist()
        rule.onNodeWithContentDescription("Сортировка").performClick()
        button("Больше книг").performClick()
        rule.waitUntil(10_000) { runBlocking { repository.preferences.first().sort == CollectionSort.BooksDescending } }
        waitTag("collection_c7")
        val favorite = rule.onNodeWithTag("collection_favorites").fetchSemanticsNode().boundsInRoot
        val most = rule.onNodeWithTag("collection_c7").fetchSemanticsNode().boundsInRoot
        assertEquals(favorite.top, most.top, 1f)
        assertTrue(favorite.left < most.left)
        rule.onNodeWithContentDescription("Назад").performClick()
        rule.onNodeWithContentDescription("Настройки коллекций").performClick()
        rule.onAllNodes(isToggleable()).onFirst().performClick()
        rule.waitUntil(10_000) { !runBlocking { repository.preferences.first().showEmpty } }
        val remote = context.getSharedPreferences("collections_demo_server", Context.MODE_PRIVATE).getString("preferences_${user.id}", null)!!
        assertTrue(remote.contains("books_desc"))
        assertTrue(remote.contains("false"))
        instrumentation.uiAutomation.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK)
        rule.waitForIdle()
        capture("collections-hidden-empty")
        rule.activityRule.scenario.recreate()
        waitTag("collection_c7")
        rule.onNodeWithTag("collection_c2").assertDoesNotExist()
    }
    @Test fun failedRequestKeepsEditorAndCanBeRetried() {
        rule.onNodeWithContentDescription("Создать коллекцию").performClick()
        rule.onNodeWithTag("collection_name").performTextReplacement("После ошибки")
        context.getSharedPreferences("collections_mock_controls", Context.MODE_PRIVATE).edit().putInt("next_status", 503).commit()
        button("Добавить").performClick()
        waitFor("Сервис временно недоступен")
        rule.onNodeWithTag("collection_name").assertTextContains("После ошибки")
        capture("collections-request-error")
        button("Добавить").performClick()
        waitFor("Здесь будут твои книги")
    }
    @Test fun offlineListRetriesAndUnauthorizedRequestReturnsToLogin() {
        button("Главная").performClick()
        context.getSharedPreferences("collections_mock_controls", Context.MODE_PRIVATE).edit().putInt("next_status", -1).commit()
        button("Коллекции").performClick()
        waitFor("Нет подключения")
        button("Повторить").assertIsDisplayed().performClick()
        try {
            rule.waitUntil(10_000) { rule.onAllNodesWithText("Нет подключения", substring = true).fetchSemanticsNodes().isEmpty() }
        } catch (failure: Throwable) {
            capture("collections-offline-timeout")
            File(context.filesDir, "qa/collections-offline-timeout.txt").writeText(rule.onRoot().printToString())
            throw failure
        }
        waitTag("collection_c1")
        rule.waitUntil(10_000) { rule.onAllNodesWithTag("content_sync_indicator").fetchSemanticsNodes().isEmpty() }
        rule.onNodeWithContentDescription("Создать коллекцию").performClick()
        rule.onNodeWithTag("collection_name").performTextReplacement("Новая")
        context.getSharedPreferences("collections_mock_controls", Context.MODE_PRIVATE).edit().putInt("next_status", 401).commit()
        button("Добавить").performClick()
        waitFor("Твой химический ридер")
        assertEquals(AuthResult.Success, runBlocking { container.authRepository.login(account, "Elementa123") })
        waitFor("Главная")
        button("Коллекции").performClick()
        waitTag("collection_c1")
        rule.onNodeWithText("Мои коллекции").assertIsDisplayed()
    }
    @Test fun backendProtectsFavoriteDeduplicatesBooksAndEnforcesThirtyCollections() {
        assertEquals(CollectionsResult.Failure(CollectionsError.Protected), runBlocking { repository.delete("favorites") })
        assertEquals(CollectionsResult.Failure(CollectionsError.Protected), runBlocking { repository.update("favorites", CollectionForm("Иное")) })
        assertEquals(CollectionsResult.Success(Unit), runBlocking { repository.addBooks("favorites", setOf("book_1")) })
        assertEquals(1, (runBlocking { repository.getBooks("favorites") } as CollectionsResult.Success).value.size)
        fixture(30)
        assertEquals(CollectionsResult.Failure(CollectionsError.LimitReached), runBlocking { repository.create(CollectionForm("Лишняя")) })
        button("Главная").performClick()
        button("Коллекции").performClick()
        rule.waitUntil(10_000) { rule.onAllNodesWithContentDescription("Создать коллекцию").fetchSemanticsNodes().isNotEmpty() }
        rule.waitForIdle()
        button("Показать все").performClick()
        waitFor("Создано 30 из 30")
        rule.onNodeWithTag("collections_grid").performScrollToNode(hasTestTag("collection_c30"))
        rule.onNodeWithContentDescription("Создать коллекцию").performClick()
        waitFor("Можно создать не больше 30 коллекций")
        rule.onNodeWithTag("collection_name").assertDoesNotExist()
    }
    @Test fun bookTapStopsBeforeTagsAndShowsReadingProgress() {
        rule.onNodeWithTag("collection_c1").performClick()
        waitTag("collection_book_book_1")
        val open = rule.onNodeWithTag("collection_book_open_book_1")
        val tags = rule.onNodeWithTag("collection_tags_book_1", useUnmergedTree = true)
        assertTrue(open.fetchSemanticsNode().boundsInRoot.bottom < tags.fetchSemanticsNode().boundsInRoot.top)
        rule.onNodeWithText("48 из 160 стр.", useUnmergedTree = true).assertIsDisplayed()
        open.performClick()
        rule.onNodeWithText("Закрыть").assertDoesNotExist()
        rule.onNodeWithTag("collection_book_book_1").assertHasNoClickAction()
        tags.performTouchInput { swipeLeft() }
        rule.onNodeWithTag("collection_book_book_1").assertExists()
        capture("collection-reading-progress")
    }
    @Test fun swipeRemovesMembershipAndUndoRestoresIt() {
        rule.onNodeWithTag("collection_c1").performClick()
        waitTag("collection_book_book_1")
        rule.onNodeWithTag("collection_book_swipe_book_1").performTouchInput { swipeLeft() }
        waitFor("Книга убрана из коллекции")
        rule.onNodeWithText("Убрать книгу из подборки?").assertDoesNotExist()
        assertTrue((runBlocking { repository.getBooks("c1") } as CollectionsResult.Success).value.isEmpty())
        capture("collection-swipe-undo")
        button("Отменить").performClick()
        waitTag("collection_book_book_1")
        assertEquals(1, (runBlocking { repository.getBooks("c1") } as CollectionsResult.Success).value.size)
    }
    @Test fun failedSwipeReturnsBookToVisibleList() {
        rule.onNodeWithTag("collection_c1").performClick()
        waitTag("collection_book_book_1")
        context.getSharedPreferences("collections_mock_controls", Context.MODE_PRIVATE).edit().putInt("next_status", 503).commit()
        rule.onNodeWithTag("collection_book_swipe_book_1").performTouchInput { swipeLeft() }
        waitFor("Сервис временно недоступен")
        waitTag("collection_book_book_1")
        rule.onNodeWithTag("collection_book_open_book_1").assertIsDisplayed()
        assertEquals(1, (runBlocking { repository.getBooks("c1") } as CollectionsResult.Success).value.size)
    }
    @Test fun pickerKeepsPositionWhileTypingAndSelecting() {
        rule.onNodeWithTag("collection_c1").performClick()
        waitTag("collection_book_book_1")
        rule.onNodeWithContentDescription("Добавить книги").performClick()
        waitTag("picker_book_book_3")
        val search = rule.onNodeWithTag("collection_search")
        search.performClick()
        rule.waitForIdle()
        val top = search.fetchSemanticsNode().boundsInRoot.top
        rule.mainClock.autoAdvance = false
        try {
            search.performTextInput("Кв")
            rule.mainClock.advanceTimeByFrame()
            rule.mainClock.advanceTimeBy(64)
            assertEquals(top, search.fetchSemanticsNode().boundsInRoot.top, 1f)
            search.performTextInput("антовая")
            rule.mainClock.advanceTimeByFrame()
            rule.mainClock.advanceTimeBy(64)
            assertEquals(top, search.fetchSemanticsNode().boundsInRoot.top, 1f)
            rule.onNodeWithTag("picker_book_book_3").performClick()
            rule.mainClock.advanceTimeByFrame()
            rule.mainClock.advanceTimeBy(64)
            assertEquals(top, search.fetchSemanticsNode().boundsInRoot.top, 1f)
            button("Добавить · 1").assertExists()
        } finally { rule.mainClock.autoAdvance = true }
        capture("collection-picker-stable")
        button("Добавить · 1").performClick()
        waitTag("collection_book_book_3")
    }
    @Test fun photoCoverUploadsPersistsAndCanBeReplacedWithPreset() {
        val monitor = object : Instrumentation.ActivityMonitor() {
            override fun onStartActivity(intent: Intent): Instrumentation.ActivityResult? {
                if (intent.action != MediaStore.ACTION_PICK_IMAGES) return null
                val file = File(context.cacheDir, "avatar_capture/qa_collection.png").apply { parentFile!!.mkdirs() }
                val bitmap = Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888).apply { eraseColor(android.graphics.Color.rgb(80, 160, 180)) }
                file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                bitmap.recycle()
                return Instrumentation.ActivityResult(Activity.RESULT_OK, Intent().setData(FileProvider.getUriForFile(context, "${context.packageName}.files", file)))
            }
        }
        monitors += monitor
        instrumentation.addMonitor(monitor)
        rule.onNodeWithContentDescription("Создать коллекцию").performClick()
        rule.onNodeWithTag("collection_name").performTextReplacement("Фото коллекции")
        button("Выбрать своё фото").performScrollTo().performClick()
        waitTag("collection_cover_preview")
        capture("collection-photo-cover")
        button("Добавить").performScrollTo().performClick()
        waitFor("Здесь будут твои книги")
        val created = (runBlocking { repository.getCollections() } as CollectionsResult.Success).value.first { it.name == "Фото коллекции" }
        assertNotNull(created.image)
        val image = android.graphics.BitmapFactory.decodeFile(android.net.Uri.parse(created.image!!.url).path)
        assertEquals(1280, image.width)
        assertEquals(800, image.height)
        image.recycle()
        rule.onNodeWithContentDescription("Назад").performClick()
        rule.onNodeWithTag("collections_grid").performScrollToNode(hasTestTag("collection_${created.id}"))
        rule.onNodeWithContentDescription("Действия с коллекцией «Фото коллекции»").performClick()
        button("Редактировать").performClick()
        waitTag("collection_cover_preview")
        rule.onNodeWithContentDescription("Космос").performScrollTo().performClick()
        button("Сохранить").performScrollTo().performClick()
        waitFor("Коллекция обновлена")
        val updated = (runBlocking { repository.getCollections() } as CollectionsResult.Success).value.first { it.id == created.id }
        assertNull(updated.image)
        assertEquals(CollectionCover.Space, updated.cover)
    }

}
