package com.example.re7entonnotesapp.auth

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.re7entonnotesapp.domain.model.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authUseCase: AuthUseCase
) : ViewModel() {

    // UI State: Exposed to the UI
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState

    /**
     * Called when the user taps "Sign in with Google".
     * Only triggers if the device is API 34+.
     */
    fun onSignInRequested() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            _authState.value = AuthState.UnsupportedVersion
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = authUseCase.signIn()

            _authState.value = when {
                result.isSuccess -> AuthState.Authenticated(result.getOrThrow())
                else -> AuthState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun onSignOutRequested() {
        viewModelScope.launch {
            authUseCase.signOut()
            _authState.value = AuthState.Unauthenticated
        }
    }
}