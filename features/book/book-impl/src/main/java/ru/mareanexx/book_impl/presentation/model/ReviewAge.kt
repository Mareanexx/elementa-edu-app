package ru.mareanexx.book_impl.presentation.model

import java.time.Duration
import java.time.Instant

internal data class ReviewAge(
    val unit: ReviewTimeUnit,
    val amount: Int = 0,
) {
    companion object {
        fun between(timestamp: Long, now: Long): ReviewAge {
            val seconds = Duration.between(Instant.ofEpochMilli(timestamp), Instant.ofEpochMilli(now)).seconds.coerceAtLeast(0)
            val (unit, length) = when {
                seconds < 60 -> ReviewTimeUnit.Now to 1L
                seconds < 3600 -> ReviewTimeUnit.Minute to 60L
                seconds < 86400 -> ReviewTimeUnit.Hour to 3600L
                seconds < 604800 -> ReviewTimeUnit.Day to 86400L
                seconds < 2592000 -> ReviewTimeUnit.Week to 604800L
                seconds < 31536000 -> ReviewTimeUnit.Month to 2592000L
                else -> ReviewTimeUnit.Year to 31536000L
            }
            return ReviewAge(unit, (seconds / length).coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
        }
    }
}
