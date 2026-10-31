package com.slem.gazastor.testing

import com.slem.gazastor.data.model.AuthUser
import com.slem.gazastor.data.model.ProfileDto
import com.slem.gazastor.data.repository.AuthRepository
import com.slem.gazastor.data.repository.SignUpOutcome
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Hand-written [AuthRepository] fake: no mocking framework, fully deterministic. */
class FakeAuthRepository(
    initialUser: AuthUser? = null
) : AuthRepository {

    private val session = MutableStateFlow(initialUser)

    override val sessionState: Flow<AuthUser?> = session

    var signInResult: Result<Unit> = Result.success(Unit)
    var signUpResult: Result<SignUpOutcome> = Result.success(SignUpOutcome.SignedIn)
    var signOutResult: Result<Unit> = Result.success(Unit)
    var profileResult: Result<ProfileDto?> = Result.success(null)
    var updateProfileResult: Result<Unit> = Result.success(Unit)

    var signInCalls = 0
        private set
    var signUpCalls = 0
        private set
    var updateProfileCalls = 0
        private set
    var lastUpdatedName: String? = null
        private set
    var lastUpdatedPhone: String? = null
        private set

    override suspend fun signIn(email: String, password: String): Result<Unit> {
        signInCalls++
        signInResult.getOrNull()?.let { session.value = AuthUser(id = "user-1", email = email) }
        return signInResult
    }

    override suspend fun signUp(email: String, password: String): Result<SignUpOutcome> {
        signUpCalls++
        if (signUpResult.getOrNull() == SignUpOutcome.SignedIn) {
            session.value = AuthUser(id = "user-1", email = email)
        }
        return signUpResult
    }

    override suspend fun signOut(): Result<Unit> {
        session.value = null
        return signOutResult
    }

    override suspend fun profile(): Result<ProfileDto?> = profileResult

    override suspend fun updateProfile(fullName: String, phone: String?): Result<Unit> {
        updateProfileCalls++
        lastUpdatedName = fullName
        lastUpdatedPhone = phone
        if (updateProfileResult.isSuccess) {
            val current = profileResult.getOrNull()
            profileResult = Result.success(
                current?.copy(full_name = fullName, phone = phone)
                    ?: ProfileDto(id = "user-1", full_name = fullName, phone = phone)
            )
        }
        return updateProfileResult
    }
}
