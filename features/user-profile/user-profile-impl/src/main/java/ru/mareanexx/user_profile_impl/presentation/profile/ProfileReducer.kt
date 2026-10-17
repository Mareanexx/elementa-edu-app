package ru.mareanexx.user_profile_impl.presentation.profile

import ru.mareanexx.user_profile_api.domain.model.ProfileField
import ru.mareanexx.user_profile_api.domain.validation.ProfileValidator

internal object ProfileReducer {
    fun change(state: ProfileUiState, field: ProfileField, value: String): ProfileUiState {
        val form = when (field) {
            ProfileField.FullName -> state.form.copy(fullName = value)
            ProfileField.Username -> state.form.copy(username = value)
            ProfileField.Description -> state.form.copy(description = value.take(ProfileValidator.MAX_DESCRIPTION_LENGTH).let {
                // При обрезке длинного текста не разрываем суррогатную пару.
                if (it.lastOrNull()?.isHighSurrogate() == true) it.dropLast(1) else it
            })
        }
        val validation = ProfileValidator.validate(form)
        val errors = state.errors - field + if (field in state.touched && validation[field] != null) {
            mapOf(field to requireNotNull(validation[field]))
        } else emptyMap()
        return state.copy(form = form, errors = errors, failure = null)
    }
}
