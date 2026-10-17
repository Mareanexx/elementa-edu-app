package ru.mareanexx.collections_api.domain.model

enum class CollectionsError {
    InvalidImage,
    NameRequired, NameTooLong, NameTaken, LimitReached, NotFound, Protected,
    Offline, Timeout, Unauthorized, Unavailable, InvalidResponse, Storage, RateLimited, InvalidBooks,
}
