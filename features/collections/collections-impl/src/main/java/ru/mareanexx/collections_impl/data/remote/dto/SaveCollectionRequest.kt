package ru.mareanexx.collections_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SaveCollectionRequest(
    val name: String?,
    @SerializedName("cover_key") val coverKey: String?,
    @SerializedName("cover_image_id") val imageId: String? = null,
)
