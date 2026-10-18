package ru.mareanexx.book_impl.presentation

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ru.mareanexx.book_api.domain.model.BookError
import ru.mareanexx.book_api.domain.repository.BookRepository
import ru.mareanexx.book_impl.presentation.components.BookCollectionsSheet
import ru.mareanexx.book_impl.presentation.model.BookCollectionPickerEffect

@Composable
fun BookCollectionPickerRoute(
    bookId: String,
    userId: String,
    repository: BookRepository,
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
    onSessionExpired: () -> Unit,
) {
    // Хранилище ViewModel освобождается после закрытия выбора подборок.
    val owner = remember(bookId, userId) { object : androidx.lifecycle.ViewModelStoreOwner {
        override val viewModelStore = androidx.lifecycle.ViewModelStore()
    } }
    DisposableEffect(owner) { onDispose { owner.viewModelStore.clear() } }
    val model: BookCollectionPickerViewModel = viewModel(owner, factory = viewModelFactory { initializer { BookCollectionPickerViewModel(bookId, repository) } })
    val state by model.state.collectAsStateWithLifecycle()
    val dismiss by rememberUpdatedState(onDismiss)
    val saved by rememberUpdatedState(onSaved)
    LaunchedEffect(model) { model.effects.collect { effect ->
        if (effect == BookCollectionPickerEffect.Saved) saved() else dismiss()
    } }
    LaunchedEffect(state.failure) { if (state.failure == BookError.Unauthorized) { dismiss(); onSessionExpired() } }
    BookCollectionsSheet(state) { intent -> when (intent) {
        BookIntent.ReloadCollections -> model.accept(BookCollectionPickerIntent.Reload)
        BookIntent.SaveCollections -> model.accept(BookCollectionPickerIntent.Save)
        BookIntent.CloseCollections -> model.accept(BookCollectionPickerIntent.Close)
        is BookIntent.ToggleCollection -> model.accept(BookCollectionPickerIntent.Toggle(intent.id))
        else -> Unit
    } }
}
