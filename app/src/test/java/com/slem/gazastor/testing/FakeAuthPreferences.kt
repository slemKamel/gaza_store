package com.slem.gazastor.testing

import com.slem.gazastor.data.preferences.AuthPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [AuthPreferences] so ViewModel tests need no Android Context. */
class FakeAuthPreferences(initial: Boolean = false) : AuthPreferences {

    private val seen = MutableStateFlow(initial)

    override val hasSeenAuthPrompt: Flow<Boolean> = seen

    override suspend fun setAuthPromptSeen() {
        seen.value = true
    }
}
