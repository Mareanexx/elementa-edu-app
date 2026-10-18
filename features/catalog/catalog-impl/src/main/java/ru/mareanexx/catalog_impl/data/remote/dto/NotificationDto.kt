package ru.mareanexx.catalog_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class NotificationDto(
    val id: String? = null,
    val sequence: Long? = null,
    val title: String? = null,
    val body: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("read_at") val readAt: String? = null,
    @SerializedName("book_id") val bookId: String? = null,
    @SerializedName("category_id") val categoryId: String? = null,
)
