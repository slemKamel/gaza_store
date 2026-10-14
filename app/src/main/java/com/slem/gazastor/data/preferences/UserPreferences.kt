package com.slem.gazastor.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Single DataStore file shared by all local preferences. */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "gaza_stor_preferences"
)

private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
private val KEY_AUTH_PROMPT_SEEN = booleanPreferencesKey("auth_prompt_seen")

/**
 * DataStore-backed local preferences.
 *
 * Implements the narrow preference interfaces so ViewModels depend on the
 * behaviour they need (and stay fakeable in tests) rather than on DataStore.
 */
class UserPreferences(private val context: Context) : OnboardingPreferences, AuthPreferences {

    override val hasCompletedOnboarding: Flow<Boolean> =
        context.dataStore.data.map { it[KEY_ONBOARDING_COMPLETED] ?: false }

    override suspend fun setOnboardingCompleted() {
        context.dataStore.edit { it[KEY_ONBOARDING_COMPLETED] = true }
    }

    override val hasSeenAuthPrompt: Flow<Boolean> =
        context.dataStore.data.map { it[KEY_AUTH_PROMPT_SEEN] ?: false }

    override suspend fun setAuthPromptSeen() {
        context.dataStore.edit { it[KEY_AUTH_PROMPT_SEEN] = true }
    }
}
