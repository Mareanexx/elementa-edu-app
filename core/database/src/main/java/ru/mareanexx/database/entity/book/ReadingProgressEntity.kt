package ru.mareanexx.database.entity.book

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.OffsetDateTime
import java.util.UUID

@Entity(
    tableName = "reading_progress",
    indices = [Index(value = ["book_id"])],
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class ReadingProgressEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "book_id")
    val bookId: UUID,

    @ColumnInfo(name = "current_page")
    val currentPage: Int,

    @ColumnInfo(name = "percent_completed")
    val percentCompleted: Double,

    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean,

    @ColumnInfo(name = "updated_at")
    val updatedAt: OffsetDateTime,
)