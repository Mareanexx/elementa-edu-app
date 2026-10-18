package ru.mareanexx.reader_impl.data.local

import androidx.room.withTransaction
import com.google.gson.Gson
import kotlinx.coroutines.flow.map
import ru.mareanexx.reader_api.domain.model.*
import ru.mareanexx.reader_impl.data.local.entity.*

internal class ReaderLocalStore(
    val database: ReaderDatabase,
    val userId: String,
    val gson: Gson,
) {
    val dao = database.readerDao()
    val transfers = dao.transfers(userId)
    val settings = dao.settings(userId).map { row -> row?.let { runCatching { gson.fromJson(it.payload, ReaderSettings::class.java) }.getOrNull() } ?: ReaderSettings() }
    suspend fun mutateTransfer(bookId: String, transform: (TransferEntity) -> TransferEntity): TransferEntity = database.withTransaction {
        val next = transform(dao.transfer(userId, bookId) ?: TransferEntity(userId, bookId))
        dao.putTransfer(next)
        next
    }
    suspend fun savePosition(value: ReaderPosition) = database.withTransaction {
        val current = dao.position(userId, value.bookId)
        val revision = (current?.revision ?: 0) + 1
        dao.putPosition(PositionEntity(userId, value.bookId, gson.toJson(value), revision, current?.syncedRevision ?: 0))
    }
    suspend fun saveAnnotation(value: ReaderAnnotation) = database.withTransaction {
        val current = dao.annotation(userId, value.id)
        val revision = (current?.revision ?: 0) + 1
        val updated = value.copy(revision = revision, updatedAt = System.currentTimeMillis())
        dao.putAnnotation(AnnotationEntity(userId, value.id, value.bookId, gson.toJson(updated), revision, current?.syncedRevision ?: 0, updated.deleted))
    }
    suspend fun mergeAnnotation(value: ReaderAnnotation) = database.withTransaction {
        val current = dao.annotation(userId, value.id)
        val previous = current?.let { gson.fromJson(it.payload, ReaderAnnotation::class.java) }
        if (current == null || (current.revision == current.syncedRevision && (previous?.updatedAt ?: 0) < value.updatedAt)) {
            dao.putAnnotation(AnnotationEntity(userId, value.id, value.bookId, gson.toJson(value), value.revision, value.revision, value.deleted))
        }
    }
    suspend fun position(bookId: String): ReaderPosition? = dao.position(userId, bookId)?.let { gson.fromJson(it.payload, ReaderPosition::class.java) }
    fun annotations(bookId: String) = dao.annotations(userId, bookId).map { rows -> rows.filterNot { it.deleted }.map { gson.fromJson(it.payload, ReaderAnnotation::class.java) }.sortedBy { it.anchor.start } }
}
