package ru.mareanexx.user_profile_impl.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.mareanexx.user_profile_api.domain.model.*
import ru.mareanexx.user_profile_api.domain.repository.ProfileRepository
import ru.mareanexx.user_profile_api.domain.usecase.UpdateAvatarUseCase
import ru.mareanexx.user_profile_api.domain.usecase.UpdateProfileUseCase
import ru.mareanexx.user_profile_api.domain.validation.ProfileValidator

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val updateProfile: UpdateProfileUseCase,
    private val updateAvatar: UpdateAvatarUseCase,
) : ViewModel() {
    private val mutable = MutableStateFlow(ProfileUiState())
    val state = mutable.asStateFlow()
    private val events = Channel<ProfileEffect>(Channel.BUFFERED)
    val effects = events.receiveAsFlow()

    init { load() }

    fun accept(intent: ProfileIntent) {
        val current = mutable.value
        if (current.saving || current.avatarBusy) return
        when (intent) {
            ProfileIntent.Reload -> if (!current.loading) load()
            ProfileIntent.BeginEdit -> current.profile?.let { profile ->
                mutable.update { it.copy(form = ProfileEditForm(profile.fullName, profile.username, profile.description), errors = emptyMap(), touched = emptySet(), failure = null) }
            }
            is ProfileIntent.ChangeField -> mutable.update { ProfileReducer.change(it, intent.field, intent.value) }
            is ProfileIntent.BlurField -> mutable.update { state ->
                val error = ProfileValidator.validate(state.form)[intent.field] ?: state.errors[intent.field]
                state.copy(touched = state.touched + intent.field, errors = state.errors - intent.field + if (error == null) emptyMap() else mapOf(intent.field to error))
            }
            ProfileIntent.Save -> save()
            is ProfileIntent.UploadAvatar -> avatar { updateAvatar(intent.image) }
            ProfileIntent.RemoveAvatar -> avatar { repository.removeAvatar() }
            ProfileIntent.DismissError -> mutable.update { it.copy(failure = null) }
        }
    }

    private fun load() {
        mutable.update { it.copy(loading = true, failure = null) }
        viewModelScope.launch {
            when (val result = repository.getProfile()) {
                is ProfileResult.Success -> mutable.update { it.copy(profile = result.value, loading = false) }
                is ProfileResult.Failure -> mutable.update { it.copy(failure = result.reason, loading = false) }
            }
        }
    }

    private fun save() {
        val form = mutable.value.form
        val errors = ProfileValidator.validate(form)
        mutable.update { it.copy(errors = errors, touched = ProfileField.entries.toSet(), failure = null) }
        if (errors.isNotEmpty()) return
        mutable.update { it.copy(saving = true) }
        viewModelScope.launch {
            when (val result = updateProfile(form)) {
                is ProfileResult.Success -> {
                    val user = result.value
                    mutable.update { it.copy(profile = user, form = ProfileEditForm(user.fullName, user.username, user.description), saving = false) }
                    events.send(ProfileEffect.Saved)
                }
                is ProfileResult.Failure -> mutable.update { it.copy(saving = false, failure = result.reason, errors = result.reason.fields) }
            }
        }
    }

    private fun avatar(request: suspend () -> ProfileResult<UserProfile>) {
        mutable.update { it.copy(avatarBusy = true, failure = null) }
        viewModelScope.launch {
            when (val result = request()) {
                is ProfileResult.Success -> {
                    mutable.update { it.copy(profile = result.value, avatarBusy = false) }
                    events.send(ProfileEffect.AvatarUpdated)
                }
                is ProfileResult.Failure -> mutable.update { it.copy(avatarBusy = false, failure = result.reason) }
            }
        }
    }
}
