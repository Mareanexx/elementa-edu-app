package ru.mareanexx.collections_impl

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import ru.mareanexx.collections_api.domain.model.*
import ru.mareanexx.collections_impl.presentation.*
import ru.mareanexx.collections_impl.presentation.model.CollectionsPage

@OptIn(ExperimentalCoroutinesApi::class)
class CollectionsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun cleanup() { Dispatchers.resetMain() }
    @Test fun `невалидная форма не отправляет запрос`() = runTest(dispatcher) {
        val repo = FakeCollectionsRepository()
        val model = CollectionsViewModel(repo)
        runCurrent()
        model.accept(CollectionsIntent.Create)
        model.accept(CollectionsIntent.Save)
        assertEquals(CollectionsError.NameRequired, model.state.value.editor!!.fieldError)
        model.accept(CollectionsIntent.ChangeName("я".repeat(61)))
        model.accept(CollectionsIntent.Save)
        assertEquals(CollectionsError.NameTooLong, model.state.value.editor!!.fieldError)
        assertEquals(0, repo.saves)
    }
    @Test fun `повторное сохранение блокируется а имя нормализуется`() = runTest(dispatcher) {
        val repo = FakeCollectionsRepository().apply { gate = CompletableDeferred() }
        val model = CollectionsViewModel(repo)
        runCurrent()
        model.accept(CollectionsIntent.Create)
        model.accept(CollectionsIntent.ChangeName("  Мои   книги "))
        model.accept(CollectionsIntent.Save)
        model.accept(CollectionsIntent.Save)
        model.accept(CollectionsIntent.ChangeName("Другое"))
        runCurrent()
        assertEquals(1, repo.saves)
        assertEquals("Мои книги", repo.lastForm!!.name)
        assertTrue(model.state.value.busy)
        repo.gate!!.complete(Unit)
        runCurrent()
        assertEquals(CollectionsPage.Detail, model.state.value.page)
        assertEquals("Мои книги", model.state.value.selected!!.name)
        model.accept(CollectionsIntent.Back)
        assertEquals(CollectionsPage.Overview, model.state.value.page)
    }
    @Test fun `конфликт сохраняет форму и исчезает при редактировании`() = runTest(dispatcher) {
        val repo = FakeCollectionsRepository()
        val model = CollectionsViewModel(repo)
        runCurrent()
        repo.failure = CollectionsError.NameTaken
        model.accept(CollectionsIntent.Create)
        model.accept(CollectionsIntent.ChangeName("Атомы"))
        model.accept(CollectionsIntent.Save)
        runCurrent()
        assertEquals("Атомы", model.state.value.editor!!.form.name)
        assertEquals(CollectionsError.NameTaken, model.state.value.editor!!.fieldError)
        model.accept(CollectionsIntent.ChangeName("Молекулы"))
        assertNull(model.state.value.editor!!.fieldError)
        repo.failure = null
        model.accept(CollectionsIntent.Save)
        runCurrent()
        assertNull(model.state.value.editor)
    }
    @Test fun `лимит учитывает только пользовательские коллекции`() = runTest(dispatcher) {
        val repo = FakeCollectionsRepository().apply { items = items + (1..30).map { BookCollection("$it", "Книга $it", CollectionCover.Chemistry, 0) } }
        val model = CollectionsViewModel(repo)
        runCurrent()
        model.accept(CollectionsIntent.Create)
        assertNull(model.state.value.editor)
        assertEquals(30, model.state.value.customCount)
        assertEquals(0, repo.saves)
    }
    @Test fun `избранное нельзя редактировать и удалять`() = runTest(dispatcher) {
        val repo = FakeCollectionsRepository()
        val model = CollectionsViewModel(repo)
        runCurrent()
        model.accept(CollectionsIntent.Edit("fav"))
        model.accept(CollectionsIntent.Delete("fav"))
        model.accept(CollectionsIntent.ConfirmDelete)
        runCurrent()
        assertNull(model.state.value.editor)
        assertEquals(0, repo.deletes)
    }
    @Test fun `добавление исключает существующие книги и повторы`() = runTest(dispatcher) {
        val book = CollectionBook("b", "Книга", "Автор", "Описание", 100, CollectionCover.Chemistry)
        val repo = FakeCollectionsRepository().apply { books = listOf(book) }
        val model = CollectionsViewModel(repo)
        runCurrent()
        model.accept(CollectionsIntent.OpenCollection("fav"))
        runCurrent()
        repo.books = listOf(book, book.copy(id = "c"))
        model.accept(CollectionsIntent.OpenPicker)
        runCurrent()
        model.accept(CollectionsIntent.ToggleBook("b"))
        model.accept(CollectionsIntent.ToggleBook("c"))
        model.accept(CollectionsIntent.AddBooks)
        runCurrent()
        assertEquals(setOf("c"), repo.added)
        assertNull(model.state.value.picker)
    }
    @Test fun `сбой настроек не меняет выбранные параметры`() = runTest(dispatcher) {
        val repo = FakeCollectionsRepository()
        val model = CollectionsViewModel(repo)
        runCurrent()
        repo.failure = CollectionsError.Storage
        model.accept(CollectionsIntent.SetPreferences(CollectionsPreferences(CollectionSort.BooksDescending, false)))
        runCurrent()
        assertEquals(CollectionsPreferences(), model.state.value.preferences)
        assertEquals(CollectionsError.Storage, model.state.value.operationError)
    }
    @Test fun `после истечения сессии старые ошибки не выходят из нового входа`() = runTest(dispatcher) {
        val repo = FakeCollectionsRepository().apply {
            failure = CollectionsError.Unauthorized
            preferences.value = CollectionsPreferences(CollectionSort.NameDescending, false)
        }
        val model = CollectionsViewModel(repo)
        runCurrent()
        assertEquals(CollectionsError.Unauthorized, model.state.value.failure)
        model.accept(CollectionsIntent.SessionExpired)
        assertNull(model.state.value.failure)
        assertEquals(CollectionsPreferences(CollectionSort.NameDescending, false), model.state.value.preferences)
        repo.failure = null
        model.accept(CollectionsIntent.Reload)
        runCurrent()
        assertNotNull(model.state.value.items.firstOrNull())
    }
    @Test fun `повторная загрузка сбрасывает сетевую ошибку`() = runTest(dispatcher) {
        val repo = FakeCollectionsRepository().apply { failure = CollectionsError.Offline }
        val model = CollectionsViewModel(repo)
        runCurrent()
        assertEquals(CollectionsError.Offline, model.state.value.failure)
        repo.failure = null
        model.accept(CollectionsIntent.Reload)
        runCurrent()
        assertNull(model.state.value.failure)
        assertFalse(model.state.value.loading)
        assertFalse(model.state.value.refreshing)
    }
    @Test fun `назад из коллекции сохраняет экран всех подборок`() = runTest(dispatcher) {
        val model = CollectionsViewModel(FakeCollectionsRepository())
        runCurrent()
        model.accept(CollectionsIntent.OpenAll)
        model.accept(CollectionsIntent.OpenCollection("fav"))
        runCurrent()
        model.accept(CollectionsIntent.Back)
        assertEquals(CollectionsPage.All, model.state.value.page)
        model.accept(CollectionsIntent.Back)
        assertEquals(CollectionsPage.Overview, model.state.value.page)
    }
    @Test fun `открытие выбора книг не повторяется при повторном событии`() = runTest(dispatcher) {
        val repo = FakeCollectionsRepository()
        val model = CollectionsViewModel(repo)
        runCurrent()
        model.accept(CollectionsIntent.OpenCollection("fav"))
        runCurrent()
        repo.libraryGate = CompletableDeferred()
        model.accept(CollectionsIntent.OpenPicker)
        model.accept(CollectionsIntent.OpenPicker)
        runCurrent()
        assertEquals(1, repo.libraryCalls)
        repo.libraryGate!!.complete(Unit)
        runCurrent()
        model.accept(CollectionsIntent.SearchBooks("Химия"))
        model.accept(CollectionsIntent.OpenPicker)
        assertEquals("Химия", model.state.value.picker!!.query)
        assertEquals(1, repo.libraryCalls)
    }
    @Test fun `свайп убирает книгу и отмена возвращает ее в исходное место`() = runTest(dispatcher) {
        val first = CollectionBook("b", "Книга", "Автор", "Описание", 100, CollectionCover.Chemistry)
        val repo = FakeCollectionsRepository().apply { books = listOf(first, first.copy(id = "c")); items = listOf(items.single().copy(bookCount = 2)) }
        val model = CollectionsViewModel(repo)
        runCurrent()
        model.accept(CollectionsIntent.OpenCollection("fav"))
        runCurrent()
        model.accept(CollectionsIntent.SwipeRemoveBook("b"))
        assertEquals(listOf("c"), model.state.value.books.map { it.id })
        runCurrent()
        assertNotNull(model.state.value.undoRemoval)
        model.accept(CollectionsIntent.UndoRemoveBook)
        runCurrent()
        assertEquals(listOf("b", "c"), model.state.value.books.map { it.id })
        assertEquals(setOf("b"), repo.added)
        assertNull(model.state.value.undoRemoval)
    }
    @Test fun `ошибка удаления откатывает оптимистическое изменение`() = runTest(dispatcher) {
        val book = CollectionBook("b", "Книга", "Автор", "Описание", 100, CollectionCover.Chemistry)
        val repo = FakeCollectionsRepository().apply { books = listOf(book); items = listOf(items.single().copy(bookCount = 1)) }
        val model = CollectionsViewModel(repo)
        runCurrent()
        model.accept(CollectionsIntent.OpenCollection("fav"))
        runCurrent()
        repo.failure = CollectionsError.Offline
        model.accept(CollectionsIntent.SwipeRemoveBook("b"))
        runCurrent()
        assertEquals(listOf(book), model.state.value.books)
        assertEquals(1, model.state.value.items.single().bookCount)
        assertEquals(CollectionsError.Offline, model.state.value.operationError)
    }
    @Test fun `старое обновление списка не возвращает удаленную свайпом книгу`() = runTest(dispatcher) {
        val book = CollectionBook("b", "Книга", "Автор", "Описание", 100, CollectionCover.Chemistry)
        val repo = FakeCollectionsRepository().apply { books = listOf(book) }
        val model = CollectionsViewModel(repo)
        runCurrent()
        model.accept(CollectionsIntent.OpenCollection("fav"))
        runCurrent()
        repo.booksGate = CompletableDeferred()
        model.accept(CollectionsIntent.ReloadBooks)
        runCurrent()
        assertTrue(model.state.value.booksRefreshing)
        model.accept(CollectionsIntent.SwipeRemoveBook("b"))
        runCurrent()
        repo.booksGate!!.complete(Unit)
        runCurrent()
        assertTrue(model.state.value.books.isEmpty())
        assertFalse(model.state.value.booksRefreshing)
    }
    @Test fun `загруженная обложка сохраняется в форме а шаблон удаляет ее`() = runTest(dispatcher) {
        val repo = FakeCollectionsRepository()
        val model = CollectionsViewModel(repo)
        runCurrent()
        model.accept(CollectionsIntent.Create)
        model.accept(CollectionsIntent.ChangeName("Фото"))
        model.accept(CollectionsIntent.BeginCoverImport)
        model.accept(CollectionsIntent.Save)
        assertEquals(0, repo.saves)
        model.accept(CollectionsIntent.UploadCover(CollectionCoverUpload(byteArrayOf(1))))
        runCurrent()
        assertNotNull(model.state.value.editor!!.form.image)
        model.accept(CollectionsIntent.ChangeCover(CollectionCover.Space))
        assertNull(model.state.value.editor!!.form.image)
        assertFalse(model.state.value.editor!!.coverBusy)
    }

}
