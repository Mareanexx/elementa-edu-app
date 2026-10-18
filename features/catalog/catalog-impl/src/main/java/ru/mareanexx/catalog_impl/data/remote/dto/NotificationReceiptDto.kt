package ru.mareanexx.catalog_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class NotificationReceiptDto(
    @SerializedName("read_at") val readAt: String? = null,
    val unread: UnreadDto? = null,
)
