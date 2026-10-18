package ru.mareanexx.book_impl

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.mareanexx.book_impl.presentation.model.ReviewAge
import ru.mareanexx.book_impl.presentation.model.ReviewTimeUnit

class ReviewAgeTest {
    private val now = 1_800_000_000_000L
    private fun ago(seconds: Long) = ReviewAge.between(now - seconds * 1000, now)

    @Test fun relativeTimeChangesAtUnitBoundaries() {
        assertEquals(ReviewTimeUnit.Now, ago(59).unit)
        assertEquals(ReviewAge(ReviewTimeUnit.Minute, 1), ago(60))
        assertEquals(ReviewAge(ReviewTimeUnit.Minute, 59), ago(3599))
        assertEquals(ReviewAge(ReviewTimeUnit.Hour, 1), ago(3600))
        assertEquals(ReviewAge(ReviewTimeUnit.Hour, 23), ago(86399))
        assertEquals(ReviewAge(ReviewTimeUnit.Day, 1), ago(86400))
        assertEquals(ReviewAge(ReviewTimeUnit.Week, 1), ago(604800))
        assertEquals(ReviewAge(ReviewTimeUnit.Month, 1), ago(2592000))
        assertEquals(ReviewAge(ReviewTimeUnit.Year, 1), ago(31536000))
    }

    @Test fun clockSkewNeverShowsNegativeTime() {
        assertEquals(ReviewTimeUnit.Now, ReviewAge.between(now + 60_000, now).unit)
    }
}
