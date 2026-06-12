package ru.mareanexx.database.entity.book

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.OffsetDateTime
import java.util.UUID

@Entity(tableName = "book")
data class BookEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: UUID,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "author")
    val author: String,

    @ColumnInfo(name = "cover_path")
    val coverPath: String?,

    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "page_count")
    val pageCount: Int,

    @ColumnInfo(name = "rating")
    val rating: Double,

    @ColumnInfo(name = "review_count")
    val reviewCount: Int,

    @ColumnInfo(name = "file_size_mb")
    val fileSizeMb: Int,

    @ColumnInfo(name = "category")
    val category: String?,

    @ColumnInfo(name = "difficulty_level")
    val difficultyLevel: String?,

    @ColumnInfo(name = "epub_url")
    val epubUrl: String?,

    @ColumnInfo(name = "cached_at")
    val cachedAt: OffsetDateTime,
)