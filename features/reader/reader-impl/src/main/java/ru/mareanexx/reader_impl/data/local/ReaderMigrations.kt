package ru.mareanexx.reader_impl.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal object ReaderMigrations {
    val firstToSecond = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE reader_transfers ADD COLUMN requestId TEXT NOT NULL DEFAULT ''")
        }
    }
}
