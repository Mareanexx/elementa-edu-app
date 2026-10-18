package ru.mareanexx.reader_impl.data.epub

import ru.mareanexx.reader_api.domain.model.ReaderChapter

internal data class PreparedPublication(
    val chapters: List<ReaderChapter>,
    val characters: Int,
)
