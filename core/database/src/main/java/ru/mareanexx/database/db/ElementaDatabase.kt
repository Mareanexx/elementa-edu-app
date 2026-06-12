package ru.mareanexx.database.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.mareanexx.database.db.converter.Converters
import ru.mareanexx.database.entity.HomeCacheEntity
import ru.mareanexx.database.entity.UserProfileEntity
import ru.mareanexx.database.entity.book.AnnotationEntity
import ru.mareanexx.database.entity.book.BookEntity
import ru.mareanexx.database.entity.book.ReadingProgressEntity
import ru.mareanexx.database.entity.collections.CollectionBookEntity
import ru.mareanexx.database.entity.collections.CollectionEntity
import ru.mareanexx.database.entity.collections.UserLibraryEntity
import ru.mareanexx.database.entity.test.TestAnswerOptionEntity
import ru.mareanexx.database.entity.test.TestEntity
import ru.mareanexx.database.entity.test.TestQuestionEntity
import ru.mareanexx.database.entity.test.TestResultEntity

@Database(
    entities = [
        HomeCacheEntity::class,
        BookEntity::class,
        UserProfileEntity::class,
        ReadingProgressEntity::class,
        UserLibraryEntity::class,
        AnnotationEntity::class,
        CollectionEntity::class,
        CollectionBookEntity::class,
        TestEntity::class,
        TestQuestionEntity::class,
        TestAnswerOptionEntity::class,
        TestResultEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class ElementaDatabase: RoomDatabase() {
    // abstract fun profileDao(): ProfileDao TODO:
}
