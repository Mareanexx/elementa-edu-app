package ru.mareanexx.user_library_impl.presentation.model

import ru.mareanexx.user_library_api.domain.model.*
import ru.mareanexx.user_library_api.domain.validation.LibrarySearchRules

enum class LibraryNotice { FavoriteAdded, FavoriteRemoved, Planned, Unplanned, Completed, Reset }
