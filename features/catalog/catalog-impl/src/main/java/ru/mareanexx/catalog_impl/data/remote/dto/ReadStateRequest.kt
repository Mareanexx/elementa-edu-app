package ru.mareanexx.catalog_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class ReadStateRequest(
    @SerializedName("through_sequence") val throughSequence: Long,
)
