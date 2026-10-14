package com.slem.gazastor.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slem.gazastor.core.di.AppModule
import com.slem.gazastor.data.preferences.AuthPreferences
import com.slem.gazastor.data.preferences.OnboardingPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Where the splash hands control over once the brand moment is over. */
enum class StartDestination { Onboarding, Auth, Home }

/**
 * Reads the local first-launch flags while the splash is visible so the right
 * screen can be chosen without a visible flicker.
 */
class SplashViewModel(
    private val onboardingPreferences: OnboardingPreferences = AppModule.userPreferences,
    private val authPreferences: AuthPreferences = AppModule.userPreferences,
    private val minimumDisplayMillis: Long = MINIMUM_DISPLAY_MILLIS
) : ViewModel() {

    private val _startDestination = MutableStateFlow<StartDestination?>(null)

    /** `null` while the splash is still showing. */
    val startDestination: StateFlow<StartDestination?> = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            val onboardingDone = onboardingPreferences.hasCompletedOnboarding.first()
            val authPromptSeen = authPreferences.hasSeenAuthPrompt.first()
            // Keep the brand on screen briefly even when DataStore answers instantly.
            delay(minimumDisplayMillis)
            _startDestination.value = when {
                !onboardingDone -> StartDestination.Onboarding
                !authPromptSeen -> StartDestination.Auth
                else -> StartDestination.Home
            }
        }
    }

    companion object {
        const val MINIMUM_DISPLAY_MILLIS = 800L
    }
}
