package ru.mareanexx.book_api.domain.model

enum class BookError {
    Offline, Timeout, Unauthorized, NotFound, Unavailable, InvalidResponse, RateLimited, Forbidden, Conflict, RatingRequired, ReviewTooLong, ReplyRequired, ReplyTooLong, CollectionNotFound, Storage,
}
