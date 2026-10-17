package ru.mareanexx.user_profile_impl.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.mareanexx.user_profile_api.domain.model.*
import ru.mareanexx.user_profile_api.domain.repository.ProfileRepository

class SettingsViewModel(
    private val repository: ProfileRepository,
) : ViewModel() {
    private val mutable = MutableStateFlow(SettingsUiState())
    val state = mutable.asStateFlow()

    init {
        viewModelScope.launch { repository.settings.collect { settings -> mutable.update { it.copy(settings = settings) } } }
        load()
    }

    fun accept(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.Reload -> if (!mutable.value.loading && !mutable.value.saving) load()
            is SettingsIntent.Update -> if (!mutable.value.loading && !mutable.value.saving) update(intent.settings)
            SettingsIntent.ClearImageCache -> if (!mutable.value.saving) clearCache()
            SettingsIntent.DismissNotice -> mutable.update { it.copy(notice = null) }
        }
    }

    private fun load() {
        mutable.update { it.copy(loading = true, failure = null) }
        viewModelScope.launch {
            when (val result = repository.getSettings()) {
                is ProfileResult.Success -> mutable.update { it.copy(settings = result.value, loading = false) }
                is ProfileResult.Failure -> mutable.update { it.copy(failure = result.reason, loading = false) }
            }
        }
    }

    private fun update(settings: AppSettings) {
        if (!settings.textScale.isFinite() || settings.textScale !in 0.9f..1.3f) return
        mutable.update { it.copy(saving = true, failure = null, notice = null) }
        viewModelScope.launch {
            when (val result = repository.updateSettings(settings)) {
                is ProfileResult.Success -> mutable.update { it.copy(settings = result.value, saving = false, notice = SettingsNotice.Saved) }
                is ProfileResult.Failure -> mutable.update { it.copy(failure = result.reason, saving = false) }
            }
        }
    }

    private fun clearCache() {
        mutable.update { it.copy(saving = true, failure = null, notice = null) }
        viewModelScope.launch {
            when (val result = repository.clearImageCache()) {
                is ProfileResult.Success -> mutable.update { it.copy(saving = false, notice = SettingsNotice.CacheCleared) }
                is ProfileResult.Failure -> mutable.update { it.copy(failure = result.reason, saving = false) }
            }
        }
    }
}
