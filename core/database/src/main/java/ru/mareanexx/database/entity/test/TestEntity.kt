package ru.mareanexx.database.entity.test

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ru.mareanexx.database.entity.book.BookEntity
import java.time.OffsetDateTime
import java.util.UUID

@Entity(
    tableName = "test",
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
data class TestEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "book_id")
    val bookId: UUID,

    @ColumnInfo(name = "chapter_title")
    val chapterTitle: String,

    @ColumnInfo(name = "questions_count")
    val questionsCount: Int,

    @ColumnInfo(name = "cached_at")
    val cachedAt: OffsetDateTime,
)