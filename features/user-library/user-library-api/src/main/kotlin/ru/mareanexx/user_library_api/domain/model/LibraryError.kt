package ru.mareanexx.user_library_api.domain.model

enum class LibraryError { Offline, Timeout, Unauthorized, NotFound, Unavailable, RateLimited, InvalidResponse, Conflict, Storage }
