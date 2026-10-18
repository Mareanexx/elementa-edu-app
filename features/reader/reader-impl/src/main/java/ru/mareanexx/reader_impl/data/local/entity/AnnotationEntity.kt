package ru.mareanexx.reader_impl.data.local.entity

import androidx.room.Entity

@Entity(tableName = "reader_annotations", primaryKeys = ["userId", "id"])
internal data class AnnotationEntity(
    val userId: String,
    val id: String,
    val bookId: String,
    val payload: String,
    val revision: Long,
    val syncedRevision: Long = 0,
    val deleted: Boolean = false,
)
