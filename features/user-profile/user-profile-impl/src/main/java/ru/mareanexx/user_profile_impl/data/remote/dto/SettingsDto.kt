package ru.mareanexx.user_profile_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class SettingsDto(
    @SerializedName("text_scale") val textScale: Float?,
    @SerializedName("reduce_motion") val reduceMotion: Boolean?,
    @SerializedName("keep_screen_on") val keepScreenOn: Boolean?,
    @SerializedName("haptic_feedback") val hapticFeedback: Boolean?,
)
