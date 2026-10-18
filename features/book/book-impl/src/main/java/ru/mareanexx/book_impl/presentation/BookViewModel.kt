package ru.mareanexx.book_impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.mareanexx.book_api.domain.model.*
import ru.mareanexx.book_api.domain.repository.BookRepository
import ru.mareanexx.book_api.domain.validation.BookInputRules
import ru.mareanexx.book_impl.presentation.model.*
import java.util.UUID

class BookViewModel(
    private val bookId: String,
    private val repository: BookRepository,
) : ViewModel() {
    private val mutable = MutableStateFlow(BookUiState())
    val state = mutable.asStateFlow()
    private val events = Channel<BookEffect>(Channel.BUFFERED)
    val effects = events.receiveAsFlow()
    private var bookJob: Job? = null
    private var reviewsJob: Job? = null
    private var repliesJob: Job? = null
    private var collectionsJob: Job? = null
    private val mutationJobs = mutableSetOf<Job>()
    init { loadBook() }

    fun accept(intent: BookIntent) {
        val current = mutable.value
        if (intent == BookIntent.SessionExpired) {
            bookJob?.cancel(); reviewsJob?.cancel(); repliesJob?.cancel(); collectionsJob?.cancel()
            mutationJobs.forEach { it.cancel() }
            mutable.value = BookUiState(loading = false)
            return
        }
        if (current.mutationBusy && intent != BookIntent.DismissNotice) return
        when (intent) {
            BookIntent.SessionExpired -> Unit
            BookIntent.Reload -> if (!current.loading && !current.refreshing) loadBook()
            BookIntent.Back -> when (current.page) {
                BookPage.Detail -> events.trySend(BookEffect.Close)
                BookPage.Thread -> mutable.update { it.copy(page = it.threadReturnPage) }
                else -> mutable.update { it.copy(page = BookPage.Detail) }
            }
            BookIntent.ToggleFavorite -> current.book?.let { shelf(favorite = true, selected = !it.shelves.favorite) }
            BookIntent.ToggleWantToRead -> current.book?.let { shelf(favorite = false, selected = !it.shelves.wantToRead) }
            BookIntent.Read -> if (current.book != null) mutable.update { it.copy(page = BookPage.Reader) }
            BookIntent.ShowDescription -> mutable.update { it.copy(descriptionOpen = true) }
            BookIntent.DismissDescription -> mutable.update { it.copy(descriptionOpen = false) }
            is BookIntent.OpenGallery -> if (intent.index in current.book?.images.orEmpty().indices) mutable.update { it.copy(galleryIndex = intent.index) }
            is BookIntent.ChangeGalleryPage -> if (intent.index in current.book?.images.orEmpty().indices) mutable.update { it.copy(galleryIndex = intent.index) }
            BookIntent.DismissGallery -> mutable.update { it.copy(galleryIndex = null) }
            BookIntent.ToggleMetadata -> mutable.update { it.copy(metadataExpanded = !it.metadataExpanded) }
            BookIntent.OpenReviews -> {
                mutable.update { it.copy(page = BookPage.Reviews) }
                if (!current.reviews.loaded && !current.reviews.loading) loadReviews(reset = true)
            }
            BookIntent.ReloadReviews -> loadReviews(reset = true)
            BookIntent.LoadMoreReviews -> if (!current.reviews.loading && !current.reviews.refreshing && !current.reviews.loadingMore && current.reviews.cursor != null) loadReviews(reset = false)
            is BookIntent.ChangeReviewQuery -> if (intent.query != current.reviews.query && intent.query.rating?.let { it in 1..5 } != false) {
                mutable.update { it.copy(reviews = ReviewsUiState(query = intent.query)) }
                loadReviews(reset = true)
            }
            BookIntent.OpenReviewEditor -> mutable.update { it.copy(reviewDraft = ReviewDraft(it.book?.myReview?.rating ?: 0, it.book?.myReview?.text.orEmpty())) }
            BookIntent.CloseReviewEditor -> mutable.update { it.copy(reviewDraft = null) }
            is BookIntent.ChangeReviewRating -> if (intent.rating in 1..5) mutable.update { it.copy(reviewDraft = it.reviewDraft?.copy(rating = intent.rating, fieldError = null, requestError = null)) }
            is BookIntent.ChangeReviewText -> mutable.update { it.copy(reviewDraft = it.reviewDraft?.copy(text = intent.text.take(BookInputRules.MAX_REVIEW_LENGTH), fieldError = null, requestError = null)) }
            BookIntent.SaveReview -> saveReview()
            BookIntent.AskDeleteReview -> if (current.book?.myReview != null) mutable.update { it.copy(reviewDeleteOpen = true) }
            BookIntent.CancelDeleteReview -> mutable.update { it.copy(reviewDeleteOpen = false) }
            BookIntent.ConfirmDeleteReview -> deleteReview()
            is BookIntent.React -> react(intent.id, intent.reaction)
            is BookIntent.OpenThread -> openThread(intent.id)
            BookIntent.ReloadReplies -> loadReplies(reset = true)
            BookIntent.LoadMoreReplies -> if (current.thread?.let { !it.loading && !it.loadingMore && it.cursor != null } == true) loadReplies(reset = false)
            is BookIntent.ChangeReply -> mutable.update { it.copy(thread = it.thread?.copy(draft = intent.text.take(BookInputRules.MAX_REPLY_LENGTH), requestKey = null, fieldError = null, postError = null)) }
            BookIntent.SubmitReply -> submitReply()
            is BookIntent.AskDeleteReply -> if (current.thread?.items?.any { it.id == intent.id && it.isMine } == true) mutable.update { it.copy(thread = it.thread?.copy(deleteId = intent.id)) }
            BookIntent.CancelDeleteReply -> mutable.update { it.copy(thread = it.thread?.copy(deleteId = null)) }
            BookIntent.ConfirmDeleteReply -> deleteReply()
            BookIntent.OpenCollections -> if (current.collections == null) { mutable.update { it.copy(collections = BookCollectionsState()) }; loadCollections() }
            BookIntent.ReloadCollections -> loadCollections()
            BookIntent.CloseCollections -> { collectionsJob?.cancel(); mutable.update { it.copy(collections = null) } }
            is BookIntent.ToggleCollection -> mutable.update { s ->
                val picker = s.collections ?: return@update s
                if (picker.items.none { it.id == intent.id }) s else s.copy(collections = picker.copy(selected = if (intent.id in picker.selected) picker.selected - intent.id else picker.selected + intent.id, failure = null))
            }
            BookIntent.SaveCollections -> saveCollections()
            BookIntent.DismissNotice -> mutable.update { it.copy(notice = null, operationError = null) }
        }
    }
    private fun loadBook() {
        bookJob?.cancel()
        mutable.update { it.copy(loading = it.book == null, refreshing = it.book != null, failure = null) }
        bookJob = viewModelScope.launch {
            when (val result = repository.getBook(bookId)) {
                is BookResult.Success -> mutable.update { it.copy(book = result.value, loading = false, refreshing = false) }
                is BookResult.Failure -> mutable.update { it.copy(failure = result.error, loading = false, refreshing = false) }
            }
        }
    }
    private fun cancelStaleReads() {
        bookJob?.cancel(); reviewsJob?.cancel()
        mutable.update { it.copy(refreshing = false, reviews = it.reviews.copy(loading = false, refreshing = false, loadingMore = false)) }
    }
    private fun mutate(block: suspend () -> Unit) {
        mutationJobs += viewModelScope.launch { block() }
        mutationJobs.removeAll { it.isCompleted }
    }
    private fun shelf(favorite: Boolean, selected: Boolean) {
        cancelStaleReads()
        mutable.update { it.copy(shelvesBusy = true, operationError = null) }
        mutate {
            val result = if (favorite) repository.setFavorite(bookId, selected) else repository.setWantToRead(bookId, selected)
            when (result) {
                is BookResult.Success -> mutable.update { it.copy(shelvesBusy = false, book = it.book?.copy(shelves = result.value), notice = if (favorite) { if (selected) BookNotice.FavoriteAdded else BookNotice.FavoriteRemoved } else { if (selected) BookNotice.Planned else BookNotice.Unplanned }, noticeVersion = it.noticeVersion + 1) }
                is BookResult.Failure -> mutable.update { it.copy(shelvesBusy = false, operationError = result.error) }
            }
        }
    }
    private fun loadReviews(reset: Boolean) {
        reviewsJob?.cancel()
        val query = mutable.value.reviews.query
        val cursor = if (reset) null else mutable.value.reviews.cursor
        mutable.update { it.copy(reviews = it.reviews.copy(loading = reset && !it.reviews.loaded, refreshing = reset && it.reviews.loaded, loadingMore = !reset, failure = null)) }
        reviewsJob = viewModelScope.launch {
            when (val result = repository.getReviews(bookId, query, cursor)) {
                is BookResult.Success -> mutable.update { it.copy(reviews = it.reviews.copy(items = (if (reset) result.value.items else it.reviews.items + result.value.items).distinctBy(BookReview::id), cursor = result.value.nextCursor,
                    totalCount = result.value.totalCount, loaded = true, loading = false, refreshing = false, loadingMore = false)) }
                is BookResult.Failure -> mutable.update { it.copy(reviews = it.reviews.copy(failure = result.error, loading = false, refreshing = false, loadingMore = false)) }
            }
        }
    }
    private fun saveReview() {
        val draft = mutable.value.reviewDraft ?: return
        val input = ReviewInput(draft.rating, draft.text)
        BookInputRules.review(input)?.let { error -> mutable.update { it.copy(reviewDraft = draft.copy(fieldError = error)) }; return }
        cancelStaleReads()
        mutable.update { it.copy(reviewDraft = draft.copy(saving = true, fieldError = null, requestError = null)) }
        mutate {
            when (val result = repository.saveReview(bookId, input)) {
                is BookResult.Success -> {
                    updateReview(result.value.id) { result.value }
                    mutable.update { it.copy(reviewDraft = null, book = it.book?.copy(myReview = result.value), notice = BookNotice.ReviewSaved, noticeVersion = it.noticeVersion + 1) }
                    loadBook()
                    if (mutable.value.reviews.loaded) loadReviews(reset = true)
                }
                is BookResult.Failure -> mutable.update { it.copy(reviewDraft = draft.copy(requestError = result.error)) }
            }
        }
    }
    private fun deleteReview() {
        val id = mutable.value.book?.myReview?.id ?: return
        cancelStaleReads()
        mutable.update { it.copy(deletingReview = true, operationError = null) }
        mutate {
            when (val result = repository.deleteReview(bookId)) {
                is BookResult.Success -> {
                    val closesThread = mutable.value.thread?.review?.id == id
                    if (closesThread) repliesJob?.cancel()
                    mutable.update { it.copy(deletingReview = false, reviewDeleteOpen = false,
                        page = if (closesThread && it.page == BookPage.Thread) it.threadReturnPage else it.page,
                        thread = if (closesThread) null else it.thread,
                        book = it.book?.copy(myReview = null, previewReviews = it.book.previewReviews.filter { item -> item.id != id }),
                        reviews = it.reviews.copy(items = it.reviews.items.filter { item -> item.id != id }), notice = BookNotice.ReviewDeleted, noticeVersion = it.noticeVersion + 1) }
                    loadBook(); if (mutable.value.reviews.loaded) loadReviews(reset = true)
                }
                is BookResult.Failure -> mutable.update { it.copy(deletingReview = false, operationError = result.error) }
            }
        }
    }
    private fun findReview(id: String): BookReview? = mutable.value.let { s ->
        s.reviews.items.firstOrNull { it.id == id } ?: s.book?.previewReviews?.firstOrNull { it.id == id } ?: s.book?.myReview?.takeIf { it.id == id } ?: s.thread?.review?.takeIf { it.id == id }
    }
    private fun updateReview(id: String, transform: (BookReview) -> BookReview) = mutable.update { s ->
        s.copy(book = s.book?.copy(myReview = s.book.myReview?.let { if (it.id == id) transform(it) else it }, previewReviews = s.book.previewReviews.map { if (it.id == id) transform(it) else it }),
            reviews = s.reviews.copy(items = s.reviews.items.map { if (it.id == id) transform(it) else it }), thread = s.thread?.let { if (it.review.id == id) it.copy(review = transform(it.review)) else it })
    }
    private fun react(id: String, reaction: ReviewReaction) {
        val review = findReview(id)?.takeUnless { it.isMine || it.id in mutable.value.votingIds } ?: return
        cancelStaleReads()
        mutable.update { it.copy(votingIds = it.votingIds + id, operationError = null) }
        mutate {
            when (val result = repository.react(bookId, id, reaction.takeUnless { it == review.reaction })) {
                is BookResult.Success -> updateReview(id) { it.copy(likes = result.value.likes, dislikes = result.value.dislikes, reaction = result.value.reaction) }
                is BookResult.Failure -> mutable.update { it.copy(operationError = result.error) }
            }
            mutable.update { it.copy(votingIds = it.votingIds - id) }
        }
    }
    private fun openThread(id: String) {
        val review = findReview(id) ?: return
        if (mutable.value.thread?.review?.id == id) {
            mutable.update { it.copy(threadReturnPage = if (it.page == BookPage.Thread) it.threadReturnPage else it.page, page = BookPage.Thread) }
            return
        }
        repliesJob?.cancel()
        mutable.update { it.copy(threadReturnPage = it.page, page = BookPage.Thread, thread = ReviewThreadState(review)) }
        loadReplies(reset = true)
    }
    private fun loadReplies(reset: Boolean) {
        val thread = mutable.value.thread ?: return
        repliesJob?.cancel()
        val cursor = if (reset) null else thread.cursor
        mutable.update { it.copy(thread = it.thread?.copy(loading = reset && !it.thread.loaded, loadingMore = !reset, failure = null)) }
        repliesJob = viewModelScope.launch {
            when (val result = repository.getReplies(bookId, thread.review.id, cursor)) {
                is BookResult.Success -> mutable.update { s -> if (s.thread?.review?.id != thread.review.id) s else s.copy(thread = s.thread.copy(items = (if (reset) result.value.items else s.thread.items + result.value.items).distinctBy(ReviewReply::id), totalCount = result.value.totalCount, cursor = result.value.nextCursor, loaded = true, loading = false, loadingMore = false)) }
                is BookResult.Failure -> mutable.update { s -> if (s.thread?.review?.id != thread.review.id) s else s.copy(thread = s.thread.copy(failure = result.error, loading = false, loadingMore = false)) }
            }
        }
    }
    private fun submitReply() {
        val thread = mutable.value.thread ?: return
        BookInputRules.reply(thread.draft)?.let { error -> mutable.update { it.copy(thread = thread.copy(fieldError = error)) }; return }
        if (thread.loading) return
        repliesJob?.cancel()
        val key = thread.requestKey ?: UUID.randomUUID().toString()
        mutable.update { it.copy(thread = thread.copy(posting = true, loadingMore = false, requestKey = key, fieldError = null, postError = null)) }
        mutate {
            when (val result = repository.reply(bookId, thread.review.id, thread.draft, key)) {
                is BookResult.Success -> {
                    val exists = mutable.value.thread?.items?.any { it.id == result.value.id } == true
                    mutable.update { it.copy(thread = it.thread?.copy(items = (listOf(result.value) + it.thread.items).distinctBy(ReviewReply::id), totalCount = it.thread.totalCount + if (exists) 0 else 1,
                        draft = "", posting = false, loaded = true, requestKey = null), notice = BookNotice.ReplySent, noticeVersion = it.noticeVersion + 1) }
                    if (!exists) updateReview(thread.review.id) { it.copy(replyCount = it.replyCount + 1) }
                    loadReplies(reset = true)
                }
                is BookResult.Failure -> mutable.update { it.copy(thread = it.thread?.copy(posting = false, postError = result.error)) }
            }
        }
    }
    private fun deleteReply() {
        val thread = mutable.value.thread ?: return
        val replyId = thread.deleteId ?: return
        repliesJob?.cancel()
        mutable.update { it.copy(thread = thread.copy(deleting = true, loadingMore = false, postError = null)) }
        mutate {
            when (val result = repository.deleteReply(bookId, thread.review.id, replyId)) {
                is BookResult.Success -> {
                    mutable.update { it.copy(thread = it.thread?.copy(items = it.thread.items.filter { reply -> reply.id != replyId }, totalCount = (it.thread.totalCount - 1).coerceAtLeast(0), deleting = false, deleteId = null), notice = BookNotice.ReplyDeleted, noticeVersion = it.noticeVersion + 1) }
                    updateReview(thread.review.id) { it.copy(replyCount = (it.replyCount - 1).coerceAtLeast(0)) }
                }
                is BookResult.Failure -> mutable.update { it.copy(thread = it.thread?.copy(deleting = false, postError = result.error)) }
            }
        }
    }
    private fun loadCollections() {
        if (mutable.value.collections == null) return
        collectionsJob?.cancel()
        mutable.update { it.copy(collections = it.collections?.copy(loading = true, failure = null)) }
        collectionsJob = viewModelScope.launch {
            when (val result = repository.getCollections(bookId)) {
                is BookResult.Success -> mutable.update { it.copy(collections = it.collections?.copy(items = result.value, selected = result.value.filter(BookCollectionChoice::selected).map(BookCollectionChoice::id).toSet(), loading = false)) }
                is BookResult.Failure -> mutable.update { it.copy(collections = it.collections?.copy(loading = false, failure = result.error)) }
            }
        }
    }
    private fun saveCollections() {
        val picker = mutable.value.collections?.takeUnless { it.loading } ?: return
        cancelStaleReads()
        mutable.update { it.copy(collections = picker.copy(saving = true, failure = null)) }
        mutate {
            when (val result = repository.saveCollections(bookId, picker.selected)) {
                is BookResult.Success -> mutable.update { it.copy(collections = null, book = it.book?.copy(shelves = result.value), notice = BookNotice.CollectionsSaved, noticeVersion = it.noticeVersion + 1) }
                is BookResult.Failure -> mutable.update { it.copy(collections = it.collections?.copy(saving = false, failure = result.error)) }
            }
        }
    }
}
