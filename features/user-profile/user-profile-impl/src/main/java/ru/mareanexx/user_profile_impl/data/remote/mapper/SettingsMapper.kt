package ru.mareanexx.user_profile_impl.data.remote.mapper

import ru.mareanexx.user_profile_api.domain.model.AppSettings
import ru.mareanexx.user_profile_impl.data.remote.dto.SettingsDto

internal object SettingsMapper {
    fun map(dto: SettingsDto): AppSettings = AppSettings(
        textScale = requireNotNull(dto.textScale).also { require(it.isFinite() && it in 0.9f..1.3f) },
        reduceMotion = requireNotNull(dto.reduceMotion),
        keepScreenOn = requireNotNull(dto.keepScreenOn),
        hapticFeedback = requireNotNull(dto.hapticFeedback),
    )

    fun toDto(settings: AppSettings) = SettingsDto(
        settings.textScale, settings.reduceMotion, settings.keepScreenOn, settings.hapticFeedback,
    )
}
