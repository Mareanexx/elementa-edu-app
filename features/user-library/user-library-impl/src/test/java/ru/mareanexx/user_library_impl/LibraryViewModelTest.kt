package ru.mareanexx.user_library_impl

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import ru.mareanexx.user_library_api.domain.model.*
import ru.mareanexx.book_api.domain.model.BookError
import ru.mareanexx.user_library_impl.presentation.*

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun cleanup() { Dispatchers.resetMain() }
    @Test fun `одна буква не отправляет поиск а быстрый ввод объединяется`() = runTest(dispatcher) {
        val repo = FakeLibraryRepository()
        val model = LibraryViewModel(repo, FakeShelfRepository()); runCurrent()
        model.accept(LibraryIntent.ChangeSearch("О")); advanceTimeBy(500); runCurrent()
        assertEquals(1, repo.calls.size)
        model.accept(LibraryIntent.ChangeSearch("Ор")); advanceTimeBy(200)
        model.accept(LibraryIntent.ChangeSearch("Органика")); advanceTimeBy(349); runCurrent()
        assertEquals(1, repo.calls.size)
        advanceTimeBy(1); runCurrent()
        assertEquals("Органика", repo.calls.last().first.search)
        assertEquals(2, repo.calls.size)
    }
    @Test fun `устаревший поиск не заменяет новый результат`() = runTest(dispatcher) {
        val repo = FakeLibraryRepository().apply { gates["старый"] = CompletableDeferred() }
        val model = LibraryViewModel(repo, FakeShelfRepository()); runCurrent()
        model.accept(LibraryIntent.ChangeSearch("старый")); advanceTimeBy(350); runCurrent()
        repo.pages[null] = LibraryFixtures.page(listOf(LibraryFixtures.book("new")))
        model.accept(LibraryIntent.ChangeSearch("новый")); advanceTimeBy(350); runCurrent()
        repo.gates.getValue("старый").complete(Unit); runCurrent()
        assertEquals("new", model.state.value.page(LibraryCategory.All).items.single().id)
    }
    @Test fun `вкладки кэшируются и возвращение не запускает скелетон`() = runTest(dispatcher) {
        val repo = FakeLibraryRepository()
        val model = LibraryViewModel(repo, FakeShelfRepository()); runCurrent()
        model.accept(LibraryIntent.SelectCategory(LibraryCategory.InProgress)); runCurrent()
        model.accept(LibraryIntent.SelectCategory(LibraryCategory.All)); runCurrent()
        assertEquals(2, repo.calls.size)
        assertFalse(model.state.value.page(LibraryCategory.All).loading)
    }
    @Test fun `обновление сохраняет карточки и использует фоновый индикатор`() = runTest(dispatcher) {
        val repo = FakeLibraryRepository()
        val model = LibraryViewModel(repo, FakeShelfRepository()); runCurrent()
        repo.gates[""] = CompletableDeferred()
        model.accept(LibraryIntent.Refresh); runCurrent()
        val page = model.state.value.page(LibraryCategory.All)
        assertTrue(page.refreshing)
        assertFalse(page.loading)
        assertEquals(1, page.items.size)
        repo.gates.getValue("").complete(Unit); runCurrent()
    }
    @Test fun `пагинация защищена от повторного запроса и дублирования книг`() = runTest(dispatcher) {
        val repo = FakeLibraryRepository().apply {
            pages[null] = LibraryFixtures.page(cursor = "next")
            pages["next"] = LibraryFixtures.page(listOf(LibraryFixtures.book(), LibraryFixtures.book("book_2")))
        }
        val model = LibraryViewModel(repo, FakeShelfRepository()); runCurrent()
        model.accept(LibraryIntent.LoadMore(LibraryCategory.All)); model.accept(LibraryIntent.LoadMore(LibraryCategory.All)); runCurrent()
        assertEquals(2, repo.calls.size)
        assertEquals(2, model.state.value.page(LibraryCategory.All).items.size)
    }
    @Test fun `двойное нажатие избранного отправляет один запрос`() = runTest(dispatcher) {
        val shelf = FakeShelfRepository().apply { gate = CompletableDeferred() }
        val model = LibraryViewModel(FakeLibraryRepository(), shelf); runCurrent()
        model.accept(LibraryIntent.ShowBookMenu(LibraryFixtures.book()))
        model.accept(LibraryIntent.ToggleFavorite); model.accept(LibraryIntent.ToggleFavorite); runCurrent()
        assertEquals(1, shelf.favoriteCalls)
        shelf.gate!!.complete(Unit); runCurrent()
        assertFalse(model.state.value.mutating)
    }
    @Test fun `ошибка избранного сохраняет прежнее состояние`() = runTest(dispatcher) {
        val shelf = FakeShelfRepository().apply { failure = BookError.Offline }
        val model = LibraryViewModel(FakeLibraryRepository(), shelf); runCurrent()
        model.accept(LibraryIntent.ShowBookMenu(LibraryFixtures.book())); model.accept(LibraryIntent.ToggleFavorite); runCurrent()
        assertFalse(model.state.value.page(LibraryCategory.All).items.single().shelves.favorite)
        assertEquals(LibraryError.Offline, model.state.value.operationError)
    }
    @Test fun `ошибка статуса сохраняет подтверждение и прежний прогресс`() = runTest(dispatcher) {
        val repo = FakeLibraryRepository().apply { statusFailure = LibraryError.Unavailable }
        val model = LibraryViewModel(repo, FakeShelfRepository()); runCurrent()
        model.accept(LibraryIntent.ShowBookMenu(LibraryFixtures.book()))
        model.accept(LibraryIntent.ChangeReadingStatus(ReadingStatus.Completed)); model.accept(LibraryIntent.ConfirmStatusChange); runCurrent()
        assertNotNull(model.state.value.pendingStatus)
        assertEquals(48, model.state.value.page(LibraryCategory.All).items.single().pagesRead)
        repo.statusFailure = null
        model.accept(LibraryIntent.ConfirmStatusChange); runCurrent()
        assertNull(model.state.value.pendingStatus)
        assertEquals(160, model.state.value.page(LibraryCategory.All).items.single().pagesRead)
    }
    @Test fun `порядок меняется только после успешного ответа настроек`() = runTest(dispatcher) {
        val repo = FakeLibraryRepository().apply { sortFailure = LibraryError.Offline }
        val model = LibraryViewModel(repo, FakeShelfRepository()); runCurrent()
        model.accept(LibraryIntent.OpenSort); model.accept(LibraryIntent.Sort(LibrarySort.Title)); runCurrent()
        assertEquals(LibrarySort.Recent, model.state.value.preferences.sort)
        assertTrue(model.state.value.sortOpen)
        repo.sortFailure = null
        model.accept(LibraryIntent.Sort(LibrarySort.Title)); runCurrent()
        assertEquals(LibrarySort.Title, model.state.value.preferences.sort)
        assertEquals(LibrarySort.Title, repo.calls.last().first.sort)
    }
    @Test fun `истечение сессии отменяет незавершённую загрузку`() = runTest(dispatcher) {
        val repo = FakeLibraryRepository().apply { gates[""] = CompletableDeferred() }
        val model = LibraryViewModel(repo, FakeShelfRepository()); runCurrent()
        model.accept(LibraryIntent.SessionExpired)
        repo.gates.getValue("").complete(Unit); runCurrent()
        assertTrue(model.state.value.pages.isEmpty())
    }
    @Test fun `пустая загруженная вкладка обновляется без повторного скелетона`() = runTest(dispatcher) {
        val repo = FakeLibraryRepository().apply { pages[null] = LibraryPage(emptyList(), null, 0, LibraryCounts()) }
        val model = LibraryViewModel(repo, FakeShelfRepository()); runCurrent()
        repo.gates[""] = CompletableDeferred()
        model.accept(LibraryIntent.Refresh); runCurrent()
        assertFalse(model.state.value.page(LibraryCategory.All).loading)
        assertTrue(model.state.value.page(LibraryCategory.All).refreshing)
        repo.gates.getValue("").complete(Unit); runCurrent()
    }
    @Test fun `повтор клавиши поиска не дублирует выполняющийся запрос`() = runTest(dispatcher) {
        val repo = FakeLibraryRepository().apply { gates["химия"] = CompletableDeferred() }
        val model = LibraryViewModel(repo, FakeShelfRepository()); runCurrent()
        model.accept(LibraryIntent.ChangeSearch("химия"))
        model.accept(LibraryIntent.SubmitSearch); runCurrent()
        model.accept(LibraryIntent.SubmitSearch); runCurrent()
        assertEquals(2, repo.calls.size)
        repo.gates.getValue("химия").complete(Unit); runCurrent()
    }
    @Test fun `просроченный курсор повторяет первую страницу вместо зацикливания`() = runTest(dispatcher) {
        val repo = FakeLibraryRepository().apply { pages[null] = LibraryFixtures.page(cursor = "next") }
        val model = LibraryViewModel(repo, FakeShelfRepository()); runCurrent()
        repo.failure = LibraryError.Conflict
        model.accept(LibraryIntent.LoadMore(LibraryCategory.All)); runCurrent()
        repo.failure = null
        model.accept(LibraryIntent.Retry(LibraryCategory.All)); runCurrent()
        assertNull(repo.calls.last().second)
    }
}
