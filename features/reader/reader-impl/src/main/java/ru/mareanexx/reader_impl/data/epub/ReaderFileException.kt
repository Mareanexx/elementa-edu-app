package ru.mareanexx.reader_impl.data.epub

import java.io.IOException
import ru.mareanexx.reader_api.domain.model.ReaderError

internal class ReaderFileException(
    val error: ReaderError,
) : IOException(error.name)
