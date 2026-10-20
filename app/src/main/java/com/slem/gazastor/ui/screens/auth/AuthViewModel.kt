package com.slem.gazastor.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slem.gazastor.core.di.AppModule
import com.slem.gazastor.data.preferences.AuthPreferences
import com.slem.gazastor.data.repository.AuthRepository
import com.slem.gazastor.data.repository.SignUpOutcome
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

/** Which form the user is looking at. */
enum class AuthMode { SignIn, SignUp }

/** Failures surfaced to the user, kept as an enum so they stay testable on the JVM. */
enum class AuthError {
    InvalidEmail,
    PasswordTooShort,
    PasswordMismatch,
    InvalidCredentials,
    EmailNotConfirmed,
    EmailAlreadyRegistered,
    WeakPassword,
    Network,
    Unknown
}

/** Inline, non-blocking notices (e.g. "check your email"). */
enum class AuthInfo { ConfirmationEmailSent }

/** One-shot signals consumed by the screen and never replayed. */
sealed interface AuthEvent {
    data object Authenticated : AuthEvent
    data object GuestSelected : AuthEvent
}

data class AuthUiState(
    val mode: AuthMode = AuthMode.SignIn,
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: AuthError? = null,
    val info: AuthInfo? = null
)

private val EMAIL_PATTERN = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

/**
 * Drives email/password sign-in and sign-up, plus the "continue as a guest"
 * escape hatch. Choosing guest marks the prompt as seen so it never blocks the
 * app again.
 */
class AuthViewModel(
    private val authRepository: AuthRepository = AppModule.authRepository,
    private val authPreferences: AuthPreferences = AppModule.userPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events: Flow<AuthEvent> = _events.receiveAsFlow()

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, error = null, info = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, error = null, info = null) }

    fun onConfirmPasswordChange(value: String) =
        _uiState.update { it.copy(confirmPassword = value, error = null, info = null) }

    fun onTogglePasswordVisibility() =
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

    fun onToggleConfirmPasswordVisibility() =
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }

    fun onModeChange(mode: AuthMode) =
        _uiState.update { it.copy(mode = mode, error = null, info = null) }

    fun submit() {
        val state = _uiState.value
        if (state.isSubmitting) return

        val validationError = validate(state)
        if (validationError != null) {
            _uiState.update { it.copy(error = validationError) }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, error = null, info = null) }
        viewModelScope.launch {
            when (state.mode) {
                AuthMode.SignIn -> handleSignIn(authRepository.signIn(state.email, state.password))
                AuthMode.SignUp -> handleSignUp(authRepository.signUp(state.email, state.password))
            }
        }
    }

    fun continueAsGuest() {
        viewModelScope.launch {
            authPreferences.setAuthPromptSeen()
            _events.send(AuthEvent.GuestSelected)
        }
    }

    private suspend fun handleSignIn(result: Result<Unit>) {
        result.fold(
            onSuccess = { completeAuthentication() },
            onFailure = { failure -> showFailure(failure) }
        )
    }

    private suspend fun handleSignUp(result: Result<SignUpOutcome>) {
        result.fold(
            onSuccess = { outcome ->
                when (outcome) {
                    SignUpOutcome.SignedIn -> completeAuthentication()
                    SignUpOutcome.ConfirmationRequired -> _uiState.update {
                        it.copy(isSubmitting = false, info = AuthInfo.ConfirmationEmailSent)
                    }
                }
            },
            onFailure = { failure -> showFailure(failure) }
        )
    }

    private suspend fun completeAuthentication() {
        authPreferences.setAuthPromptSeen()
        _uiState.update { it.copy(isSubmitting = false, password = "", confirmPassword = "") }
        _events.send(AuthEvent.Authenticated)
    }

    private fun showFailure(failure: Throwable) {
        _uiState.update { it.copy(isSubmitting = false, error = failure.toAuthError()) }
    }

    private fun validate(state: AuthUiState): AuthError? = when {
        !EMAIL_PATTERN.matches(state.email.trim()) -> AuthError.InvalidEmail
        state.password.length < MIN_PASSWORD_LENGTH -> AuthError.PasswordTooShort
        state.mode == AuthMode.SignUp && state.confirmPassword != state.password ->
            AuthError.PasswordMismatch
        else -> null
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }
}

private fun Throwable.toAuthError(): AuthError = when (this) {
    is AuthRestException -> authErrorForErrorCode(errorCode)
    // Network problems surface as IO failures, including ktor timeouts.
    is IOException -> AuthError.Network
    else -> AuthError.Unknown
}

/** Maps a Supabase auth error code to a message the UI can show. */
internal fun authErrorForErrorCode(code: AuthErrorCode?): AuthError = when (code) {
    AuthErrorCode.InvalidCredentials -> AuthError.InvalidCredentials
    AuthErrorCode.EmailNotConfirmed -> AuthError.EmailNotConfirmed
    AuthErrorCode.EmailExists, AuthErrorCode.UserAlreadyExists -> AuthError.EmailAlreadyRegistered
    AuthErrorCode.WeakPassword -> AuthError.WeakPassword
    else -> AuthError.Unknown
}
