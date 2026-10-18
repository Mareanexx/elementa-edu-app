package ru.mareanexx.reader_impl.data.local

import android.content.Context
import androidx.room.Room

internal object ReaderDatabaseProvider {
    @Volatile private var instance: ReaderDatabase? = null
    fun get(context: Context): ReaderDatabase = instance ?: synchronized(this) {
        instance ?: Room.databaseBuilder(context.applicationContext, ReaderDatabase::class.java, "elementa_reader.db").addMigrations(ReaderMigrations.firstToSecond).build().also { instance = it }
    }
}
