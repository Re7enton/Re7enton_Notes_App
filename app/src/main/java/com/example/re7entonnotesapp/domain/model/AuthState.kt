package com.example.re7entonnotesapp.domain.model

/**
 * Holds all of the authentication + authorization status your UI needs.
 */
data class AuthState(
    val idToken: String? = null,     // raw JWT if signed in
    val email: String? = null,       // parsed “email” claim
    val driveAuthorized: Boolean = false,
    val driveAccessToken: String? = null
)
