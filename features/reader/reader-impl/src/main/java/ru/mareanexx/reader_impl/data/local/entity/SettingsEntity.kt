package ru.mareanexx.reader_impl.data.local.entity

import androidx.room.Entity

@Entity(tableName = "reader_settings", primaryKeys = ["userId"])
internal data class SettingsEntity(
    val userId: String,
    val payload: String,
)
