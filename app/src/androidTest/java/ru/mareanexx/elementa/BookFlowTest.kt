package ru.mareanexx.elementa

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import ru.mareanexx.auth_api.domain.model.AuthResult
import ru.mareanexx.auth_api.domain.model.AuthUser
import ru.mareanexx.book_api.domain.model.*
import ru.mareanexx.collections_api.domain.model.*
import java.io.File

/** Проверяет UI, MVI и HTTP на отдельном аккаунте, не меняя данные владельца приложения. */
class BookFlowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context: Context get() = instrumentation.targetContext
    private val container get() = (context.applicationContext as ElementaApplication).container
    private val controls get() = context.getSharedPreferences("book_mock_controls", Context.MODE_PRIVATE)
    private lateinit var user: AuthUser
    private lateinit var account: String
    private lateinit var collectionId: String
    private val repository get() = container.bookRepository(user)
    private val collections get() = container.collectionsRepository(user)
    private val targetReview = "book_1-review-1"
    private val ownReplies = mutableSetOf<String>()

    private fun button(text: String) = rule.onNode(hasText(text) and hasClickAction())
    private fun waitTag(tag: String) {
        try { rule.waitUntil(15_000) { rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() } }
        catch (error: Throwable) { capture("book-timeout"); throw error }
    }
    private fun waitText(text: String) = rule.waitUntil(15_000) { rule.onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty() }
    private fun capture(name: String) {
        rule.waitForIdle()
        instrumentation.waitForIdleSync()
        android.os.SystemClock.sleep(350)
        val dir = File(context.filesDir, "qa").apply { mkdirs() }
        File(dir, "$name.txt").writeText(rule.onAllNodes(isRoot()).onLast().printToString())
        File(dir, "$name.png").outputStream().use { instrumentation.uiAutomation.takeScreenshot().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
    private fun book() = (runBlocking { repository.getBook("book_1") } as BookResult.Success).value
    private fun back() { rule.onNodeWithContentDescription("Назад").performClick(); rule.waitForIdle() }
    private fun scrollBook(matcher: SemanticsMatcher) { rule.onNodeWithTag("book_content").performScrollToNode(matcher) }
    private fun openBook(id: String) {
        rule.onNodeWithTag("collection_books_list").performScrollToNode(hasTestTag("collection_book_open_$id"))
        rule.onNodeWithTag("collection_book_open_$id").performClick()
        waitTag("book_content")
    }
    private fun openReviews() {
        scrollBook(hasText("Смотреть все"))
        button("Смотреть все").performClick()
        waitTag("book_reviews_list")
        waitTag("review_$targetReview")
    }
    @Before fun setup() {
        controls.edit().clear().putInt("delay_ms", 100).putBoolean("long_description", true).commit()
        context.getSharedPreferences("collections_mock_controls", Context.MODE_PRIVATE).edit().clear().commit()
        runBlocking { container.authRepository.clearSession() }
        waitText("Зарегистрироваться")
        account = "qa_books_${System.nanoTime()}"
        assertEquals(AuthResult.Success, runBlocking { container.authRepository.register("Мария Химик", account, "Elementa123") })
        user = runBlocking { container.authRepository.session.first()!!.user }
        assertEquals(CollectionsResult.Success(Unit), runBlocking { collections.addBooks("favorites", setOf("book_1", "book_2", "book_12")) })
        collectionId = (runBlocking { collections.create(CollectionForm("Для практикума")) } as CollectionsResult.Success).value.id
        waitText("Главная")
        button("Коллекции").performClick()
        waitText("Для практикума")
        button("Открыть").performClick()
        waitTag("collection_book_open_book_1")
        openBook("book_1")
    }
    @After fun cleanup() {
        runCatching { capture("book-at-cleanup") }
        controls.edit().clear().commit()
        if (::user.isInitialized) {
            runBlocking {
                repository.react("book_1", targetReview, null)
                ownReplies.forEach { repository.deleteReply("book_1", targetReview, it) }
                repository.deleteReview("book_1")
                container.authRepository.clearSession()
            }
            context.getSharedPreferences("auth_demo_accounts", Context.MODE_PRIVATE).edit().remove(account).commit()
            context.getSharedPreferences("collections_demo_server", Context.MODE_PRIVATE).edit().remove("collections_${user.id}").remove("preferences_${user.id}").commit()
            context.getSharedPreferences("collections_preferences", Context.MODE_PRIVATE).edit().remove("preferences_${user.id}").commit()
        }
    }
    @Test fun galleryDescriptionAndReaderKeepActualProgress() {
        capture("book-overview")
        rule.onNodeWithTag("book_cover_open").performClick()
        waitTag("book_gallery")
        rule.onNode(hasText("1 / 3") and hasAnyAncestor(hasTestTag("book_gallery"))).assertIsDisplayed()
        rule.onNodeWithTag("book_gallery_pager").performTouchInput { swipeLeft() }
        waitText("2 / 3")
        rule.onNodeWithContentDescription("Открыть изображение 3").performClick()
        waitText("3 / 3")
        rule.onNodeWithTag("book_gallery_image_diagram").performTouchInput { doubleClick() }
        rule.onNodeWithTag("book_gallery_image_diagram").assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Масштаб: 250%"))
        capture("book-gallery")
        rule.onNodeWithContentDescription("Закрыть").performClick()
        scrollBook(hasTestTag("book_description"))
        val layouts = mutableListOf<TextLayoutResult>()
        rule.onNodeWithTag("book_description").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult) { it(layouts) }
        assertEquals(6, layouts.single().lineCount)
        val underlyingScroll = rule.onNodeWithTag("book_content").fetchSemanticsNode().config[SemanticsProperties.VerticalScrollAxisRange].value()
        button("Полностью").performClick()
        waitTag("book_description_sheet")
        waitTag("book_full_description")
        val sheetTop = rule.onNodeWithTag("book_description_sheet").fetchSemanticsNode().boundsInRoot.top
        val scroll = rule.onNodeWithTag("book_description_scroll")
        scroll.performTouchInput { swipeDown(durationMillis = 600) }
        rule.onNodeWithTag("book_description_sheet").assertIsDisplayed()
        assertEquals(sheetTop, rule.onNodeWithTag("book_description_sheet").fetchSemanticsNode().boundsInRoot.top, 1f)
        scroll.performTouchInput { swipeUp(durationMillis = 600) }
        assertTrue(scroll.fetchSemanticsNode().config[SemanticsProperties.VerticalScrollAxisRange].value() > 0f)
        assertEquals(sheetTop, rule.onNodeWithTag("book_description_sheet").fetchSemanticsNode().boundsInRoot.top, 1f)
        capture("book-description")
        button("Понятно").performClick()
        rule.waitUntil(10_000) { rule.onAllNodesWithTag("book_description_sheet").fetchSemanticsNodes().isEmpty() }
        assertEquals(underlyingScroll, rule.onNodeWithTag("book_content").fetchSemanticsNode().config[SemanticsProperties.VerticalScrollAxisRange].value(), 1f)
        scrollBook(hasTestTag("book_read"))
        rule.onNodeWithTag("book_read").performClick()
        waitTag("book_reader_entry")
        rule.onNodeWithText("Начальная страница: 49").assertIsDisplayed()
        assertEquals(48, book().pagesRead)
        button("Вернуться к книге").performClick()
        waitTag("book_content")
    }
    @Test fun shelfChangesAreSharedAndFailuresKeepPreviousState() {
        controls.edit().putInt("next_status", 503).commit()
        rule.onNodeWithTag("book_favorite").performClick()
        waitText("Сервис временно недоступен")
        assertTrue(book().shelves.favorite)
        rule.onNodeWithTag("book_favorite").performClick()
        waitText("Книга убрана из избранного")
        assertFalse(book().shelves.favorite)
        rule.onNodeWithTag("book_favorite").performClick()
        waitText("Книга в избранном")
        rule.onNodeWithTag("book_plan").performClick()
        waitText("В списке чтения")
        assertTrue((runBlocking { collections.getBooks("reading-list") } as CollectionsResult.Success).value.any { it.id == "book_1" })
        rule.onNodeWithTag("book_collections").performClick()
        waitText("Выбрать коллекции")
        button("Для практикума").performClick()
        button("Сохранить").performClick()
        waitText("В коллекциях: 1")
        assertTrue((runBlocking { collections.getBooks(collectionId) } as CollectionsResult.Success).value.any { it.id == "book_1" })
        back()
        waitTag("collection_books_list")
        back()
        waitText("Хочу прочитать")
        rule.onNodeWithText("Хочу прочитать").performClick()
        waitTag("collection_book_book_1")
        capture("book-reading-list")
    }
    @Test fun reviewsSupportExclusiveReactionsAndIdempotentReplyRetry() {
        openReviews()
        rule.onNodeWithTag("book_reviews_list").performScrollToNode(hasTestTag("review_like_$targetReview"))
        rule.onNodeWithTag("review_like_$targetReview").performClick()
        rule.waitUntil(10_000) { (runBlocking { repository.getReviews("book_1", ReviewsQuery(), null) } as BookResult.Success).value.items.first { it.id == targetReview }.reaction == ReviewReaction.Like }
        rule.onNodeWithTag("review_like_$targetReview").assertIsSelected()
        rule.onNodeWithTag("review_dislike_$targetReview").performClick()
        rule.waitUntil(10_000) { (runBlocking { repository.getReviews("book_1", ReviewsQuery(), null) } as BookResult.Success).value.items.first { it.id == targetReview }.reaction == ReviewReaction.Dislike }
        rule.onNodeWithTag("review_like_$targetReview").assertIsNotSelected()
        rule.onNodeWithTag("review_dislike_$targetReview").assertIsSelected()
        capture("book-reviews")
        button("Ответов: 2").performClick()
        waitTag("reply_$targetReview-reply-2")
        val before = (runBlocking { repository.getReplies("book_1", targetReview, null) } as BookResult.Success).value.totalCount
        rule.onNodeWithTag("book_reply_input").performTextReplacement("Спасибо! Попробую разобрать примеры.")
        controls.edit().putBoolean("drop_reply_after_save", true).commit()
        rule.onNodeWithContentDescription("Отправить ответ").performClick()
        waitText("Нет подключения")
        rule.onNodeWithTag("book_reply_input").assertTextContains("Спасибо! Попробую разобрать примеры.")
        val stored = (runBlocking { repository.getReplies("book_1", targetReview, null) } as BookResult.Success).value.items.single { it.isMine }
        ownReplies += stored.id
        rule.onNodeWithContentDescription("Отправить ответ").performClick()
        waitTag("reply_${stored.id}")
        assertEquals(before + 1, (runBlocking { repository.getReplies("book_1", targetReview, null) } as BookResult.Success).value.totalCount)
        capture("book-discussion")
        rule.onNodeWithContentDescription("Действия с ответом").performClick()
        button("Удалить").performClick()
        waitText("Удалить ответ?")
        button("Удалить").performClick()
        waitText("Ответ удалён")
        assertEquals(before, (runBlocking { repository.getReplies("book_1", targetReview, null) } as BookResult.Success).value.totalCount)
        back()
        waitTag("book_reviews_list")
    }
    @Test fun ownReviewValidatesRatingAndCanBeEditedAndDeleted() {
        val count = book().rating.count
        scrollBook(hasText("Оставить отзыв"))
        button("Оставить отзыв").performClick()
        button("Опубликовать").performClick()
        rule.onNodeWithText("Выбери оценку от 1 до 5").assertIsDisplayed()
        rule.onNodeWithTag("book_rate_5").performClick()
        rule.onNodeWithTag("book_review_input").performTextReplacement("Понятные схемы и примеры, удобно повторять теорию.")
        button("Опубликовать").performClick()
        waitText("Отзыв сохранён")
        assertEquals(count + 1, book().rating.count)
        assertEquals(5, book().myReview!!.rating)
        scrollBook(hasText("Изменить отзыв"))
        button("Изменить отзыв").performClick()
        rule.onNodeWithTag("book_rate_4").performClick()
        button("Сохранить изменения").performClick()
        waitText("Отзыв сохранён")
        assertEquals(4, book().myReview!!.rating)
        val own = book().myReview!!
        scrollBook(hasTestTag("review_${own.id}"))
        button("Ответить").performClick()
        waitTag("book_replies_list")
        rule.onNodeWithContentDescription("Действия с отзывом").performClick()
        button("Удалить").performClick()
        waitText("Удалить отзыв?")
        button("Удалить").performClick()
        waitText("Отзыв удалён")
        waitTag("book_content")
        rule.onNodeWithTag("book_replies_list").assertDoesNotExist()
        assertEquals(count, book().rating.count)
        assertNull(book().myReview)
    }
    @Test fun singleMissingCoversAndRatingFilterHaveHonestStates() {
        back()
        openBook("book_2")
        rule.onNodeWithTag("book_cover_open").performClick()
        waitTag("book_gallery")
        rule.onNode(hasText("1 / 1") and hasAnyAncestor(hasTestTag("book_gallery"))).assertIsDisplayed()
        rule.onNodeWithText("Единственное изображение").assertIsDisplayed()
        rule.onNodeWithContentDescription("Закрыть").performClick()
        back()
        openBook("book_12")
        rule.onNodeWithText("Нет обложки").assertIsDisplayed()
        rule.onNodeWithTag("book_cover_open").assertIsNotEnabled()
        rule.onAllNodesWithText("Пока без оценок").onFirst().assertIsDisplayed()
        capture("book-empty-cover")
        back()
        openBook("book_1")
        openReviews()
        button("Полезные").performClick()
        button("Низкие оценки").performClick()
        waitTag("review_book_1-review-6")
        rule.onNodeWithContentDescription("Отзывы с оценкой 1 из 5").performScrollTo().performClick()
        rule.onNodeWithContentDescription("Отзывы с оценкой 1 из 5").assertIsSelected()
        rule.waitForIdle()
        capture("book-filtered-reviews")
        rule.onNodeWithTag("book_reviews_list").performScrollToNode(hasTestTag("review_book_1-review-6"))
        rule.onNodeWithTag("review_book_1-review-6").assertIsDisplayed()
        assertEquals(1, (runBlocking { repository.getReviews("book_1", ReviewsQuery(ReviewSort.Lowest, 1), null) } as BookResult.Success).value.items.first().rating)
    }
    @Test fun expiredSessionClosesBookAndReturnsToWelcome() {
        controls.edit().putInt("next_status", 401).commit()
        rule.onNodeWithTag("book_favorite").performClick()
        waitText("Твой химический ридер")
        rule.onNodeWithTag("book_content").assertDoesNotExist()
        assertNull(runBlocking { container.authRepository.session.first() })
    }
    @Test fun initialOfflineRequestOffersRetryWithoutLosingNavigation() {
        back()
        rule.onNodeWithTag("collection_books_list").performScrollToNode(hasTestTag("collection_book_open_book_2"))
        controls.edit().putInt("next_status", -1).commit()
        rule.onNodeWithTag("collection_book_open_book_2").performClick()
        waitText("Нет подключения")
        rule.onNodeWithTag("book_content").assertDoesNotExist()
        button("Повторить").performClick()
        waitTag("book_content")
        back()
        waitTag("collection_books_list")
    }
}
