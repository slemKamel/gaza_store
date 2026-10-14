package com.slem.gazastor.data.repository

import com.slem.gazastor.data.model.AuthUser
import com.slem.gazastor.data.model.ProfileDto
import com.slem.gazastor.data.remote.supabase.SupabaseClientProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(
    private val supabase: SupabaseClient = SupabaseClientProvider.client
) : AuthRepository {

    override val sessionState: Flow<AuthUser?> = supabase.auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated ->
                status.session.user?.let { AuthUser(id = it.id, email = it.email) }

            else -> null
        }
    }

    override suspend fun signIn(email: String, password: String): Result<Unit> = safeAuthCall {
        // Capture first: inside the config lambda `this` is the provider config.
        val loginEmail = email.trim()
        val loginPassword = password
        supabase.auth.signInWith(Email) {
            this.email = loginEmail
            this.password = loginPassword
        }
    }

    override suspend fun signUp(email: String, password: String): Result<SignUpOutcome> = safeAuthCall {
        val signUpEmail = email.trim()
        val signUpPassword = password
        supabase.auth.signUpWith(Email) {
            this.email = signUpEmail
            this.password = signUpPassword
        }
        // supabase-kt creates the session itself when the project does not
        // require email confirmation; otherwise no session exists yet.
        if (supabase.auth.currentSessionOrNull() != null) {
            SignUpOutcome.SignedIn
        } else {
            SignUpOutcome.ConfirmationRequired
        }
    }

    override suspend fun signOut(): Result<Unit> = safeAuthCall {
        supabase.auth.signOut()
    }

    override suspend fun profile(): Result<ProfileDto?> = safeAuthCall {
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: return@safeAuthCall null
        supabase.from("profiles").select {
            filter {
                eq("id", userId)
            }
        }.decodeList<ProfileDto>().firstOrNull()
    }
}

/** Wraps a suspending auth call in [Result] without swallowing cancellation. */
private suspend fun <T> safeAuthCall(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
