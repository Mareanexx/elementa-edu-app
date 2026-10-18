package ru.mareanexx.catalog_api.domain.model

data class HomeFeed(
    val continueReading: List<CatalogBook>,
    val categories: List<CatalogCategory>,
    val sections: List<HomeSection>,
    val libraryCount: Int,
    val collectionCount: Int,
    val unread: UnreadState,
)
