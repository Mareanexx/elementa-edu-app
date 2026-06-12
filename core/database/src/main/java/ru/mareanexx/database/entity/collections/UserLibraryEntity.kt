package ru.mareanexx.database.entity.collections

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ru.mareanexx.database.entity.book.BookEntity
import java.time.OffsetDateTime
import java.util.UUID

@Entity(
    tableName = "user_library",
    indices = [
        Index(value = ["book_id"]),
        Index(value = ["status"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class UserLibraryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "book_id")
    val bookId: UUID,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "added_at")
    val addedAt: OffsetDateTime,

    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean,
)