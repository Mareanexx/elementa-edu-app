package ru.mareanexx.user_profile_api

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import ru.mareanexx.user_profile_api.domain.model.*
import ru.mareanexx.user_profile_api.domain.repository.ProfileRepository
import ru.mareanexx.user_profile_api.domain.usecase.*
import ru.mareanexx.user_profile_api.domain.validation.ProfileValidator

class ProfileRulesTest {
    private val valid = ProfileEditForm("Мария Химик", "chemist", "Люблю читать")

    @Test fun `проверка всех полей не допускает неверные данные`() {
        val errors = ProfileValidator.validate(ProfileEditForm(" ", "1нельзя", "x".repeat(251)))
        assertEquals(ProfileField.entries.toSet(), errors.keys)
        assertTrue(ProfileValidator.validate(valid).isEmpty())
    }

    @Test fun `описание может быть пустым и содержать переносы строк`() {
        assertTrue(ProfileValidator.validate(valid.copy(description = "")).isEmpty())
        assertTrue(ProfileValidator.validate(valid.copy(description = "Читаю\nИзучаю химию")).isEmpty())
    }

    @Test fun `описание принимает 250 символов и отклоняет 251`() {
        assertTrue(ProfileValidator.validate(valid.copy(description = "я".repeat(250))).isEmpty())
        assertEquals(ProfileError.DescriptionTooLong, ProfileValidator.validate(valid.copy(description = "я".repeat(251)))[ProfileField.Description])
    }

    @Test fun `сценарий нормализует поля до вызова репозитория`() = runTest {
        val repository = RecordingRepository()
        UpdateProfileUseCase(repository)(valid.copy(fullName = " Мария Химик ", username = " CHEMIST ", description = " Привет "))
        assertEquals(ProfileEditForm("Мария Химик", "chemist", "Привет"), repository.lastForm)
    }

    @Test fun `невалидная форма не вызывает репозиторий`() = runTest {
        val repository = RecordingRepository()
        val result = UpdateProfileUseCase(repository)(valid.copy(username = "абв")) as ProfileResult.Failure
        assertEquals(ProfileError.InvalidField, result.reason.error)
        assertNull(repository.lastForm)
    }

    @Test fun `аватар ограничен типом и размером до отправки`() = runTest {
        val repository = RecordingRepository()
        val upload = UpdateAvatarUseCase(repository)
        assertTrue(upload(AvatarUpload(byteArrayOf())) is ProfileResult.Failure)
        assertTrue(upload(AvatarUpload(byteArrayOf(1), mimeType = "application/pdf")) is ProfileResult.Failure)
        assertEquals(ProfileError.TooLarge, (upload(AvatarUpload(ByteArray(5 * 1024 * 1024 + 1))) as ProfileResult.Failure).reason.error)
        assertEquals(0, repository.uploads)
        upload(AvatarUpload(byteArrayOf(1)))
        assertEquals(1, repository.uploads)
    }

    @Test fun `прогресс достижения ограничен и остаток не бывает отрицательным`() {
        val item = Achievement("1", "Книга", "Прочитай книгу", AchievementCategory.Reading, AchievementArt.Blue, 25, 20, "книг", "2026-10-12")
        assertEquals(1f, item.progress)
        assertEquals(0, item.remaining)
        assertTrue(item.isUnlocked)
        assertFalse(item.copy(current = 3, unlockedAt = null).isUnlocked)
    }

    @Test fun `обрезка не позволяет открыть пустой фон у широкого фото`() {
        val crop = AvatarCrop(1f, 10f, 10f).clamp(1200, 600)
        assertEquals(0.5f, crop.offsetX, 0.0001f)
        assertEquals(0f, crop.offsetY, 0.0001f)
    }

    @Test fun `обрезка ограничивает смещение с учётом масштаба портретного фото`() {
        val crop = AvatarCrop(2f, -10f, -10f).clamp(600, 1200)
        assertEquals(-0.5f, crop.offsetX, 0.0001f)
        assertEquals(-1.5f, crop.offsetY, 0.0001f)
    }

    private class RecordingRepository : ProfileRepository {
        override val settings = MutableStateFlow(AppSettings())
        var lastForm: ProfileEditForm? = null
        var uploads = 0
        private val profile = UserProfile("1", "Мария", "chemist", "", null, ProfileStatistics(0, 0, 0))
        override suspend fun getProfile() = ProfileResult.Success(profile)
        override suspend fun updateProfile(form: ProfileEditForm): ProfileResult<UserProfile> { lastForm = form; return ProfileResult.Success(profile) }
        override suspend fun updateAvatar(image: AvatarUpload): ProfileResult<UserProfile> { uploads++; return ProfileResult.Success(profile) }
        override suspend fun removeAvatar() = ProfileResult.Success(profile)
        override suspend fun getAchievements() = ProfileResult.Success(emptyList<Achievement>())
        override suspend fun getSettings() = ProfileResult.Success(settings.value)
        override suspend fun updateSettings(settings: AppSettings) = ProfileResult.Success(settings)
        override suspend fun clearImageCache() = ProfileResult.Success(Unit)
    }
}
