package ru.mareanexx.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.OffsetDateTime

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "username")
    val username: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "avatar_path")
    val avatarPath: String?,

    @ColumnInfo(name = "books_read_count")
    val booksReadCount: Int,

    @ColumnInfo(name = "annotations_count")
    val annotationsCount: Int,

    @ColumnInfo(name = "tests_passed_count")
    val testsPassedCount: Int,

    @ColumnInfo(name = "cached_at")
    val cachedAt: OffsetDateTime,

    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean,
)
