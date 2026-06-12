package ru.mareanexx.database.entity.collections

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ru.mareanexx.database.entity.UserProfileEntity
import java.time.OffsetDateTime

@Entity(
    tableName = "collection",
    indices = [Index(value = ["user_id"])],
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class CollectionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "user_id")
    val userId: Int,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "cover_path")
    val coverPath: String?,

    @ColumnInfo(name = "books_count")
    val booksCount: Int,

    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean,

    @ColumnInfo(name = "created_at")
    val createdAt: OffsetDateTime,

    @ColumnInfo(name = "updated_at")
    val updatedAt: OffsetDateTime,
)