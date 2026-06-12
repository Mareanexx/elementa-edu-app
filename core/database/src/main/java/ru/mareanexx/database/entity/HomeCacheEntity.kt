package ru.mareanexx.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.OffsetDateTime

@Entity(
    tableName = "home_cache",
    indices = [Index(value = ["section"])]
)
data class HomeCacheEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "section")
    val section: String,

    @ColumnInfo(name = "json_data")
    val jsonData: String,

    @ColumnInfo(name = "cached_at")
    val cachedAt: OffsetDateTime,
)
