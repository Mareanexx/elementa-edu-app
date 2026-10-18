package ru.mareanexx.catalog_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class NotificationPageDto(
    val items: List<NotificationDto>? = null,
    @SerializedName("next_cursor") val nextCursor: String? = null,
    @SerializedName("snapshot_sequence") val snapshotSequence: Long? = null,
    val unread: UnreadDto? = null,
)
