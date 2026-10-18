package ru.mareanexx.catalog_api.domain.model

enum class CatalogError { Offline, Timeout, Unauthorized, NotFound, Conflict, RateLimited, Unavailable, InvalidResponse, Storage }
