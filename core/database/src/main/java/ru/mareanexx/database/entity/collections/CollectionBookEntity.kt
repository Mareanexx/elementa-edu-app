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
    tableName = "collection_book",
    indices = [
        Index(value = ["collection_id"]),
        Index(value = ["book_id"]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collection_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class CollectionBookEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: UUID,

    @ColumnInfo(name = "collection_id")
    val collectionId: Int,

    @ColumnInfo(name = "book_id")
    val bookId: UUID,

    @ColumnInfo(name = "position")
    val position: Int,

    @ColumnInfo(name = "added_at")
    val addedAt: OffsetDateTime,
)
