package com.slem.gazastor.data.preferences

import kotlinx.coroutines.flow.Flow

/**
 * Whether the first-launch sign-in prompt has already been shown.
 *
 * Set once the user either signs in or picks "continue as a guest", so the
 * prompt appears a single time and the app stays usable without an account.
 */
interface AuthPreferences {

    /** Emits whether the sign-in prompt has already been dismissed or completed. */
    val hasSeenAuthPrompt: Flow<Boolean>

    /** Marks the sign-in prompt as seen so it is not shown again. */
    suspend fun setAuthPromptSeen()
}
