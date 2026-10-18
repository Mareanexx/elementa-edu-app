package ru.mareanexx.reader_api.domain.validation

import ru.mareanexx.reader_api.domain.model.*

object ReaderRules {
    const val MAX_NOTE = 4000
    const val MAX_QUOTE = 10000
    const val MAX_PACKAGE_BYTES = 100L * 1024 * 1024
    fun valid(anchor: ReaderAnchor) = anchor.version.isNotBlank() && anchor.chapterId.isNotBlank() && anchor.start >= 0 && anchor.end > anchor.start && anchor.quote.length in 1..MAX_QUOTE && anchor.end - anchor.start == anchor.quote.length
    fun valid(settings: ReaderSettings) = settings.fontSize.isFinite() && settings.fontSize in 14f..30f && settings.lineHeight.isFinite() && settings.lineHeight in 1.3f..2f && settings.margin in 12..40
    fun valid(position: ReaderPosition) = position.bookId.isNotBlank() && position.version.isNotBlank() && position.offset >= 0 && position.progression.isFinite() && position.progression in 0.0..1.0 && position.sourcePagesRead >= 0
}
