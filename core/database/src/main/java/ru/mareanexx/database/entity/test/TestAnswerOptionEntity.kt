package ru.mareanexx.database.entity.test

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "test_answer_option",
    indices = [Index(value = ["question_id"])],
    foreignKeys = [
        ForeignKey(
            entity = TestQuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["question_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class TestAnswerOptionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: UUID,

    @ColumnInfo(name = "question_id")
    val questionId: Int,

    @ColumnInfo(name = "option_text")
    val optionText: String,

    @ColumnInfo(name = "is_correct")
    val isCorrect: Boolean,
)
