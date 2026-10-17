package ru.mareanexx.user_profile_impl.data.remote.mapper

import ru.mareanexx.user_profile_api.domain.model.*
import ru.mareanexx.user_profile_impl.data.remote.dto.ProfileDto

internal object ProfileMapper {
    fun map(dto: ProfileDto): UserProfile {
        val stats = requireNotNull(dto.statistics)
        val id = requireNotNull(dto.id).also { require(it.isNotBlank()) }
        val name = requireNotNull(dto.fullName).also { require(it.isNotBlank()) }
        val username = requireNotNull(dto.username).also { require(it.isNotBlank()) }
        return UserProfile(
            id = id,
            fullName = name,
            username = username,
            description = requireNotNull(dto.description),
            avatarUrl = dto.avatarUrl?.takeIf { it.isNotBlank() },
            statistics = ProfileStatistics(
                requireNotNull(stats.booksRead).also { require(it >= 0) },
                requireNotNull(stats.notesPublished).also { require(it >= 0) },
                requireNotNull(stats.testsPassed).also { require(it >= 0) },
            ),
        )
    }
}
