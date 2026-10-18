package ru.mareanexx.reader_impl.data.local.entity

import androidx.room.Entity

@Entity(tableName = "reader_transfers", primaryKeys = ["userId", "bookId"])
internal data class TransferEntity(
    val userId: String,
    val bookId: String,
    @androidx.room.ColumnInfo(defaultValue = "''")
    val requestId: String = "",
    val title: String = "",
    val pinned: Boolean = false,
    val phase: String = "Queued",
    val bytesRead: Long = 0,
    val totalBytes: Long = 0,
    val error: String? = null,
    val manifestJson: String? = null,
    val directory: String? = null,
    val completedAt: Long = 0,
    val lastAccess: Long = 0,
)
