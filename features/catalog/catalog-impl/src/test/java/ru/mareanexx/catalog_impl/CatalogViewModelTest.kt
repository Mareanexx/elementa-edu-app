package ru.mareanexx.catalog_impl

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import ru.mareanexx.book_api.domain.model.BookError
import ru.mareanexx.catalog_api.domain.model.*
import ru.mareanexx.catalog_impl.presentation.CatalogEffect
import ru.mareanexx.catalog_impl.presentation.home.*
import ru.mareanexx.catalog_impl.presentation.catalog.*
import ru.mareanexx.catalog_impl.presentation.search.*
import ru.mareanexx.catalog_impl.presentation.notifications.*

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun cleanup() { Dispatchers.resetMain() }
    @Test fun `быстрый ввод объединяется а одиночная буква не отправляется`() = runTest(dispatcher) {
        val repo = FakeCatalogRepository(); val vm = SearchViewModel(repo)
        vm.accept(SearchIntent.Edit("О")); advanceTimeBy(500); runCurrent(); assertTrue(repo.calls.isEmpty())
        vm.accept(SearchIntent.Edit("Ор")); advanceTimeBy(200)
        vm.accept(SearchIntent.Edit("Органика")); advanceTimeBy(349); runCurrent(); assertTrue(repo.calls.isEmpty())
        advanceTimeBy(1); runCurrent(); assertEquals("Органика", repo.calls.single().first.search)
        assertEquals(0, repo.historyCalls)
    }
    @Test fun `запоздавший поиск не заменяет актуальный результат даже при неотменяемом транспорте`() = runTest(dispatcher) {
        val gate = CompletableDeferred<Unit>()
        val repo = FakeCatalogRepository().apply { getBooks = { query, _ -> if (query.search == "старый") withContext(NonCancellable) { gate.await() }; CatalogResult.Success(CatalogFixtures.page(query.search)) } }
        val vm = SearchViewModel(repo)
        vm.accept(SearchIntent.Edit("старый")); advanceTimeBy(350); runCurrent()
        vm.accept(SearchIntent.Edit("новый")); advanceTimeBy(350); runCurrent()
        gate.complete(Unit); runCurrent()
        assertEquals("новый", vm.state.value.page.items.single().id)
    }
    @Test fun `IME сразу запускает ожидающий поиск и запоминает нормализованный запрос`() = runTest(dispatcher) {
        val repo = FakeCatalogRepository(); val vm = SearchViewModel(repo)
        vm.accept(SearchIntent.Edit("  Космос   химия  ")); vm.accept(SearchIntent.Submit); runCurrent()
        assertEquals("Космос химия", repo.calls.single().first.search)
        assertEquals(listOf("Космос химия"), vm.state.value.history)
        advanceTimeBy(500); runCurrent(); assertEquals(1, repo.calls.size)
    }
    @Test fun `очистка строки отменяет ожидающий поиск и возвращает нулевое состояние`() = runTest(dispatcher) {
        val repo = FakeCatalogRepository(); val vm = SearchViewModel(repo)
        vm.accept(SearchIntent.Edit("химия")); advanceTimeBy(200); vm.accept(SearchIntent.Edit("")); advanceTimeBy(500); runCurrent()
        assertTrue(repo.calls.isEmpty()); assertFalse(vm.state.value.page.loaded)
    }
    @Test fun `поиск не дублирует догрузку и убирает дубли карточек`() = runTest(dispatcher) {
        val repo = FakeCatalogRepository().apply { getBooks = { _, cursor -> CatalogResult.Success(if (cursor == null) CatalogFixtures.page(cursor = "next") else CatalogPage(listOf(CatalogFixtures.book(), CatalogFixtures.book("book_2")), null, 2)) } }
        val vm = SearchViewModel(repo); vm.accept(SearchIntent.ChooseQuery("Химия")); runCurrent()
        vm.accept(SearchIntent.More); vm.accept(SearchIntent.More); runCurrent()
        assertEquals(2, repo.calls.size); assertEquals(2, vm.state.value.page.items.size)
    }
    @Test fun `истёкший курсор повторяется с первой страницы`() = runTest(dispatcher) {
        val repo = FakeCatalogRepository().apply { getBooks = { _, cursor -> if (cursor == null) CatalogResult.Success(CatalogFixtures.page(cursor = "old")) else CatalogResult.Failure(CatalogError.Conflict) } }
        val vm = SearchViewModel(repo); vm.accept(SearchIntent.ChooseQuery("Химия")); runCurrent()
        vm.accept(SearchIntent.More); runCurrent(); assertEquals(CatalogError.Conflict, vm.state.value.page.error)
        vm.accept(SearchIntent.Retry); runCurrent(); assertNull(repo.calls.last().second)
    }
    @Test fun `переключение категорий кэширует страницы и не запрашивает всё повторно`() = runTest(dispatcher) {
        val repo = FakeCatalogRepository(); val vm = CatalogViewModel(repo)
        vm.accept(CatalogIntent.Open("organic")); runCurrent()
        vm.accept(CatalogIntent.Select("physical")); runCurrent()
        vm.accept(CatalogIntent.Select("organic")); runCurrent()
        assertEquals(listOf("organic", "physical"), repo.calls.map { it.first.categoryId }); assertTrue(vm.state.value.page.loaded)
    }
    @Test fun `смена сортировки отменяет старую страницу`() = runTest(dispatcher) {
        val gate = CompletableDeferred<Unit>()
        val repo = FakeCatalogRepository().apply { getBooks = { query, _ -> if (query.sort == CatalogSort.Recommended) withContext(NonCancellable) { gate.await() }; CatalogResult.Success(CatalogFixtures.page(query.sort.name)) } }
        val vm = CatalogViewModel(repo); vm.accept(CatalogIntent.Open()); runCurrent()
        vm.accept(CatalogIntent.Sort(CatalogSort.Title)); runCurrent(); gate.complete(Unit); runCurrent()
        assertEquals("Title", vm.state.value.page.items.single().id)
    }
    @Test fun `синхронизация каталога сохраняет карточки и не показывает скелетон`() = runTest(dispatcher) {
        val gate = CompletableDeferred<Unit>(); val repo = FakeCatalogRepository(); val vm = CatalogViewModel(repo)
        vm.accept(CatalogIntent.Open()); runCurrent()
        repo.getBooks = { _, _ -> gate.await(); CatalogResult.Success(CatalogFixtures.page()) }
        vm.invalidate(); runCurrent()
        assertTrue(vm.state.value.page.refreshing); assertFalse(vm.state.value.page.loading); assertEquals(1, vm.state.value.page.items.size)
        gate.complete(Unit); runCurrent()
    }
    @Test fun `главная сохраняет загруженный контент при ошибке фонового обновления`() = runTest(dispatcher) {
        val repo = FakeCatalogRepository(); val vm = HomeViewModel(repo, FakeShelfRepository())
        vm.accept(HomeIntent.Enter); runCurrent(); repo.getHome = { CatalogResult.Failure(CatalogError.Offline) }
        vm.accept(HomeIntent.Refresh); runCurrent()
        assertNotNull(vm.state.value.feed); assertEquals(CatalogError.Offline, vm.state.value.error); assertFalse(vm.state.value.loading)
    }
    @Test fun `двойное добавление в избранное даёт один запрос а ошибка сохраняет меню`() = runTest(dispatcher) {
        val gate = CompletableDeferred<Unit>(); val books = FakeShelfRepository().apply { this.gate = gate; failure = BookError.Offline }
        val vm = HomeViewModel(FakeCatalogRepository(), books)
        vm.accept(HomeIntent.Actions(CatalogFixtures.book())); vm.accept(HomeIntent.ToggleFavorite); vm.accept(HomeIntent.ToggleFavorite); runCurrent()
        assertEquals(1, books.favoriteCalls); gate.complete(Unit); runCurrent()
        assertNotNull(vm.state.value.actionBook); assertEquals(CatalogError.Offline, vm.state.value.actionError)
    }
    @Test fun `ошибка авторизации требует возврата ко входу`() = runTest(dispatcher) {
        val repo = FakeCatalogRepository().apply { getHome = { CatalogResult.Failure(CatalogError.Unauthorized) } }
        val vm = HomeViewModel(repo, FakeShelfRepository()); vm.accept(HomeIntent.Enter); runCurrent()
        assertEquals(CatalogEffect.SessionExpired, vm.effects.first())
    }
    @Test fun `переход из уведомления происходит сразу а отметка сохраняется в фоне`() = runTest(dispatcher) {
        val gate = CompletableDeferred<Unit>()
        val repo = FakeCatalogRepository().apply { receipt = { gate.await(); CatalogResult.Failure(CatalogError.Offline) } }
        val vm = NotificationsViewModel(repo); vm.accept(NotificationsIntent.Enter); runCurrent()
        vm.accept(NotificationsIntent.Open(vm.state.value.items.single())); runCurrent()
        assertEquals(CatalogEffect.OpenBook("book_1"), vm.effects.first()); assertTrue(vm.state.value.saving)
        gate.complete(Unit); runCurrent(); assertEquals(CatalogError.Offline, vm.state.value.writeError); assertNull(vm.state.value.items.single().readAt)
    }
    @Test fun `прочитать все использует границу снимка и защищено от двойного нажатия`() = runTest(dispatcher) {
        val gate = CompletableDeferred<Unit>(); val repo = FakeCatalogRepository().apply { receipt = { gate.await(); CatalogResult.Failure(CatalogError.Offline) } }
        val vm = NotificationsViewModel(repo); vm.accept(NotificationsIntent.Enter); runCurrent()
        vm.accept(NotificationsIntent.MarkAll); vm.accept(NotificationsIntent.MarkAll); runCurrent(); assertEquals(listOf(3L), repo.markAllCalls)
        gate.complete(Unit); runCurrent()
        repo.getNotes = { _, _ -> CatalogResult.Success(NotificationPage(listOf(CatalogFixtures.note()), null, 8, UnreadState(5, 2))) }
        vm.accept(NotificationsIntent.Refresh); runCurrent(); vm.accept(NotificationsIntent.RetryWrite); runCurrent()
        assertEquals(listOf(3L, 3L), repo.markAllCalls)
    }
    @Test fun `запоздавшая загрузка не перезаписывает отметку прочтения`() = runTest(dispatcher) {
        val gate = CompletableDeferred<Unit>(); val repo = FakeCatalogRepository(); val vm = NotificationsViewModel(repo)
        vm.accept(NotificationsIntent.Enter); runCurrent()
        var read = false
        repo.getNotes = { _, _ -> if (!read) withContext(NonCancellable) { gate.await() }; CatalogResult.Success(NotificationPage(listOf(CatalogFixtures.note().copy(readAt = if (read) java.time.Instant.now() else null)), null, 3, UnreadState(0, 2))) }
        vm.accept(NotificationsIntent.Refresh); runCurrent()
        repo.receipt = { read = true; CatalogResult.Success(NotificationReceipt(java.time.Instant.now(), UnreadState(0, 2))) }
        vm.accept(NotificationsIntent.MarkAll); runCurrent(); gate.complete(Unit); runCurrent()
        assertNotNull(vm.state.value.items.single().readAt)
    }
    @Test fun `категория открытая по ссылке обновляет устаревший кэш после изменения книги`() = runTest(dispatcher) {
        val repo = FakeCatalogRepository(); val vm = CatalogViewModel(repo)
        vm.accept(CatalogIntent.Open("organic")); runCurrent()
        vm.accept(CatalogIntent.Select("physical")); runCurrent()
        vm.invalidate(); runCurrent()
        repo.getBooks = { _, _ -> CatalogResult.Success(CatalogFixtures.page("updated")) }
        vm.accept(CatalogIntent.Open("organic")); runCurrent()
        assertEquals("updated", vm.state.value.page.items.single().id)
    }
    @Test fun `новое нажатие прочитать все использует свежий снимок после предыдущей ошибки`() = runTest(dispatcher) {
        val repo = FakeCatalogRepository().apply { receipt = { CatalogResult.Failure(CatalogError.Offline) } }
        val vm = NotificationsViewModel(repo); vm.accept(NotificationsIntent.Enter); runCurrent()
        vm.accept(NotificationsIntent.MarkAll); runCurrent()
        repo.getNotes = { _, _ -> CatalogResult.Success(NotificationPage(listOf(CatalogFixtures.note()), null, 9, UnreadState(5, 2))) }
        vm.accept(NotificationsIntent.Refresh); runCurrent()
        vm.accept(NotificationsIntent.MarkAll); runCurrent()
        assertEquals(listOf(3L, 9L), repo.markAllCalls)
    }

}
