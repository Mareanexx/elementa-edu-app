package ru.mareanexx.reader_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class ReaderTagDto(
    val id: String? = null,
    val label: String? = null,
    val kind: String? = null,
)
