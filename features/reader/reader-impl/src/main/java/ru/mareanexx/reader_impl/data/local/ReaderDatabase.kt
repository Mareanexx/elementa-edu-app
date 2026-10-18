package ru.mareanexx.reader_impl.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.mareanexx.reader_impl.data.local.entity.*

/** Отдельное хранилище ридера не меняет пока не подключённую общую схему приложения. */
@Database(entities = [TransferEntity::class, PositionEntity::class, AnnotationEntity::class, SettingsEntity::class], version = 2, exportSchema = true)
internal abstract class ReaderDatabase : RoomDatabase() {
    abstract fun readerDao(): ReaderDao
}
