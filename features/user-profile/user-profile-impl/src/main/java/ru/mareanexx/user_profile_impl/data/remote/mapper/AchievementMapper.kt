package ru.mareanexx.user_profile_impl.data.remote.mapper

import java.time.LocalDate
import ru.mareanexx.user_profile_api.domain.model.*
import ru.mareanexx.user_profile_impl.data.remote.dto.AchievementDto

internal object AchievementMapper {
    fun map(dto: AchievementDto): Achievement {
        val current = requireNotNull(dto.current).also { require(it >= 0) }
        val target = requireNotNull(dto.target).also { require(it > 0) }
        val date = dto.unlockedAt?.also { LocalDate.parse(it); require(current >= target) }
        return Achievement(
            id = requireNotNull(dto.id).also { require(it.isNotBlank()) },
            title = requireNotNull(dto.title),
            description = requireNotNull(dto.description),
            category = when (dto.category) {
                "reading" -> AchievementCategory.Reading
                "notes" -> AchievementCategory.Notes
                "tests" -> AchievementCategory.Tests
                "habits" -> AchievementCategory.Habits
                else -> error("Неизвестная категория достижения")
            },
            art = when (dto.art) {
                "red" -> AchievementArt.Red
                "blue" -> AchievementArt.Blue
                "orange" -> AchievementArt.Orange
                "pink" -> AchievementArt.Pink
                else -> AchievementArt.Blue
            },
            current = current,
            target = target,
            unit = requireNotNull(dto.unit),
            unlockedAt = date,
        )
    }
}
