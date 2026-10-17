package ru.mareanexx.user_profile_impl

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Before
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import ru.mareanexx.user_profile_api.domain.model.*
import ru.mareanexx.user_profile_api.domain.usecase.*
import ru.mareanexx.user_profile_impl.presentation.profile.*
import ru.mareanexx.user_profile_impl.presentation.settings.*

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun cleanup() { Dispatchers.resetMain() }
    private fun model(repo: FakeProfileRepository) = ProfileViewModel(repo, UpdateProfileUseCase(repo), UpdateAvatarUseCase(repo))

    @Test fun `повторное сохранение и редактирование блокируются до ответа`() = runTest(dispatcher) {
        val repo = FakeProfileRepository().apply { gate = CompletableDeferred() }
        val model = model(repo)
        runCurrent()
        model.accept(ProfileIntent.BeginEdit)
        model.accept(ProfileIntent.ChangeField(ProfileField.FullName, "Новая Мария"))
        model.accept(ProfileIntent.Save)
        model.accept(ProfileIntent.Save)
        model.accept(ProfileIntent.ChangeField(ProfileField.FullName, "Не должно примениться"))
        runCurrent()
        assertEquals(1, repo.profileCalls)
        assertTrue(model.state.value.saving)
        assertEquals("Новая Мария", model.state.value.form.fullName)
        repo.gate!!.complete(Unit)
        runCurrent()
        assertEquals("Новая Мария", model.state.value.profile!!.fullName)
        assertFalse(model.state.value.dirty)
    }

    @Test fun `ошибка занятого логина остаётся до его редактирования`() = runTest(dispatcher) {
        val repo = FakeProfileRepository().apply { failure = ProfileFailure(ProfileError.UsernameTaken, mapOf(ProfileField.Username to ProfileError.UsernameTaken)) }
        val model = model(repo)
        runCurrent()
        model.accept(ProfileIntent.BeginEdit)
        model.accept(ProfileIntent.Save)
        runCurrent()
        model.accept(ProfileIntent.BlurField(ProfileField.Username))
        assertEquals(ProfileError.UsernameTaken, model.state.value.errors[ProfileField.Username])
        model.accept(ProfileIntent.ChangeField(ProfileField.Username, "new_chemist"))
        assertNull(model.state.value.errors[ProfileField.Username])
        assertNull(model.state.value.failure)
    }

    @Test fun `вставка описания ограничена 250 символами и сохраняется`() = runTest(dispatcher) {
        val repo = FakeProfileRepository()
        val model = model(repo)
        runCurrent()
        model.accept(ProfileIntent.BeginEdit)
        model.accept(ProfileIntent.ChangeField(ProfileField.Description, "я".repeat(300)))
        assertEquals("я".repeat(250), model.state.value.form.description)
        model.accept(ProfileIntent.Save)
        runCurrent()
        assertEquals("я".repeat(250), model.state.value.profile!!.description)
        assertNull(model.state.value.failure)
    }

    @Test fun `ограничение описания сохраняет целые эмодзи на границе`() = runTest(dispatcher) {
        val model = model(FakeProfileRepository())
        runCurrent()
        model.accept(ProfileIntent.BeginEdit)
        model.accept(ProfileIntent.ChangeField(ProfileField.Description, "я".repeat(249) + "🧪"))
        assertEquals("я".repeat(249), model.state.value.form.description)
        model.accept(ProfileIntent.ChangeField(ProfileField.Description, "я".repeat(248) + "🧪"))
        assertEquals("я".repeat(248) + "🧪", model.state.value.form.description)
    }

    @Test fun `загрузка аватара блокирует повторную загрузку и удаление`() = runTest(dispatcher) {
        val repo = FakeProfileRepository().apply { gate = CompletableDeferred() }
        val model = model(repo)
        runCurrent()
        model.accept(ProfileIntent.UploadAvatar(AvatarUpload(byteArrayOf(1))))
        model.accept(ProfileIntent.UploadAvatar(AvatarUpload(byteArrayOf(2))))
        model.accept(ProfileIntent.RemoveAvatar)
        runCurrent()
        assertEquals(1, repo.avatarCalls)
        assertTrue(model.state.value.avatarBusy)
        repo.gate!!.complete(Unit)
        runCurrent()
        assertFalse(model.state.value.avatarBusy)
        assertNotNull(model.state.value.profile!!.avatarUrl)
    }

    @Test fun `ошибка сервера не заменяет сохранённые настройки`() = runTest(dispatcher) {
        val repo = FakeProfileRepository().apply { failure = ProfileFailure(ProfileError.Offline) }
        val model = SettingsViewModel(repo)
        runCurrent()
        model.accept(SettingsIntent.Update(AppSettings(keepScreenOn = true)))
        runCurrent()
        assertFalse(model.state.value.settings.keepScreenOn)
        assertEquals(ProfileError.Offline, model.state.value.failure!!.error)
        assertFalse(model.state.value.saving)
    }

    @Test fun `настройки сохраняются единожды и поступают в наблюдаемое состояние`() = runTest(dispatcher) {
        val repo = FakeProfileRepository().apply { gate = CompletableDeferred() }
        val model = SettingsViewModel(repo)
        runCurrent()
        val next = AppSettings(textScale = 1.2f, reduceMotion = true, keepScreenOn = true, hapticFeedback = false)
        model.accept(SettingsIntent.Update(next))
        model.accept(SettingsIntent.Update(AppSettings()))
        runCurrent()
        assertEquals(1, repo.settingsCalls)
        repo.gate!!.complete(Unit)
        runCurrent()
        assertEquals(next, model.state.value.settings)
        assertEquals(next, repo.settings.value)
    }
}
