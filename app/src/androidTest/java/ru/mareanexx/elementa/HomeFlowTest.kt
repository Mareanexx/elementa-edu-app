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
import ru.mareanexx.catalog_api.domain.model.*
import java.io.File

/** Изолированные аккаунты сохраняют книги, коллекции и историю основного пользователя. */
class HomeFlowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context: Context get() = instrumentation.targetContext
    private val container get() = (context.applicationContext as ElementaApplication).container
    private val controls get() = context.getSharedPreferences("catalog_mock_controls", Context.MODE_PRIVATE)
    private lateinit var user: AuthUser
    private lateinit var account: String
    private val catalog get() = container.catalogRepository(user)
    private fun button(text: String) = rule.onNode(hasText(text) and hasClickAction())
    private fun waitTag(tag: String, unmerged: Boolean = false) {
        try { rule.waitUntil(15_000) { rule.onAllNodesWithTag(tag, useUnmergedTree = unmerged).fetchSemanticsNodes().isNotEmpty() } }
        catch (failure: Throwable) { capture("home-timeout"); throw failure }
    }
    private fun waitText(text: String) { rule.waitUntil(15_000) { rule.onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty() } }
    private fun capture(name: String) {
        rule.waitForIdle(); instrumentation.waitForIdleSync(); android.os.SystemClock.sleep(250)
        val dir = File(context.filesDir, "qa").apply { mkdirs() }
        File(dir, "$name.txt").writeText(rule.onAllNodes(isRoot()).onLast().printToString())
        File(dir, "$name.png").outputStream().use { instrumentation.uiAutomation.takeScreenshot().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
    private fun back() { instrumentation.uiAutomation.performGlobalAction(1); rule.waitForIdle() }
    private fun refresh() {
        rule.onNodeWithTag("home_feed").performScrollToIndex(0)
        rule.onNodeWithTag("home_feed").performTouchInput { swipeDown(startY = height * .12f, endY = height * .85f, durationMillis = 650) }
    }
    private fun search(query: String) {
        rule.onNodeWithTag("catalog_search_input").performTextReplacement(query)
        rule.onNodeWithTag("catalog_search_input").performImeAction()
    }
    @Before fun setup() {
        controls.edit().clear().putInt("delay_ms", 90).commit()
        context.getSharedPreferences("book_mock_controls", Context.MODE_PRIVATE).edit().clear().commit()
        runBlocking { container.authRepository.clearSession() }
        waitText("Зарегистрироваться")
        account = "qa_home_${System.nanoTime()}"
        assertEquals(AuthResult.Success, runBlocking { container.authRepository.register("Мария Химик", account, "Elementa123") })
        user = runBlocking { container.authRepository.session.first()!!.user }
        waitTag("home_continue")
    }
    @After fun cleanup() {
        runCatching { capture("home-at-cleanup") }
        controls.edit().clear().commit()
        context.getSharedPreferences("book_mock_controls", Context.MODE_PRIVATE).edit().clear().commit()
        if (::user.isInitialized) {
            runBlocking { container.authRepository.clearSession() }
            context.getSharedPreferences("auth_demo_accounts", Context.MODE_PRIVATE).edit().remove(account).commit()
            for (file in listOf("catalog_search_history", "catalog_demo_notifications", "collections_demo_server", "collections_preferences", "profile_demo_server", "book_demo_server", "book_rating_mock_cache", "library_demo_server")) {
                val preferences = context.getSharedPreferences(file, Context.MODE_PRIVATE)
                val editor = preferences.edit()
                preferences.all.keys.filter { it.contains(user.id) }.forEach(editor::remove)
                editor.commit()
            }
        }
    }
    @Test fun continueReaderAndQuickLinksOpenExistingDestinations() {
        rule.onNodeWithText("12 книг").assertIsDisplayed()
        rule.onNodeWithText("0 коллекций").assertIsDisplayed()
        val hero = rule.onNodeWithTag("home_continue").fetchSemanticsNode().boundsInRoot
        assertEquals(with(rule.density) { androidx.compose.ui.unit.Dp(16f).toPx() }, hero.left, 1f)
        rule.onNodeWithTag("home_read_book_1").performClick()
        waitTag("book_reader_entry")
        rule.onNodeWithText("Начальная страница: 49").assertIsDisplayed()
        button("Вернуться в библиотеку").performClick(); waitTag("home_continue")
        button("Моя библиотека").performClick(); waitTag("library_screen")
        button("Главная").performClick(); waitTag("home_continue")
        button("Мои коллекции").performClick(); waitText("Мои коллекции")
        rule.onNodeWithTag("home_screen").assertDoesNotExist()
        button("Главная").performClick(); waitTag("home_continue")
        capture("home-overview")
    }
    @Test fun categoryDeepLinkSwipeAndSortingSelectCorrectBooks() {
        rule.onNodeWithTag("home_category_organic").onChild().performClick()
        waitTag("catalog_grid_organic"); waitTag("catalog_tile_book_1")
        rule.onNodeWithTag("catalog_tab_1").assertIsSelected()
        rule.onNodeWithTag("bottom_navigation").assertDoesNotExist()
        capture("home-organic")
        rule.onNodeWithTag("catalog_grid_organic").performTouchInput { swipeLeft(durationMillis = 550) }
        waitTag("catalog_grid_inorganic"); waitTag("catalog_tile_book_6")
        rule.onNodeWithTag("catalog_tab_2").assertIsSelected()
        rule.onNodeWithTag("catalog_tab_1").performClick(); waitTag("catalog_grid_organic")
        button("Рекомендуемые").performClick(); button("По названию").performClick()
        waitTag("catalog_tile_book_11")
        rule.onNodeWithTag("catalog_tile_book_11").assertIsDisplayed()
        capture("home-catalog-sorted")
    }
    @Test fun globalSearchUsesBackendHistoryAndOpensBookDetails() {
        rule.onNodeWithTag("home_search").performClick(); waitTag("catalog_search_input")
        waitText("Сейчас ищут"); capture("home-search-zero")
        rule.onNodeWithTag("catalog_search_input").performTextReplacement("К")
        waitText("Введи хотя бы 2 символа"); assertEquals(0, controls.getInt("search_requests", 0))
        search("  Космос  "); waitTag("catalog_row_book_3")
        assertEquals("Космос", controls.getString("last_query", null))
        rule.onNodeWithTag("catalog_row_book_3").performClick(); waitTag("book_content")
        rule.onNodeWithContentDescription("Назад").performClick(); waitTag("catalog_search_input")
        search("Галочкин"); waitTag("catalog_row_book_1")
        search("неттакойкнигинигде"); waitText("По этому запросу книг нет")
        rule.onNodeWithContentDescription("Очистить поиск").performClick(); waitText("Недавние запросы")
        assertTrue(runBlocking { catalog.recentQueries.first() }.contains("Космос"))
        button("Очистить историю").performClick()
        rule.waitUntil(10_000) { catalog.recentQueries.value.isEmpty() }
        capture("home-search-history-cleared")
    }
    @Test fun notificationsMarkReadUpdateBadgeAndPersistAcrossRecreation() {
        rule.onNodeWithTag("home_notifications").performClick(); waitTag("notification_notice_6")
        waitText("Непрочитанных уведомлений: 3"); capture("home-notifications")
        rule.onNodeWithTag("notification_notice_6").performClick(); waitTag("catalog_grid_organic")
        assertEquals(2, (runBlocking { catalog.notifications(NotificationFilter.All) } as CatalogResult.Success).value.unread.count)
        rule.onNodeWithContentDescription("Назад").performClick(); waitTag("notifications_screen")
        button("Прочитать все").performClick(); waitText("Непрочитанных уведомлений: 0")
        rule.onNodeWithTag("catalog_tab_1").performClick(); waitText("Непрочитанных уведомлений нет")
        rule.activityRule.scenario.recreate(); waitTag("notifications_screen"); waitText("Непрочитанных уведомлений: 0")
        rule.onNodeWithContentDescription("Назад").performClick(); waitTag("home_continue")
        rule.onNodeWithTag("home_notifications").assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.StateDescription, "Непрочитанных уведомлений: 0"))
    }
    @Test fun failedNotificationWriteCanRetryWithoutLosingUnreadState() {
        rule.onNodeWithTag("home_notifications").performClick(); waitTag("notification_notice_6")
        controls.edit().putInt("next_status", 503).commit()
        button("Прочитать все").performClick(); waitText("Сервис временно недоступен")
        waitText("Непрочитанных уведомлений: 3")
        button("Повторить").performClick(); waitText("Непрочитанных уведомлений: 0")
    }
    @Test fun homeRefreshRetainsContentAndSessionExpiryReturnsToWelcome() {
        controls.edit().putInt("delay_ms", 1200).commit()
        refresh(); waitTag("content_sync_indicator")
        rule.onNodeWithTag("home_continue").assertIsDisplayed(); rule.onNodeWithTag("catalog_skeleton").assertDoesNotExist()
        rule.waitUntil(10_000) { rule.onAllNodesWithTag("content_sync_indicator").fetchSemanticsNodes().isEmpty() }
        controls.edit().putInt("delay_ms", 90).putInt("next_status", -1).commit()
        refresh(); waitText("Нет подключения")
        rule.onNodeWithTag("home_continue").assertIsDisplayed()
        controls.edit().putInt("next_status", 401).commit()
        button("Повторить").performClick(); waitText("Твой химический ридер")
    }
    @Test fun bookActionsUseSharedShelfAndCollectionPicker() {
        rule.onNodeWithTag("home_feed").performScrollToNode(hasTestTag("home_section_popular"))
        rule.onNodeWithTag("catalog_menu_book_11").performClick()
        button("В избранное").performClick(); waitText("Книга добавлена в избранное")
        assertTrue((runBlocking { container.bookRepository(user).getBook("book_11") } as BookResult.Success).value.shelves.favorite)
        rule.onNodeWithTag("catalog_menu_book_11").performClick(); button("Хочу прочитать").performClick(); waitText("Добавлено в «Хочу прочитать»")
        assertTrue((runBlocking { container.bookRepository(user).getBook("book_11") } as BookResult.Success).value.shelves.wantToRead)
        rule.onNodeWithTag("catalog_menu_book_11").performClick(); button("В коллекцию").performClick(); waitText("Выбрать коллекции")
    }
    @Test fun catalogPaginationScopesCursorsAndHistoryIsIsolatedByAccount() {
        controls.edit().putInt("page_size", 3).commit()
        val first = (runBlocking { catalog.books(CatalogQuery(sort = CatalogSort.Popular)) } as CatalogResult.Success).value
        val second = (runBlocking { catalog.books(CatalogQuery(sort = CatalogSort.Popular), first.nextCursor) } as CatalogResult.Success).value
        assertEquals(3, first.items.size); assertEquals(3, second.items.size)
        assertTrue(first.items.map { it.id }.intersect(second.items.map { it.id }.toSet()).isEmpty())
        assertEquals(CatalogResult.Failure(CatalogError.Conflict), runBlocking { catalog.books(CatalogQuery(sort = CatalogSort.Title), first.nextCursor) })
        runBlocking { catalog.rememberQuery("  Химия   молекул  "); catalog.rememberQuery("ХИМИЯ молекул") }
        assertEquals(listOf("ХИМИЯ молекул"), catalog.recentQueries.value)
        val other = container.catalogRepository(user.copy(id = "isolated_${user.id}"))
        assertTrue(other.recentQueries.value.isEmpty())
        assertEquals(3, (runBlocking { other.notifications(NotificationFilter.Unread) } as CatalogResult.Success).value.unread.count)
    }
    @Test fun emptyHomeAndCategoryRemainUsableAndCanRecover() {
        controls.edit().putBoolean("empty_catalog", true).commit()
        refresh(); waitText("У каждой реакции есть начало")
        rule.onNodeWithTag("home_continue").assertDoesNotExist()
        button("Найти свою книгу").performClick(); waitText("Здесь скоро появятся книги")
        rule.onNodeWithTag("catalog_grid_all").assertIsDisplayed()
        capture("home-catalog-empty")
        rule.onNodeWithContentDescription("Назад").performClick(); waitTag("home_screen")
        controls.edit().remove("empty_catalog").commit()
        refresh(); waitTag("home_continue")
    }

    @Test fun searchBadgesCombineShelvesAndReadingStatusAndRefreshAfterChanges() {
        val books = container.bookRepository(user)
        assertTrue(runBlocking { books.setFavorite("book_1", true) } is BookResult.Success)
        assertTrue(runBlocking { books.setWantToRead("book_1", true) } is BookResult.Success)
        rule.onNodeWithTag("home_search").performClick(); waitTag("catalog_search_input")
        search("Галочкин"); waitTag("catalog_badge_reading_book_1", true)
        rule.onNodeWithTag("catalog_badge_favorite_book_1", true).assertIsDisplayed()
        rule.onNodeWithTag("catalog_badge_planned_book_1", true).assertIsDisplayed()
        waitText("В процессе · 30%")
        capture("home-search-combined-badges")
        rule.onNodeWithTag("catalog_menu_book_1").performClick(); waitTag("catalog_book_actions")
        rule.onAllNodesWithText("Закрыть").assertCountEquals(0)
        val handle = rule.onNodeWithTag("catalog_actions_handle", true).fetchSemanticsNode().boundsInRoot
        assertTrue(handle.height <= with(rule.density) { androidx.compose.ui.unit.Dp(24f).toPx() })
        capture("home-search-compact-actions")
        button("Убрать из избранного").performClick()
        rule.waitUntil(10_000) { rule.onAllNodesWithTag("catalog_badge_favorite_book_1", true).fetchSemanticsNodes().isEmpty() }
        rule.onNodeWithTag("catalog_badge_planned_book_1", true).assertIsDisplayed()
        rule.onNodeWithTag("catalog_badge_reading_book_1", true).assertIsDisplayed()
        assertTrue(runBlocking { container.libraryRepository(user).setReadingStatus("book_1", ru.mareanexx.user_library_api.domain.model.ReadingStatus.Completed) } is ru.mareanexx.user_library_api.domain.model.LibraryResult.Success)
        waitTag("catalog_badge_completed_book_1", true)
        rule.onNodeWithTag("catalog_badge_reading_book_1", true).assertDoesNotExist()
        rule.onNodeWithTag("catalog_badge_planned_book_1", true).assertIsDisplayed()
    }

    @Test fun continueCarouselLimitsBooksAndOpensSelectedReader() {
        val bridge = ru.mareanexx.collections_impl.data.mock.DemoBookLibraryBridge(context, user.id)
        listOf("book_3", "book_5", "book_6", "book_7").forEach { bridge.setPagesRead(it, 30) }
        refresh(); waitText("1 / 5")
        val feed = (runBlocking { catalog.home() } as CatalogResult.Success).value
        assertEquals(5, feed.continueReading.size)
        assertTrue(feed.continueReading.all { it.pagesRead in 1 until it.pages })
        assertFalse(feed.continueReading.any { it.id == "book_7" })
        rule.onNodeWithTag("home_continue_pager").performTouchInput { swipeLeft(durationMillis = 500) }
        val position = rule.onNodeWithTag("home_continue_pager").fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.StateDescription]
        val number = Regex("книга (\\d+)").find(position)!!.groupValues[1].toInt()
        assertTrue(number in 2..5)
        val selected = feed.continueReading[number - 1]
        rule.onNodeWithTag("home_continue_position_${selected.id}").assertTextEquals("$number / 5").assertIsDisplayed()
        rule.onNodeWithTag("home_read_${selected.id}").assertIsDisplayed()
        capture("home-continue-carousel-selected")
        rule.onNodeWithTag("home_read_${selected.id}").performClick(); waitTag("book_reader_entry")
        rule.onNodeWithText("Начальная страница: ${selected.pagesRead + 1}").assertIsDisplayed()
        button("Вернуться в библиотеку").performClick(); waitTag("home_continue_pager")
        rule.onNodeWithTag("home_continue_position_${selected.id}").assertIsDisplayed()
    }

}
