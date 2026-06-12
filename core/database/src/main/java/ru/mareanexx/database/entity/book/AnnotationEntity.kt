package ru.mareanexx.database.entity.book

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.OffsetDateTime
import java.util.UUID

@Entity(
    tableName = "annotation",
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
data class AnnotationEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "book_id")
    val bookId: UUID,

    @ColumnInfo(name = "start_offset")
    val startOffset: Int,

    @ColumnInfo(name = "end_offset")
    val endOffset: Int,

    @ColumnInfo(name = "highlight_color")
    val highlightColor: String,

    @ColumnInfo(name = "comment")
    val comment: String?,

    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean,

    @ColumnInfo(name = "created_at")
    val createdAt: OffsetDateTime,
)