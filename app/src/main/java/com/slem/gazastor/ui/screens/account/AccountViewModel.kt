package com.slem.gazastor.ui.screens.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slem.gazastor.core.di.AppModule
import com.slem.gazastor.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AccountUiState(
    val isSignedIn: Boolean = false,
    val email: String? = null,
    val displayName: String? = null,
    val role: String? = null,
    val isSigningOut: Boolean = false
)

/** Mirrors the auth session and loads the matching `profiles` row. */
class AccountViewModel(
    private val authRepository: AuthRepository = AppModule.authRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.sessionState.collect { user ->
                if (user == null) {
                    _uiState.value = AccountUiState()
                } else {
                    _uiState.update {
                        it.copy(isSignedIn = true, email = user.email, isSigningOut = false)
                    }
                    loadProfile()
                }
            }
        }
    }

    fun signOut() {
        if (_uiState.value.isSigningOut) return
        _uiState.update { it.copy(isSigningOut = true) }
        viewModelScope.launch {
            // A failed sign-out still ends in a usable state; the session flow
            // decides what the user sees next.
            authRepository.signOut()
            _uiState.update { it.copy(isSigningOut = false) }
        }
    }

    private suspend fun loadProfile() {
        authRepository.profile().onSuccess { profile ->
            _uiState.update {
                it.copy(
                    displayName = profile?.full_name?.takeIf { name -> name.isNotBlank() },
                    role = profile?.role
                )
            }
        }
    }
}
