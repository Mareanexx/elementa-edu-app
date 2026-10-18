package ru.mareanexx.user_library_api.domain.model

data class LibraryCounts(
    val all: Int = 0,
    val inProgress: Int = 0,
    val completed: Int = 0,
    val wantToRead: Int = 0,
    val downloaded: Int = 0,
) {
    fun count(category: LibraryCategory): Int = when (category) {
        LibraryCategory.All -> all
        LibraryCategory.InProgress -> inProgress
        LibraryCategory.Completed -> completed
        LibraryCategory.WantToRead -> wantToRead
        LibraryCategory.Downloaded -> downloaded
    }
}
