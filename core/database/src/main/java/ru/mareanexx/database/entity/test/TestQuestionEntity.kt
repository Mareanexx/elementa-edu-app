package ru.mareanexx.database.entity.test

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "test_question",
    indices = [Index(value = ["test_id"])],
    foreignKeys = [
        ForeignKey(
            entity = TestEntity::class,
            parentColumns = ["id"],
            childColumns = ["test_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class TestQuestionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "test_id")
    val testId: Int,

    @ColumnInfo(name = "question_text")
    val questionText: String,

    @ColumnInfo(name = "question_type")
    val questionType: String,

    @ColumnInfo(name = "position")
    val position: Int,
)