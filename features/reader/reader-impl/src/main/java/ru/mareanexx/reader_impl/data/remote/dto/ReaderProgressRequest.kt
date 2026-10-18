package ru.mareanexx.reader_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class ReaderProgressRequest(
    val position: ReaderPositionDto,
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("client_revision") val clientRevision: Long,
)
