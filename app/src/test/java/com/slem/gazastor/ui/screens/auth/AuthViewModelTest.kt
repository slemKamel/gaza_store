package com.slem.gazastor.ui.screens.auth

import com.slem.gazastor.data.repository.SignUpOutcome
import com.slem.gazastor.testing.FakeAuthPreferences
import com.slem.gazastor.testing.FakeAuthRepository
import com.slem.gazastor.testing.MainDispatcherRule
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.exceptions.HttpRequestException
import io.ktor.client.request.HttpRequestBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeAuthRepository()
    private val preferences = FakeAuthPreferences()

    private fun viewModel() = AuthViewModel(repository, preferences)

    @Test
    fun submit_withInvalidEmail_showsValidationErrorWithoutCallingRepository() = runTest {
        val viewModel = viewModel()
        viewModel.onEmailChange("not-an-email")
        viewModel.onPasswordChange("secret1")

        viewModel.submit()
        advanceUntilIdle()

        assertEquals(AuthError.InvalidEmail, viewModel.uiState.value.error)
        assertEquals(0, repository.signInCalls)
    }

    @Test
    fun submit_withShortPassword_showsValidationError() = runTest {
        val viewModel = viewModel()
        viewModel.onEmailChange("user@example.com")
        viewModel.onPasswordChange("123")

        viewModel.submit()
        advanceUntilIdle()

        assertEquals(AuthError.PasswordTooShort, viewModel.uiState.value.error)
        assertEquals(0, repository.signInCalls)
    }

    @Test
    fun signIn_success_emitsAuthenticatedAndMarksPromptSeen() = runTest {
        val viewModel = viewModel()
        val events = mutableListOf<AuthEvent>()
        val collector = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect { events.add(it) }
        }

        viewModel.onEmailChange("user@example.com")
        viewModel.onPasswordChange("secret1")
        viewModel.submit()
        advanceUntilIdle()

        assertEquals(listOf(AuthEvent.Authenticated), events)
        assertEquals(1, repository.signInCalls)
        assertTrue("auth prompt should be remembered", preferences.hasSeenAuthPrompt.first())
        assertFalse(viewModel.uiState.value.isSubmitting)
        collector.cancel()
    }

    @Test
    fun signIn_failure_showsMappedError() = runTest {
        repository.signInResult = Result.failure(IllegalStateException("boom"))
        val viewModel = viewModel()
        viewModel.onEmailChange("user@example.com")
        viewModel.onPasswordChange("secret1")

        viewModel.submit()
        advanceUntilIdle()

        assertEquals(AuthError.Unknown, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun signUp_requiringConfirmation_showsInfoInsteadOfNavigating() = runTest {
        repository.signUpResult = Result.success(SignUpOutcome.ConfirmationRequired)
        val viewModel = viewModel()
        viewModel.onModeChange(AuthMode.SignUp)
        viewModel.onEmailChange("new@example.com")
        viewModel.onPasswordChange("secret1")

        viewModel.submit()
        advanceUntilIdle()

        assertEquals(AuthInfo.ConfirmationEmailSent, viewModel.uiState.value.info)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun continueAsGuest_marksPromptSeenAndEmitsGuestEvent() = runTest {
        val viewModel = viewModel()
        val events = mutableListOf<AuthEvent>()
        val collector = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect { events.add(it) }
        }

        viewModel.continueAsGuest()
        advanceUntilIdle()

        assertEquals(listOf(AuthEvent.GuestSelected), events)
        assertTrue(preferences.hasSeenAuthPrompt.first())
        collector.cancel()
    }

    @Test
    fun submit_withNetworkFailure_showsNetworkError() = runTest {
        repository.signInResult = Result.failure(
            HttpRequestException("timeout", HttpRequestBuilder())
        )
        val viewModel = viewModel()
        viewModel.onEmailChange("user@example.com")
        viewModel.onPasswordChange("secret1")

        viewModel.submit()
        advanceUntilIdle()

        assertEquals(AuthError.Network, viewModel.uiState.value.error)
    }

    @Test
    fun authErrorForErrorCode_mapsKnownCodes() {
        assertEquals(AuthError.InvalidCredentials, authErrorForErrorCode(AuthErrorCode.InvalidCredentials))
        assertEquals(AuthError.EmailNotConfirmed, authErrorForErrorCode(AuthErrorCode.EmailNotConfirmed))
        assertEquals(AuthError.EmailAlreadyRegistered, authErrorForErrorCode(AuthErrorCode.EmailExists))
        assertEquals(AuthError.EmailAlreadyRegistered, authErrorForErrorCode(AuthErrorCode.UserAlreadyExists))
        assertEquals(AuthError.WeakPassword, authErrorForErrorCode(AuthErrorCode.WeakPassword))
        assertEquals(AuthError.Unknown, authErrorForErrorCode(null))
    }
}
