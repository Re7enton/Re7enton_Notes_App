package com.example.re7entonnotesapp.auth

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.GetCustomCredentialOption
import androidx.credentials.CredentialManagerCallback
import androidx.credentials.CustomCredential
import androidx.credentials.PrepareGetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.gms.auth.api.identity.AuthorizationClient
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.re7entonnotesapp.R
import com.example.re7entonnotesapp.presentation.AuthState
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "AuthViewModel"

@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val credentialManager: CredentialManager,
    private val authorizationClient: AuthorizationClient,
    private val authPrefs: AuthPrefs,                       // persisted prefs
    private val authStateFlow: MutableStateFlow<AuthState>  // shared flow
) : ViewModel() {

    /** Expose read‐only AuthState to UI */
    val authState: StateFlow<AuthState> = authStateFlow

    private val _error = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _error

    init {
        // restore persisted Drive‑authorized
        viewModelScope.launch {
            authPrefs.driveAuthorizedFlow.collect { granted ->
                if (granted) {
                    // update in-memory state
                    authStateFlow.update { it.copy(driveAuthorized = true) }
                }
            }
        }
    }

    /** Helper: parse “email” claim from JWT. */
    private fun parseEmail(token: String?): String? =
        token
            ?.split(".")
            ?.getOrNull(1)
            ?.let { String(Base64.decode(it, Base64.URL_SAFE)) }
            ?.let { org.json.JSONObject(it).optString("email").takeIf { e->e.isNotEmpty() } }

    /** Update the shared flow. */
    private fun updateAuth(transform: AuthState.() -> AuthState) {
        authStateFlow.update(transform)
    }

    /** Silent one‐tap sign‑in (no UI) on API‑34+. */
    @RequiresApi(34)
    fun trySilentSignIn() {
        Log.d(TAG,"trySilentSignIn()")
        val option = GetGoogleIdOption.Builder()
            .setServerClientId(context.getString(R.string.server_client_id))
            .setFilterByAuthorizedAccounts(true)
            .build()
        val req = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        credentialManager.getCredentialAsync(
            context, req, null,
            ContextCompat.getMainExecutor(context),
            object: CredentialManagerCallback<GetCredentialResponse,GetCredentialException>{
                override fun onResult(r: GetCredentialResponse) = handleCredential(r.credential)
                override fun onError(e: GetCredentialException) {
                    Log.d(TAG,"silent sign‑in failed",e)
                }
            }
        )
    }

    /** Interactive “Sign In with Google” (shows UI). */
    @RequiresApi(34)
    fun signIn() {
        Log.d(TAG,"signIn()")
        val option = GetSignInWithGoogleOption.Builder(
            context.getString(R.string.server_client_id)
        ).build()
        val req = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        credentialManager.getCredentialAsync(
            context, req, null,
            ContextCompat.getMainExecutor(context),
            object: CredentialManagerCallback<GetCredentialResponse,GetCredentialException>{
                override fun onResult(r: GetCredentialResponse) {
                    Log.d(TAG,"signIn: credential=${r.credential::class.simpleName}")
                    handleCredential(r.credential)
                }
                override fun onError(e: GetCredentialException) {
                    Log.e(TAG,"interactive sign‑in failed",e)
                    _error.value = context.getString(R.string.sign_in_failed)
                }
            }
        )
    }

    /** Common handler for both silent & interactive flows. */
    private fun handleCredential(cred: androidx.credentials.Credential) {
        when {
            // direct ID‐token
            cred is GoogleIdTokenCredential -> {
                val t = cred.idToken
                updateAuth { copy(idToken=t, email=parseEmail(t)) }
            }
            // wrapped in CustomCredential
            cred is CustomCredential
                    && cred.type==GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                try {
                    val idCred = GoogleIdTokenCredential.createFrom(cred.data)
                    val t = idCred.idToken
                    updateAuth { copy(idToken=t, email=parseEmail(t)) }
                } catch(ex:Exception) {
                    Log.e(TAG,"invalid ID token",ex)
                    _error.value = context.getString(R.string.invalid_token)
                }
            }
            else -> {
                Log.e(TAG,"unknown credential: ${cred::class}")
                _error.value = context.getString(R.string.unsupported_credential)
            }
        }
    }

    /** Request Drive appDataFolder consent (UI). */
    @RequiresApi(34)
    fun requestDriveAuth(onPendingIntent:(PendingIntent)->Unit) {
        Log.d(TAG,"requestDriveAuth()")
        val req = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(DriveScopes.DRIVE_APPDATA)))
            .build()
        authorizationClient.authorize(req)
            .addOnSuccessListener{ res:AuthorizationResult ->
                if(res.hasResolution()) onPendingIntent(res.getPendingIntent()!!)
                else {
                    updateAuth { copy(driveAuthorized=true, driveAccessToken=res.accessToken) }
                    // persist flag
                    viewModelScope.launch { authPrefs.setDriveAuthorized(true) }
                }
            }
            .addOnFailureListener{ e->
                Log.e(TAG,"Drive consent error",e)
                _error.value = context.getString(R.string.drive_auth_failed)
            }
    }

    /** Handle Drive consent result Intent. */
    @RequiresApi(34)
    fun handleDriveAuthResponse(data:Intent?) {
        Log.d(TAG,"handleDriveAuthResponse")
        if(data==null) {
            _error.value = context.getString(R.string.no_drive_data)
            return
        }
        val res = authorizationClient.getAuthorizationResultFromIntent(data)
        if(!res.hasResolution()) {
            updateAuth { copy(driveAuthorized=true, driveAccessToken=res.accessToken) }
            viewModelScope.launch { authPrefs.setDriveAuthorized(true) }
        } else {
            Log.e(TAG,"Drive consent still needs resolution")
            _error.value = context.getString(R.string.drive_consent_incomplete)
        }
    }

    /** Sign‑out resets everything + clears persisted flag. */
    fun signOut() {
        Log.d(TAG,"signOut()")
        updateAuth { AuthState() }
        viewModelScope.launch { authPrefs.setDriveAuthorized(false) }
    }
}