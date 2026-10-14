package com.slem.gazastor.ui.screens.onboarding

import com.slem.gazastor.testing.FakeOnboardingPreferences
import com.slem.gazastor.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun complete_persistsFlagBeforeReportingDone() = runTest {
        val preferences = FakeOnboardingPreferences()
        val viewModel = OnboardingViewModel(preferences)
        var doneCalled = false

        viewModel.complete { doneCalled = true }
        advanceUntilIdle()

        assertTrue("onboarding flag should be persisted", preferences.hasCompletedOnboarding.first())
        assertTrue("callback should fire after the write", doneCalled)
    }
}
