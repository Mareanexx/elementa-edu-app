package ru.mareanexx.reader_impl.data.local.entity

import androidx.room.Entity

@Entity(tableName = "reader_positions", primaryKeys = ["userId", "bookId"])
internal data class PositionEntity(
    val userId: String,
    val bookId: String,
    val payload: String,
    val revision: Long,
    val syncedRevision: Long = 0,
)
