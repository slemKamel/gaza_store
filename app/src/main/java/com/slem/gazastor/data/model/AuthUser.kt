package com.slem.gazastor.data.model

/** Minimal signed-in user surfaced to the UI. */
data class AuthUser(
    val id: String,
    val email: String?
)
