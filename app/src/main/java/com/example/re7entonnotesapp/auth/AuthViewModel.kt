package com.example.re7entonnotesapp.auth

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.re7entonnotesapp.presentation.AuthState
import com.google.android.gms.auth.api.signin.GoogleSignIn          // static helper
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInClient: GoogleSignInClient,
    defaultState: AuthState
) : ViewModel() {

    private val _state = MutableStateFlow(defaultState)
    val stateFlow: StateFlow<AuthState> = _state

    /** Launch the GoogleSignIn intent */
    fun getSignInIntent() = signInClient.signInIntent

    /** Handle the result from that intent */
    fun handleSignInResult(data: Intent?) {
        if (data == null) return

        // GoogleSignInClient does NOT have getSignedInAccountFromIntent(...)
        // Use the static helper on GoogleSignIn instead
        GoogleSignIn.getSignedInAccountFromIntent(data)
            .addOnSuccessListener { acct: GoogleSignInAccount ->
                // update our state with email + whether Drive-appDataFolder was granted
                val email = acct.email
                // grantedScopes is Set<Scope>; use scopeUri, not .uri
                val hasDrive = acct.grantedScopes.any { scope ->
                    scope.scopeUri == DriveScopes.DRIVE_APPDATA
                }
                _state.update { it.copy(email = email, driveAuthorized = hasDrive) }
            }
            .addOnFailureListener {
                // user canceled or error
            }
    }

    /** Sign‑out resets everything */
    fun signOut() {
        signInClient.signOut().addOnCompleteListener {
            _state.value = AuthState()
        }
    }
}