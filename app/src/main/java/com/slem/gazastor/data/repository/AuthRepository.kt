package com.slem.gazastor.data.repository

import com.slem.gazastor.data.model.AuthUser
import com.slem.gazastor.data.model.ProfileDto
import kotlinx.coroutines.flow.Flow

/** What happened after a successful sign-up call. */
enum class SignUpOutcome {
    /** A session was created immediately (email confirmation is disabled). */
    SignedIn,

    /** The account exists but the user must confirm their email before signing in. */
    ConfirmationRequired
}

/**
 * Account access on top of Supabase Auth. Every call returns a [Result] (same
 * convention as [ContentRepository]) so the UI can show the real reason instead
 * of failing silently. Guests simply never get a session; they use the app as
 * the anonymous role.
 */
interface AuthRepository {

    /** Emits the signed-in user, or `null` while signed out. */
    val sessionState: Flow<AuthUser?>

    suspend fun signIn(email: String, password: String): Result<Unit>

    suspend fun signUp(email: String, password: String): Result<SignUpOutcome>

    suspend fun signOut(): Result<Unit>

    /** The current user's `profiles` row, or `null` when signed out / not found. */
    suspend fun profile(): Result<ProfileDto?>

    /**
     * Updates the signed-in user's own name and phone. [phone] is stored as SQL
     * NULL when null/blank, so the account screen can omit the line entirely.
     *
     * Only these two columns are ever sent: the `profiles` update policy allows
     * the owner's row, and the role-escalation trigger rejects any role change.
     */
    suspend fun updateProfile(fullName: String, phone: String?): Result<Unit>
}
