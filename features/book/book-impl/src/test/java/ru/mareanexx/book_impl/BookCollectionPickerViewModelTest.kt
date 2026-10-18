package ru.mareanexx.book_impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import ru.mareanexx.book_api.domain.model.BookError
import ru.mareanexx.book_impl.presentation.*
import ru.mareanexx.book_impl.presentation.model.BookCollectionPickerEffect

@OptIn(ExperimentalCoroutinesApi::class)
class BookCollectionPickerViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun cleanup() { Dispatchers.resetMain() }
    @Test fun `ошибка сохранения не теряет выбор и допускает повтор`() = runTest(dispatcher) {
        val repo = FakeBookRepository()
        val model = BookCollectionPickerViewModel("book_1", repo); runCurrent()
        model.accept(BookCollectionPickerIntent.Toggle("c"))
        repo.failure = BookError.Unavailable
        model.accept(BookCollectionPickerIntent.Save); runCurrent()
        assertEquals(setOf("c"), model.state.value.selected)
        assertFalse(model.state.value.saving)
        repo.failure = null
        model.accept(BookCollectionPickerIntent.Save); runCurrent()
        assertEquals(BookCollectionPickerEffect.Saved, model.effects.first())
        assertEquals(1, repo.book.shelves.collectionCount)
    }
    @Test fun `недоступная подборка не добавляется в выбранные`() = runTest(dispatcher) {
        val model = BookCollectionPickerViewModel("book_1", FakeBookRepository()); runCurrent()
        model.accept(BookCollectionPickerIntent.Toggle("unknown"))
        assertTrue(model.state.value.selected.isEmpty())
        model.accept(BookCollectionPickerIntent.Close)
        assertEquals(BookCollectionPickerEffect.Closed, model.effects.first())
    }
    @Test fun `ошибка первой загрузки даёт повтор`() = runTest(dispatcher) {
        val repo = FakeBookRepository().apply { failure = BookError.Offline }
        val model = BookCollectionPickerViewModel("book_1", repo); runCurrent()
        assertEquals(BookError.Offline, model.state.value.failure)
        repo.failure = null
        model.accept(BookCollectionPickerIntent.Reload); runCurrent()
        assertNull(model.state.value.failure)
        assertEquals(1, model.state.value.items.size)
    }
}
