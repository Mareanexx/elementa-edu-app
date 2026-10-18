package ru.mareanexx.book_impl.presentation

import ru.mareanexx.book_api.domain.model.*

sealed interface BookIntent {
    data object Reload : BookIntent
    data object Back : BookIntent
    data object SessionExpired : BookIntent
    data object DismissNotice : BookIntent
    data object ToggleFavorite : BookIntent
    data object ToggleWantToRead : BookIntent
    data object Read : BookIntent
    data object ShowDescription : BookIntent
    data object DismissDescription : BookIntent
    data object DismissGallery : BookIntent
    data object ToggleMetadata : BookIntent
    data object OpenReviews : BookIntent
    data object ReloadReviews : BookIntent
    data object LoadMoreReviews : BookIntent
    data object OpenReviewEditor : BookIntent
    data object CloseReviewEditor : BookIntent
    data object SaveReview : BookIntent
    data object AskDeleteReview : BookIntent
    data object CancelDeleteReview : BookIntent
    data object ConfirmDeleteReview : BookIntent
    data object ReloadReplies : BookIntent
    data object LoadMoreReplies : BookIntent
    data object SubmitReply : BookIntent
    data object CancelDeleteReply : BookIntent
    data object ConfirmDeleteReply : BookIntent
    data object OpenCollections : BookIntent
    data object ReloadCollections : BookIntent
    data object SaveCollections : BookIntent
    data object CloseCollections : BookIntent
    data class OpenGallery(
        val index: Int,
    ) : BookIntent
    data class ChangeGalleryPage(
        val index: Int,
    ) : BookIntent
    data class ChangeReviewQuery(
        val query: ReviewsQuery,
    ) : BookIntent
    data class ChangeReviewRating(
        val rating: Int,
    ) : BookIntent
    data class ChangeReviewText(
        val text: String,
    ) : BookIntent
    data class React(
        val id: String,
        val reaction: ReviewReaction,
    ) : BookIntent
    data class OpenThread(
        val id: String,
    ) : BookIntent
    data class ChangeReply(
        val text: String,
    ) : BookIntent
    data class AskDeleteReply(
        val id: String,
    ) : BookIntent
    data class ToggleCollection(
        val id: String,
    ) : BookIntent
}
