package ru.mareanexx.database.db.converter

import androidx.room.TypeConverter
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Класс-конвертер, позволяющий конвертировать тип OffsetDateTime (время с таймзоной)
 * в строку и обратно
 */
class Converters {

    /**
     * UUID
     */
    @TypeConverter
    fun fromUuid(value: UUID?): String? = value?.toString()

    @TypeConverter
    fun toUuid(value: String?): UUID? = value?.let { UUID.fromString(it) }

    /**
     * OffsetDateTime
     */
    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    @TypeConverter
    fun fromOffsetDateTime(value: OffsetDateTime?): String? =
        value?.format(formatter)

    @TypeConverter
    fun toOffsetDateTime(value: String?): OffsetDateTime? =
        value?.let { OffsetDateTime.parse(it, formatter) }
}