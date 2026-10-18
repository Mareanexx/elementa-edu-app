package ru.mareanexx.user_library_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class LibraryPreferencesDto(
    @SerializedName("sort_order") val sort: String?,
)
