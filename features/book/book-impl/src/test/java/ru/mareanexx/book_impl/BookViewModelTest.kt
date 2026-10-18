package ru.mareanexx.book_impl

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import ru.mareanexx.book_api.domain.model.*
import ru.mareanexx.book_impl.presentation.*
import ru.mareanexx.book_impl.presentation.model.BookPage

@OptIn(ExperimentalCoroutinesApi::class)
class BookViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun cleanup() { Dispatchers.resetMain() }
    @Test fun `двойное нажатие сердца отправляет один запрос`() = runTest(dispatcher) {
        val repo = FakeBookRepository().apply { favoriteGate = CompletableDeferred() }
        val model = BookViewModel("book_1", repo)
        runCurrent()
        model.accept(BookIntent.ToggleFavorite); model.accept(BookIntent.ToggleFavorite)
        runCurrent()
        assertEquals(1, repo.favoriteCalls)
        repo.favoriteGate!!.complete(Unit); runCurrent()
        assertTrue(model.state.value.book!!.shelves.favorite)
    }
    @Test fun `ошибка избранного сохраняет прежнее состояние и допускает повтор`() = runTest(dispatcher) {
        val repo = FakeBookRepository()
        val model = BookViewModel("book_1", repo); runCurrent()
        repo.failure = BookError.Offline
        model.accept(BookIntent.ToggleFavorite); runCurrent()
        assertFalse(model.state.value.book!!.shelves.favorite)
        assertEquals(BookError.Offline, model.state.value.operationError)
        repo.failure = null
        model.accept(BookIntent.ToggleFavorite); runCurrent()
        assertTrue(model.state.value.book!!.shelves.favorite)
    }
    @Test fun `запоздавшее обновление не отменяет изменение избранного`() = runTest(dispatcher) {
        val repo = FakeBookRepository()
        val model = BookViewModel("book_1", repo); runCurrent()
        repo.bookGate = CompletableDeferred()
        model.accept(BookIntent.Reload); runCurrent()
        assertTrue(model.state.value.refreshing)
        assertFalse(model.state.value.loading)
        model.accept(BookIntent.ToggleFavorite); runCurrent()
        repo.bookGate!!.complete(Unit); runCurrent()
        assertTrue(model.state.value.book!!.shelves.favorite)
        assertFalse(model.state.value.refreshing)
    }
    @Test fun `старый запрос отзывов не перетирает новую сортировку`() = runTest(dispatcher) {
        val gate = CompletableDeferred<Unit>()
        val repo = FakeBookRepository().apply { reviewGates[ReviewSort.Helpful] = gate; pages[ReviewSort.Newest] = listOf(BookFixtures.review("new")) }
        val model = BookViewModel("book_1", repo); runCurrent()
        model.accept(BookIntent.OpenReviews); runCurrent()
        model.accept(BookIntent.ChangeReviewQuery(ReviewsQuery(ReviewSort.Newest))); runCurrent()
        gate.complete(Unit); runCurrent()
        assertEquals(ReviewSort.Newest, model.state.value.reviews.query.sort)
        assertEquals("new", model.state.value.reviews.items.single().id)
    }
    @Test fun `повтор ответа после потери результата использует прежний ключ и не дублирует ответ`() = runTest(dispatcher) {
        val repo = FakeBookRepository().apply { lostReplyResponse = true }
        val model = BookViewModel("book_1", repo); runCurrent()
        model.accept(BookIntent.OpenThread("r1")); runCurrent()
        model.accept(BookIntent.ChangeReply("Спасибо за объяснение"))
        model.accept(BookIntent.SubmitReply); runCurrent()
        assertEquals("Спасибо за объяснение", model.state.value.thread!!.draft)
        assertNotNull(model.state.value.thread!!.requestKey)
        model.accept(BookIntent.SubmitReply); runCurrent()
        assertEquals(2, repo.replyKeys.size)
        assertEquals(repo.replyKeys.first(), repo.replyKeys.last())
        assertEquals(1, repo.storedReplies.size)
        assertEquals(1, model.state.value.thread!!.items.size)
        assertEquals("", model.state.value.thread!!.draft)
    }
    @Test fun `выбор коллекций не теряется при ошибке сохранения`() = runTest(dispatcher) {
        val repo = FakeBookRepository()
        val model = BookViewModel("book_1", repo); runCurrent()
        model.accept(BookIntent.OpenCollections); runCurrent()
        model.accept(BookIntent.ToggleCollection("c"))
        repo.failure = BookError.Unavailable
        model.accept(BookIntent.SaveCollections); runCurrent()
        assertEquals(setOf("c"), model.state.value.collections!!.selected)
        repo.failure = null
        model.accept(BookIntent.SaveCollections); runCurrent()
        assertNull(model.state.value.collections)
        assertEquals(1, model.state.value.book!!.shelves.collectionCount)
    }
    @Test fun `оценка обязательна и повторное сохранение отзыва блокируется`() = runTest(dispatcher) {
        val repo = FakeBookRepository().apply { reviewGate = CompletableDeferred() }
        val model = BookViewModel("book_1", repo); runCurrent()
        model.accept(BookIntent.OpenReviewEditor); model.accept(BookIntent.SaveReview); runCurrent()
        assertEquals(BookError.RatingRequired, model.state.value.reviewDraft!!.fieldError)
        assertEquals(0, repo.saveCalls)
        model.accept(BookIntent.ChangeReviewRating(5)); model.accept(BookIntent.SaveReview); model.accept(BookIntent.SaveReview); runCurrent()
        assertEquals(1, repo.saveCalls)
        repo.reviewGate!!.complete(Unit); runCurrent()
        assertEquals(5, model.state.value.book!!.myReview!!.rating)
        assertNull(model.state.value.reviewDraft)
    }
    @Test fun `чужие и собственные реакции обрабатываются раздельно`() = runTest(dispatcher) {
        val repo = FakeBookRepository()
        val model = BookViewModel("book_1", repo); runCurrent()
        model.accept(BookIntent.OpenReviews); runCurrent()
        model.accept(BookIntent.React("r1", ReviewReaction.Like)); runCurrent()
        assertEquals(ReviewReaction.Like, model.state.value.reviews.items.single().reaction)
        model.accept(BookIntent.React("r1", ReviewReaction.Like)); runCurrent()
        assertNull(model.state.value.reviews.items.single().reaction)
        repo.pages[ReviewSort.Helpful] = listOf(BookFixtures.review("mine", mine = true))
        model.accept(BookIntent.ReloadReviews); runCurrent()
        model.accept(BookIntent.React("mine", ReviewReaction.Dislike)); runCurrent()
        assertEquals(2, repo.reactionCalls)
    }
    @Test fun `истечение сессии отменяет незавершённые изменения`() = runTest(dispatcher) {
        val repo = FakeBookRepository().apply { favoriteGate = CompletableDeferred() }
        val model = BookViewModel("book_1", repo); runCurrent()
        model.accept(BookIntent.ToggleFavorite); runCurrent()
        model.accept(BookIntent.SessionExpired)
        repo.favoriteGate!!.complete(Unit); runCurrent()
        assertNull(model.state.value.book)
        assertFalse(model.state.value.shelvesBusy)
    }
    @Test fun `заготовка чтения сохраняет фактический прогресс`() = runTest(dispatcher) {
        val model = BookViewModel("book_1", FakeBookRepository()); runCurrent()
        model.accept(BookIntent.Read)
        assertEquals(BookPage.Reader, model.state.value.page)
        assertEquals(48, model.state.value.book!!.pagesRead)
        model.accept(BookIntent.Back)
        assertEquals(BookPage.Detail, model.state.value.page)
    }
    @Test fun `повторное открытие обсуждения возвращает на текущий экран`() = runTest(dispatcher) {
        val model = BookViewModel("book_1", FakeBookRepository()); runCurrent()
        model.accept(BookIntent.OpenThread("r1")); runCurrent()
        model.accept(BookIntent.Back)
        assertEquals(BookPage.Detail, model.state.value.page)
        model.accept(BookIntent.OpenReviews); runCurrent()
        model.accept(BookIntent.OpenThread("r1")); runCurrent()
        model.accept(BookIntent.Back)
        assertEquals(BookPage.Reviews, model.state.value.page)
    }
    @Test fun `редактирование собственного отзыва обновляет открытое обсуждение`() = runTest(dispatcher) {
        val repo = FakeBookRepository().apply { book = book.copy(myReview = BookFixtures.review(mine = true)) }
        val model = BookViewModel("book_1", repo); runCurrent()
        model.accept(BookIntent.OpenThread("r1")); runCurrent()
        model.accept(BookIntent.OpenReviewEditor)
        model.accept(BookIntent.ChangeReviewText("Новые впечатления"))
        model.accept(BookIntent.SaveReview); runCurrent()
        assertEquals("Новые впечатления", model.state.value.thread!!.review.text)
        assertEquals(BookPage.Thread, model.state.value.page)
    }
    @Test fun `удаление собственного отзыва закрывает его обсуждение`() = runTest(dispatcher) {
        val repo = FakeBookRepository().apply { book = book.copy(myReview = BookFixtures.review(mine = true)) }
        val model = BookViewModel("book_1", repo); runCurrent()
        model.accept(BookIntent.OpenThread("r1")); runCurrent()
        model.accept(BookIntent.AskDeleteReview)
        model.accept(BookIntent.ConfirmDeleteReview); runCurrent()
        assertNull(model.state.value.thread)
        assertNull(model.state.value.book!!.myReview)
        assertEquals(BookPage.Detail, model.state.value.page)
    }
    @Test fun `прямой вход в чтение не проходит через экран описания`() = runTest(dispatcher) {
        val model = BookViewModel("book_1", FakeBookRepository(), BookPage.Reader)
        assertEquals(BookPage.Reader, model.state.value.page)
        runCurrent()
        assertEquals(BookPage.Reader, model.state.value.page)
        assertEquals(48, model.state.value.book!!.pagesRead)
    }
}
