package ru.mareanexx.catalog_impl.presentation.notifications

import androidx.compose.runtime.Composable
import ru.mareanexx.catalog_impl.presentation.components.quantityText
import androidx.compose.ui.res.stringResource
import ru.mareanexx.catalog_impl.R
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun notificationTime(createdAt: Instant, now: Instant): String {
    val seconds = Duration.between(createdAt, now).seconds.coerceAtLeast(0)
    val count: Int
    val resource: Int
    when {
        seconds < 60 -> return stringResource(R.string.notifications_now)
        seconds < 3600 -> { count = (seconds / 60).toInt(); resource = R.plurals.notifications_minutes }
        seconds < 86_400 -> { count = (seconds / 3600).toInt(); resource = R.plurals.notifications_hours }
        seconds < 604_800 -> { count = (seconds / 86_400).toInt(); resource = R.plurals.notifications_days }
        else -> return DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("ru")).withZone(ZoneId.systemDefault()).format(createdAt)
    }
    return quantityText(resource, count)
}
