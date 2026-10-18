package ru.mareanexx.user_library_impl.presentation.model

import ru.mareanexx.user_library_api.domain.model.*
import ru.mareanexx.user_library_api.domain.validation.LibrarySearchRules

data class LibraryStatusChange(
    val book: LibraryBook,
    val status: ReadingStatus,
)
