package ru.mareanexx.elementa.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.mareanexx.auth_api.domain.repository.AuthRepository
import ru.mareanexx.elementa.presentation.session.model.SessionState
import ru.mareanexx.elementa.presentation.session.model.SessionIntent

class SessionViewModel(
    private val repository: AuthRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow<SessionState>(SessionState.Restoring)
    val state = mutableState.asStateFlow()
    private var observation: Job? = null

    init { restore() }

    fun accept(intent: SessionIntent) {
        when (intent) {
            SessionIntent.RetryRestore -> restore()
            SessionIntent.SignOut -> signOut()
        }
    }

    private fun restore() {
        observation?.cancel()
        mutableState.value = SessionState.Restoring
        observation = viewModelScope.launch {
            try {
                repository.session.collectLatest { session ->
                    if (session == null) mutableState.value = SessionState.Guest
                    else {
                        mutableState.value = SessionState.SignedIn(session.user)
                        delay((session.expiresAtMillis - System.currentTimeMillis()).coerceAtLeast(0))
                        repository.clearSession()
                    }
                }
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (_: Exception) { mutableState.value = SessionState.RestoreFailed }
        }
    }

    private fun signOut() {
        val signedIn = mutableState.value as? SessionState.SignedIn ?: return
        if (signedIn.signingOut) return
        mutableState.value = signedIn.copy(signingOut = true, signOutFailed = false)
        viewModelScope.launch {
            try { repository.clearSession()
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (_: Exception) {
                mutableState.update { if (it is SessionState.SignedIn) it.copy(signingOut = false, signOutFailed = true) else it }
            }
        }
    }
}
