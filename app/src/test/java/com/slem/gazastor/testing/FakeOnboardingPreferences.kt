package com.slem.gazastor.testing

import com.slem.gazastor.data.preferences.OnboardingPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [OnboardingPreferences] so ViewModel tests need no Android Context. */
class FakeOnboardingPreferences(initial: Boolean = false) : OnboardingPreferences {

    private val completed = MutableStateFlow(initial)

    override val hasCompletedOnboarding: Flow<Boolean> = completed

    override suspend fun setOnboardingCompleted() {
        completed.value = true
    }
}
