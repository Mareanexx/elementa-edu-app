package ru.mareanexx.user_library_api.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.mareanexx.user_library_api.domain.model.*

interface LibraryRepository {
    val changes: Flow<Unit>
    val recentQueries: Flow<List<String>>
    suspend fun getBooks(query: LibraryQuery, cursor: String?): LibraryResult<LibraryPage>
    suspend fun getPreferences(): LibraryResult<LibraryPreferences>
    suspend fun savePreferences(value: LibraryPreferences): LibraryResult<LibraryPreferences>
    suspend fun setReadingStatus(bookId: String, status: ReadingStatus): LibraryResult<LibraryBook>
    suspend fun rememberQuery(query: String): LibraryResult<Unit>
    suspend fun clearRecentQueries(): LibraryResult<Unit>
}
