package ru.mareanexx.reader_impl.data.remote.mapper

import ru.mareanexx.book_api.domain.model.*
import ru.mareanexx.reader_api.domain.model.*
import ru.mareanexx.reader_api.domain.validation.ReaderRules
import ru.mareanexx.reader_impl.data.remote.dto.*
import ru.mareanexx.reader_impl.data.remote.model.ReadingManifest
import java.net.URI

internal object ReaderMapper {
    fun manifest(dto: ReadingManifestDto, baseUrl: String): ReadingManifest {
        require(dto.format == "application/epub+zip")
        val base = URI(baseUrl)
        val path = text(dto.contentPath)
        require(path.startsWith("/api/v1/") && !path.startsWith("//") && !path.contains("\\"))
        val url = base.resolve(path)
        require(url.scheme == base.scheme && url.host == base.host && url.port == base.port && url.userInfo == null)
        return ReadingManifest(book(requireNotNull(dto.book)), text(dto.version), url.toString(),
            requireNotNull(dto.byteSize).also { require(it in 1..ReaderRules.MAX_PACKAGE_BYTES) }, text(dto.sha256).also { require(it.matches(Regex("[a-f0-9]{64}"))) })
    }
    fun book(dto: ReaderBookDto): ReaderBook {
        val pages = requireNotNull(dto.pages).also { require(it > 0) }
        val read = requireNotNull(dto.pagesRead).also { require(it in 0..pages) }
        val count = requireNotNull(dto.ratingCount).also { require(it >= 0) }
        val average = dto.ratingAverage?.also { require(it.isFinite() && it in 1.0..5.0 && count > 0) }
        return ReaderBook(text(dto.id), text(dto.title), text(dto.authors), pages, read, dto.coverUrl, dto.coverKey.orEmpty(), average, count,
            dto.tags.orEmpty().map { BookTag(text(it.id), text(it.label), BookTagKind.entries.firstOrNull { kind -> kind.name.equals(it.kind, true) } ?: BookTagKind.Topic) },
            BookShelves(requireNotNull(dto.favorite), requireNotNull(dto.wantToRead), requireNotNull(dto.collectionCount).also { require(it in 0..30) }))
    }
    fun annotation(dto: ReaderAnnotationDto): ReaderAnnotation {
        val source = requireNotNull(dto.anchor)
        val anchor = ReaderAnchor(text(source.version), text(source.chapterId), requireNotNull(source.start), requireNotNull(source.end), text(source.quote), source.prefix.orEmpty().take(64), source.suffix.orEmpty().take(64))
        require(ReaderRules.valid(anchor))
        val note = dto.note?.also { require(it.length <= ReaderRules.MAX_NOTE) }
        return ReaderAnnotation(text(dto.id), text(dto.bookId), anchor, HighlightColor.entries.first { it.name.equals(dto.color, true) }, note,
            requireNotNull(dto.createdAt).also { require(it > 0) }, requireNotNull(dto.updatedAt).also { require(it > 0) }, requireNotNull(dto.revision).also { require(it > 0) }, dto.deleted ?: false)
    }
    fun annotation(value: ReaderAnnotation) = ReaderAnnotationDto(value.id, value.bookId,
        ReaderAnchorDto(value.anchor.version, value.anchor.chapterId, value.anchor.start, value.anchor.end, value.anchor.quote, value.anchor.prefix, value.anchor.suffix),
        value.color.name.lowercase(), value.note, value.createdAt, value.updatedAt, value.revision, value.deleted)
    fun position(value: ReaderPositionDto) = ReaderPosition(text(value.bookId), text(value.version), value.chapterId.orEmpty(), requireNotNull(value.offset), requireNotNull(value.progression), requireNotNull(value.sourcePagesRead), requireNotNull(value.updatedAt)).also { require(ReaderRules.valid(it)) }
    fun position(value: ReaderPosition) = ReaderPositionDto(value.bookId, value.version, value.chapterId, value.offset, value.progression, value.sourcePagesRead, value.updatedAt)
    private fun text(value: String?) = requireNotNull(value).also { require(it.isNotBlank()) }
}
