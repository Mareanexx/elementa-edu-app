package ru.mareanexx.database.entity.test

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.OffsetDateTime

@Entity(
    tableName = "test_result",
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
data class TestResultEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "test_id")
    val testId: Int,

    @ColumnInfo(name = "score")
    val score: Int,

    @ColumnInfo(name = "total_questions")
    val totalQuestions: Int,

    @ColumnInfo(name = "correct_answers")
    val correctAnswers: Int,

    @ColumnInfo(name = "passed_at")
    val passedAt: OffsetDateTime,

    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean,
)
