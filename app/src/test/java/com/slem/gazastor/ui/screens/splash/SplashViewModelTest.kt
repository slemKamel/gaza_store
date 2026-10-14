package com.slem.gazastor.ui.screens.splash

import com.slem.gazastor.testing.FakeAuthPreferences
import com.slem.gazastor.testing.FakeOnboardingPreferences
import com.slem.gazastor.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun startDestination_isNotResolvedBeforeTheDelay() = runTest {
        val viewModel = SplashViewModel(FakeOnboardingPreferences(), FakeAuthPreferences())

        assertNull(viewModel.startDestination.value)
    }

    @Test
    fun startDestination_isOnboarding_whenOnboardingNotCompleted() = runTest {
        val viewModel = SplashViewModel(
            onboardingPreferences = FakeOnboardingPreferences(initial = false),
            authPreferences = FakeAuthPreferences(initial = false)
        )

        advanceUntilIdle()

        assertEquals(StartDestination.Onboarding, viewModel.startDestination.value)
    }

    @Test
    fun startDestination_isAuth_whenAuthPromptNotSeenYet() = runTest {
        val viewModel = SplashViewModel(
            onboardingPreferences = FakeOnboardingPreferences(initial = true),
            authPreferences = FakeAuthPreferences(initial = false)
        )

        advanceUntilIdle()

        assertEquals(StartDestination.Auth, viewModel.startDestination.value)
    }

    @Test
    fun startDestination_isHome_whenEverythingAlreadySeen() = runTest {
        val viewModel = SplashViewModel(
            onboardingPreferences = FakeOnboardingPreferences(initial = true),
            authPreferences = FakeAuthPreferences(initial = true)
        )

        advanceUntilIdle()

        assertEquals(StartDestination.Home, viewModel.startDestination.value)
    }
}
