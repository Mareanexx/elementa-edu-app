package ru.mareanexx.book_impl.presentation.components

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalResources
import kotlinx.coroutines.delay
import ru.mareanexx.book_impl.R
import ru.mareanexx.book_impl.presentation.model.ReviewAge
import ru.mareanexx.book_impl.presentation.model.ReviewTimeUnit

/** Относительное время обновляется, пока отзыв находится на экране. */
@Composable
internal fun reviewDate(timestamp: Long): String {
    val now by produceState(System.currentTimeMillis(), timestamp) {
        while (true) {
            value = System.currentTimeMillis()
            delay(30_000)
        }
    }
    val age = ReviewAge.between(timestamp, now)
    val strings = LocalResources.current
    val resource = when (age.unit) {
        ReviewTimeUnit.Now -> return strings.getString(R.string.book_time_now)
        ReviewTimeUnit.Minute -> R.plurals.book_time_minutes
        ReviewTimeUnit.Hour -> R.plurals.book_time_hours
        ReviewTimeUnit.Day -> R.plurals.book_time_days
        ReviewTimeUnit.Week -> R.plurals.book_time_weeks
        ReviewTimeUnit.Month -> R.plurals.book_time_months
        ReviewTimeUnit.Year -> R.plurals.book_time_years
    }
    return strings.getQuantityString(resource, age.amount, age.amount)
}
