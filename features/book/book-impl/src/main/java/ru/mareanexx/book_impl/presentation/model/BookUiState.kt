package ru.mareanexx.book_impl.presentation.model

import ru.mareanexx.book_api.domain.model.*

data class BookUiState(
    val book: BookDetails? = null,
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val failure: BookError? = null,
    val page: BookPage = BookPage.Detail,
    val shelvesBusy: Boolean = false,
    val descriptionOpen: Boolean = false,
    val galleryIndex: Int? = null,
    val metadataExpanded: Boolean = false,
    val reviewDraft: ReviewDraft? = null,
    val reviewDeleteOpen: Boolean = false,
    val deletingReview: Boolean = false,
    val collections: BookCollectionsState? = null,
    val reviews: ReviewsUiState = ReviewsUiState(),
    val thread: ReviewThreadState? = null,
    val threadReturnPage: BookPage = BookPage.Reviews,
    val votingIds: Set<String> = emptySet(),
    val operationError: BookError? = null,
    val notice: BookNotice? = null,
    val noticeVersion: Long = 0,
) {
    val mutationBusy: Boolean get() = shelvesBusy || reviewDraft?.saving == true || deletingReview || collections?.saving == true || thread?.posting == true || thread?.deleting == true
}
