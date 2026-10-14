package com.slem.gazastor.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slem.gazastor.core.di.AppModule
import com.slem.gazastor.data.preferences.OnboardingPreferences
import kotlinx.coroutines.launch

/**
 * Persists that onboarding was completed. Navigation is triggered only after the
 * write succeeds, so the flow can never show up again because of a cancelled
 * scope.
 */
class OnboardingViewModel(
    private val onboardingPreferences: OnboardingPreferences = AppModule.userPreferences
) : ViewModel() {

    fun complete(onDone: () -> Unit) {
        viewModelScope.launch {
            onboardingPreferences.setOnboardingCompleted()
            onDone()
        }
    }
}
