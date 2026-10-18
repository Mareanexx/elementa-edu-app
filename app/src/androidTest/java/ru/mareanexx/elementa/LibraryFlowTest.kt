package ru.mareanexx.elementa

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import ru.mareanexx.auth_api.domain.model.AuthResult
import ru.mareanexx.auth_api.domain.model.AuthUser
import ru.mareanexx.book_api.domain.model.BookResult
import ru.mareanexx.collections_api.domain.model.*
import ru.mareanexx.user_library_api.domain.model.*
import java.io.File

/** Проверки используют отдельные аккаунты и общий с книгой и коллекциями HTTP-мок. */
class LibraryFlowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context: Context get() = instrumentation.targetContext
    private val container get() = (context.applicationContext as ElementaApplication).container
    private val controls get() = context.getSharedPreferences("library_mock_controls", Context.MODE_PRIVATE)
    private lateinit var user: AuthUser
    private lateinit var account: String
    private lateinit var collectionId: String
    private val library get() = container.libraryRepository(user)
    private val books get() = container.bookRepository(user)
    private val collections get() = container.collectionsRepository(user)
    private fun button(text: String) = rule.onNode(hasText(text) and hasClickAction())
    private fun waitText(text: String) = rule.waitUntil(15_000) { rule.onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty() }
    private fun waitTag(tag: String) {
        try { rule.waitUntil(15_000) { rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() } }
        catch (failure: Throwable) { capture("library-timeout"); throw failure }
    }
    private fun capture(name: String) {
        rule.waitForIdle(); instrumentation.waitForIdleSync(); android.os.SystemClock.sleep(350)
        val dir = File(context.filesDir, "qa").apply { mkdirs() }
        File(dir, "$name.txt").writeText(rule.onAllNodes(isRoot()).onLast().printToString())
        File(dir, "$name.png").outputStream().use { instrumentation.uiAutomation.takeScreenshot().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
    private fun tab(category: LibraryCategory) {
        rule.onNodeWithTag("library_tab_${category.name}").performScrollTo().performClick()
        waitTag("library_list_${category.name}")
        rule.waitForIdle()
    }
    private fun refresh() {
        val category = LibraryCategory.entries.first { rule.onAllNodes(hasTestTag("library_tab_${it.name}") and isSelected()).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("library_list_${category.name}").performScrollToIndex(0)
        rule.onNodeWithTag("library_list_${category.name}").performTouchInput { swipeDown(startY = height * .12f, endY = height * .9f, durationMillis = 700) }
    }
    private fun query(text: String) {
        rule.onNodeWithTag("library_search").performTextReplacement(text)
        rule.onNodeWithTag("library_search").performImeAction()
    }
    private fun currentBook(id: String = "book_1") = (runBlocking { books.getBook(id) } as BookResult.Success).value
    @Before fun setup() {
        controls.edit().clear().putInt("delay_ms", 100).commit()
        context.getSharedPreferences("book_mock_controls", Context.MODE_PRIVATE).edit().clear().commit()
        runBlocking { container.authRepository.clearSession() }
        waitText("Зарегистрироваться")
        account = "qa_library_${System.nanoTime()}"
        assertEquals(AuthResult.Success, runBlocking { container.authRepository.register("Мария Химик", account, "Elementa123") })
        user = runBlocking { container.authRepository.session.first()!!.user }
        assertTrue(runBlocking { library.setReadingStatus("book_3", ReadingStatus.Completed) } is LibraryResult.Success)
        assertTrue(runBlocking { library.setReadingStatus("book_5", ReadingStatus.Completed) } is LibraryResult.Success)
        assertTrue(runBlocking { books.setWantToRead("book_6", true) } is BookResult.Success)
        assertTrue(runBlocking { books.setFavorite("book_1", true) } is BookResult.Success)
        collectionId = (runBlocking { collections.create(CollectionForm("Практикум")) } as CollectionsResult.Success).value.id
        waitText("Главная")
        button("Библиотека").performClick()
        waitTag("library_book_book_1")
        waitText("Моя библиотека")
    }
    @After fun cleanup() {
        runCatching { capture("library-at-cleanup") }
        controls.edit().clear().commit()
        context.getSharedPreferences("book_mock_controls", Context.MODE_PRIVATE).edit().clear().commit()
        if (::user.isInitialized) {
            runBlocking { container.authRepository.clearSession() }
            context.getSharedPreferences("auth_demo_accounts", Context.MODE_PRIVATE).edit().remove(account).commit()
            val shared = context.getSharedPreferences("collections_demo_server", Context.MODE_PRIVATE)
            val editor = shared.edit().remove("collections_${user.id}").remove("preferences_${user.id}")
            shared.all.keys.filter { it.startsWith("progress_${user.id}_") }.forEach(editor::remove)
            editor.commit()
            val demo = context.getSharedPreferences("library_demo_server", Context.MODE_PRIVATE)
            val demoEditor = demo.edit().remove("sort_${user.id}")
            demo.all.keys.filter { it.startsWith("touched_${user.id}_") }.forEach(demoEditor::remove)
            demoEditor.commit()
            context.getSharedPreferences("library_search_history", Context.MODE_PRIVATE).edit().remove("queries_${user.id}").commit()
            context.getSharedPreferences("collections_preferences", Context.MODE_PRIVATE).edit().remove("preferences_${user.id}").commit()
        }
    }
    @Test fun overviewCarouselAndCompletedGridHaveDifferentLayouts() {
        val card = rule.onNodeWithTag("library_book_book_1").fetchSemanticsNode().boundsInRoot
        assertEquals(with(rule.density) { androidx.compose.ui.unit.Dp(16f).toPx() }, card.left, 1f)
        capture("library-overview")
        rule.onNodeWithTag("library_list_All").performScrollToNode(hasTestTag("library_tile_book_7"))
        val left = rule.onNodeWithTag("library_tile_book_7").fetchSemanticsNode().boundsInRoot
        val right = rule.onNodeWithTag("library_tile_book_8").fetchSemanticsNode().boundsInRoot
        assertEquals(left.top, right.top, 1f)
        assertEquals(left.width, right.width, 1f)
        capture("library-grid")
        tab(LibraryCategory.InProgress)
        rule.onNodeWithTag("library_book_book_1").assertIsDisplayed()
        rule.onNodeWithTag("library_pager").performTouchInput { swipeLeft() }
        waitTag("library_tile_book_3")
        rule.onNodeWithTag("library_tab_Completed").assertIsSelected()
        rule.onNodeWithTag("library_completed_carousel").assertDoesNotExist()
        val first = rule.onNodeWithTag("library_tile_book_3").fetchSemanticsNode().boundsInRoot
        val second = rule.onNodeWithTag("library_tile_book_5").fetchSemanticsNode().boundsInRoot
        assertEquals(first.top, second.top, 1f)
        assertEquals(first.width, second.width, 1f)
        capture("library-completed")
        tab(LibraryCategory.WantToRead)
        waitTag("library_tile_book_6")
        rule.onNodeWithTag("library_tile_book_6").assertIsDisplayed()
        capture("library-planned")
    }
    @Test fun completedShelfCardsHaveEqualHeightWithDifferentAuthorLengths() {
        assertTrue(runBlocking { library.setReadingStatus("book_1", ReadingStatus.Completed) } is LibraryResult.Success)
        assertTrue(runBlocking { library.setReadingStatus("book_2", ReadingStatus.Completed) } is LibraryResult.Success)
        refresh()
        waitTag("library_tile_book_1")
        val first = rule.onNodeWithTag("library_tile_book_1").fetchSemanticsNode().boundsInRoot
        val second = rule.onNodeWithTag("library_tile_book_2").fetchSemanticsNode().boundsInRoot
        assertEquals(first.top, second.top, 1f)
        assertEquals(first.height, second.height, 1f)
        val firstAction = rule.onNodeWithTag("library_read_book_1").fetchSemanticsNode().boundsInRoot
        val secondAction = rule.onNodeWithTag("library_read_book_2").fetchSemanticsNode().boundsInRoot
        assertEquals(firstAction.bottom, secondAction.bottom, 1f)
        capture("library-uniform-completed-shelf")
    }
    @Test fun scrollingCollapsesSearchAndSearchButtonRestoresInput() {
        val expandedTabs = rule.onNodeWithTag("library_tab_All").fetchSemanticsNode().boundsInRoot.top
        rule.onNodeWithTag("library_list_All").performTouchInput { swipeUp(durationMillis = 600) }
        rule.waitUntil(10_000) { rule.onAllNodesWithTag("library_search").fetchSemanticsNodes().isEmpty() }
        val compactTabs = rule.onNodeWithTag("library_tab_All").fetchSemanticsNode().boundsInRoot.top
        assertTrue(expandedTabs - compactTabs > with(rule.density) { androidx.compose.ui.unit.Dp(60f).toPx() })
        capture("library-search-collapsed")
        rule.onNodeWithTag("library_search_toggle").performClick()
        waitTag("library_search")
        rule.onNodeWithTag("library_search").assertIsFocused()
        query("Космос")
        waitTag("library_book_book_3")
        query("хим")
        waitTag("library_book_book_1")
        rule.onNodeWithTag("library_list_All").performTouchInput { swipeUp(durationMillis = 600) }
        rule.waitUntil(10_000) { rule.onAllNodesWithTag("library_search").fetchSemanticsNodes().isEmpty() }
        rule.onNodeWithTag("library_search_toggle").performClick()
        waitTag("library_search")
        rule.onNodeWithTag("library_search").assertTextEquals("хим")
    }
    @Test fun searchUsesBackendForTitlesAuthorsTopicsAndStoresOnlySubmittedQueries() {
        rule.onNodeWithTag("library_search").performTextReplacement("К")
        waitText("Введи хотя бы 2 символа")
        assertEquals(0, controls.getInt("search_requests", 0))
        query("  Космос  ")
        waitTag("library_book_book_3")
        waitText("Найдено книг: 1")
        assertEquals("Космос", controls.getString("last_query", null))
        rule.onNodeWithTag("bottom_navigation").assertIsDisplayed()
        capture("library-search-topic")
        query("Галочкин")
        waitTag("library_book_book_1")
        query("полимеров")
        waitTag("library_book_book_11")
        query("несуществующаякнига")
        waitText("По этому запросу книг нет")
        capture("library-search-empty")
        rule.onNodeWithContentDescription("Очистить поиск").performClick()
        rule.onNodeWithTag("library_search").performClick()
        waitText("Недавние запросы")
        button("Космос").assertExists()
        button("Очистить историю").performClick()
        rule.waitUntil(10_000) { runBlocking { library.recentQueries.first().isEmpty() } }
    }
    @Test fun readerArrowReturnsDirectlyToLibraryAndBookBodyOpensDetails() {
        rule.onNodeWithTag("library_read_book_1").performClick()
        waitTag("reader_ready")
        rule.onNodeWithTag("reader_close").performClick()
        waitTag("library_book_book_1")
        rule.onNodeWithTag("book_content").assertDoesNotExist()
        rule.onNodeWithTag("library_book_book_1").performClick()
        waitTag("book_content")
        rule.onNodeWithText("О книге").assertIsDisplayed()
        rule.onNodeWithContentDescription("Назад").performClick()
        waitTag("library_book_book_1")
    }
    @Test fun statusErrorsKeepConfirmationAndSuccessfulChangesReachBookAndCollections() {
        tab(LibraryCategory.InProgress)
        rule.onNodeWithTag("library_menu_book_1").performClick()
        button("Отметить прочитанной").performClick()
        waitText("Книга уже прочитана?")
        controls.edit().putInt("next_status", 503).commit()
        button("Прочитано").performClick()
        waitText("Сервис временно недоступен")
        button("Прочитано").performClick()
        waitText("Книга отмечена прочитанной")
        assertEquals(160, currentBook().pagesRead)
        assertEquals(160, (runBlocking { collections.getLibrary() } as CollectionsResult.Success).value.first { it.id == "book_1" }.pagesRead)
        tab(LibraryCategory.Completed)
        waitTag("library_tile_book_1")
        rule.onNodeWithTag("library_menu_book_1").performClick()
        button("Сбросить прогресс").performClick()
        waitText("Начать сначала?")
        button("Начать сначала").performClick()
        waitText("Прогресс сброшен")
        assertEquals(0, currentBook().pagesRead)
    }
    @Test fun bookMenuSharesFavoriteReadingListAndCustomCollectionWithDetails() {
        rule.onNodeWithTag("library_menu_book_1").performClick()
        button("Убрать из избранного").performClick()
        waitText("Книга убрана из избранного")
        assertFalse(currentBook().shelves.favorite)
        rule.onNodeWithTag("library_menu_book_1").performClick()
        button("Хочу прочитать").performClick()
        waitText("Добавлено в «Хочу прочитать»")
        assertTrue(currentBook().shelves.wantToRead)
        rule.onNodeWithTag("library_menu_book_1").performClick()
        button("В коллекцию").performClick()
        waitText("Выбрать коллекции")
        waitText("Практикум")
        button("Практикум").performClick()
        button("Сохранить").performClick()
        waitText("Подборки обновлены")
        assertTrue((runBlocking { collections.getBooks(collectionId) } as CollectionsResult.Success).value.any { it.id == "book_1" })
        waitTag("library_book_book_1")
        rule.onNodeWithTag("book_content").assertDoesNotExist()
        tab(LibraryCategory.WantToRead)
        waitTag("library_tile_book_1")
    }
    @Test fun toolbarSortPersistsAndPullToRefreshKeepsContentWithoutSkeleton() {
        rule.onNodeWithContentDescription("Настроить библиотеку").performClick()
        button("Обновить").assertDoesNotExist()
        button("Сортировка").performClick()
        button("По названию").performClick()
        rule.waitUntil(10_000) { (runBlocking { library.getPreferences() } as LibraryResult.Success).value.sort == LibrarySort.Title }
        rule.onNodeWithTag("library_book_book_1").assertExists()
        controls.edit().putInt("delay_ms", 1000).commit()
        refresh()
        waitTag("library_refresh_indicator")
        rule.onNodeWithTag("library_skeleton").assertDoesNotExist()
        rule.onNodeWithTag("library_book_book_1").assertIsDisplayed()
        rule.waitUntil(10_000) { rule.onAllNodesWithTag("library_refresh_indicator").fetchSemanticsNodes().isEmpty() }
        controls.edit().putInt("delay_ms", 100).putInt("next_status", -1).commit()
        refresh()
        waitText("Нет подключения")
        rule.onNodeWithTag("library_book_book_1").assertIsDisplayed()
        button("Обновить").performClick()
        rule.waitUntil(10_000) { rule.onAllNodesWithTag("library_refresh_indicator").fetchSemanticsNodes().isEmpty() }
        rule.activityRule.scenario.recreate()
        waitTag("library_book_book_1")
        assertEquals(LibrarySort.Title, (runBlocking { library.getPreferences() } as LibraryResult.Success).value.sort)
    }
    @Test fun emptyLibraryCanRefreshAndExpiredSessionReturnsToWelcome() {
        controls.edit().putBoolean("empty_library", true).commit()
        refresh()
        waitText("Твоя библиотека начинается здесь")
        capture("library-global-empty")
        controls.edit().remove("empty_library").commit()
        refresh()
        waitTag("library_book_book_1")
        controls.edit().putInt("next_status", 401).commit()
        refresh()
        waitText("Твой химический ридер")
        rule.onNodeWithTag("library_screen").assertDoesNotExist()
    }
    @Test fun backendPaginatesAndRejectsCursorFromAnotherQuery() {
        controls.edit().putInt("page_size", 4).commit()
        val query = LibraryQuery(sort = LibrarySort.Title)
        val first = (runBlocking { library.getBooks(query, null) } as LibraryResult.Success).value
        assertEquals(4, first.items.size)
        assertNotNull(first.nextCursor)
        val second = (runBlocking { library.getBooks(query, first.nextCursor) } as LibraryResult.Success).value
        assertEquals(4, second.items.size)
        assertTrue(first.items.map { it.id }.intersect(second.items.map { it.id }.toSet()).isEmpty())
        assertEquals(LibraryResult.Failure(LibraryError.Conflict), runBlocking { library.getBooks(query.copy(sort = LibrarySort.Author), first.nextCursor) })
    }
}
