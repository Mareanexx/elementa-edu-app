package ru.mareanexx.catalog_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class UnreadDto(
    val count: Int? = null,
    val revision: Long? = null,
)
