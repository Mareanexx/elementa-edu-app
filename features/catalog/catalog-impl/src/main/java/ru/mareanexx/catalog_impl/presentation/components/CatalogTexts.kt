package ru.mareanexx.catalog_impl.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ru.mareanexx.catalog_api.domain.model.*
import ru.mareanexx.catalog_impl.R
import ru.mareanexx.catalog_impl.presentation.model.CatalogNotice

@Composable
internal fun errorText(error: CatalogError) = stringResource(when (error) {
    CatalogError.Offline -> R.string.catalog_offline
    CatalogError.Timeout -> R.string.catalog_timeout
    CatalogError.Unauthorized -> R.string.catalog_unauthorized
    CatalogError.NotFound -> R.string.catalog_not_found
    CatalogError.Conflict -> R.string.catalog_conflict
    CatalogError.RateLimited -> R.string.catalog_rate_limited
    CatalogError.Unavailable -> R.string.catalog_unavailable
    CatalogError.Storage -> R.string.catalog_storage
    CatalogError.InvalidResponse -> R.string.catalog_invalid
})
@Composable
internal fun sortText(sort: CatalogSort) = stringResource(when (sort) {
    CatalogSort.Recommended -> R.string.catalog_sort_recommended
    CatalogSort.Popular -> R.string.catalog_sort_popular
    CatalogSort.Newest -> R.string.catalog_sort_newest
    CatalogSort.Title -> R.string.catalog_sort_title
    CatalogSort.Relevance -> R.string.catalog_sort_relevance
})
internal fun noticeResource(notice: CatalogNotice) = when (notice) {
    CatalogNotice.FavoriteAdded -> R.string.catalog_favorite_added
    CatalogNotice.FavoriteRemoved -> R.string.catalog_favorite_removed
    CatalogNotice.PlannedAdded -> R.string.catalog_planned_added
    CatalogNotice.PlannedRemoved -> R.string.catalog_planned_removed
}
@Composable
internal fun sectionText(id: String) = stringResource(when (id) {
    "popular" -> R.string.home_popular
    "newest" -> R.string.home_newest
    else -> R.string.home_recommended
})

/** В текущей русской локализации окончания не зависят от языка системы устройства. */
@Composable
internal fun quantityText(@androidx.annotation.PluralsRes resource: Int, count: Int): String {
    val context = androidx.compose.ui.platform.LocalContext.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val resources = androidx.compose.runtime.remember(context, configuration) {
        val localized = android.content.res.Configuration(configuration)
        localized.setLocale(java.util.Locale.forLanguageTag("ru"))
        context.createConfigurationContext(localized).resources
    }
    return resources.getQuantityString(resource, count, count)
}
