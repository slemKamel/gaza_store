package com.slem.gazastor.data.preferences

import kotlinx.coroutines.flow.Flow

/**
 * Local, device-scoped onboarding flag.
 *
 * Kept as an interface so callers (ViewModels) depend on the behaviour rather
 * than DataStore, which lets unit tests pass a fake without a mocking framework.
 */
interface OnboardingPreferences {

    /** Emits whether the user already walked through the onboarding pages. */
    val hasCompletedOnboarding: Flow<Boolean>

    /** Marks onboarding as done so it is skipped on the next launch. */
    suspend fun setOnboardingCompleted()
}
