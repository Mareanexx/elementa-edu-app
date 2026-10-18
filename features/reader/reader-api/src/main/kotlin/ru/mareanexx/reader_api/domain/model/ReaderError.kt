package ru.mareanexx.reader_api.domain.model

enum class ReaderError { Offline, Timeout, Unauthorized, Forbidden, NotFound, InvalidResponse, Unavailable, RateLimited, UnsupportedFormat, CorruptBook, TooLarge, Storage, OutOfSpace, Cancelled, InvalidAnnotation }
