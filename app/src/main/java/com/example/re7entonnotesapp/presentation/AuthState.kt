package com.example.re7entonnotesapp.presentation

/**
 * Holds all of the authentication + authorization status your UI needs.
 */
data class AuthState(
    val idToken: String? = null,     // raw JWT if signed in
    val email: String? = null,       // parsed “email” claim
    val driveAuthorized: Boolean = false,
    val driveAccessToken: String? = null
)

//data class AuthState(
//    val email: String? = null,
//    val idToken: String? = null,
//    val driveAuthorized: Boolean = false,
//    val driveAccessToken: String? = null,
//    val needsDrivePermission: Boolean = false,
//    val error: String? = null
//)