package ru.mareanexx.collections_impl.presentation.screen.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ru.mareanexx.collections_api.domain.model.*
import ru.mareanexx.collections_impl.presentation.model.CollectionsUiState
import ru.mareanexx.collections_impl.presentation.screen.CollectionsScreen
import ru.mareanexx.ui.theme.ElementaAppTheme

@Preview
@Composable
private fun CollectionsPreview() {
    ElementaAppTheme {
        CollectionsScreen(CollectionsUiState(loading = false, items = listOf(
            BookCollection("favorite", "", CollectionCover.Chemistry, 12, true),
            BookCollection("organic", "Органическая химия", CollectionCover.Chemistry, 8),
            BookCollection("science", "Научные статьи", CollectionCover.Molecules, 14),
        )), all = false, onIntent = {})
    }
}
