package com.example.re7entonnotesapp.di

import android.content.Context
import androidx.credentials.CredentialManager
import com.example.re7entonnotesapp.R
import com.example.re7entonnotesapp.presentation.AuthState
import com.google.android.gms.auth.api.identity.AuthorizationClient
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

/**
 * Provides the new Credential Manager and Identity clients
 * — replaces the old GoogleSignInClient approach.
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthState(): AuthState = AuthState()

      @Provides @Singleton
      fun provideGoogleSignInOptions(@ApplicationContext ctx: Context) =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
          .requestEmail()
          .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
          .requestIdToken(ctx.getString(R.string.server_client_id))
          .build()

      @Provides @Singleton
      fun provideGoogleSignInClient(
            @ApplicationContext ctx: Context,
        opts: GoogleSignInOptions
      ): GoogleSignInClient =
        GoogleSignIn.getClient(ctx, opts)


    /** AndroidX CredentialManager for federated sign-in, passkeys, passwords */
    @Provides
    @Singleton
    fun provideCredentialManager(@ApplicationContext ctx: Context): CredentialManager =
        CredentialManager.create(ctx)

    /** Identity API Sign-In client (BeginSignInRequest / getSignInCredentialFromIntent) */
    @Provides
    @Singleton
    fun provideSignInClient(@ApplicationContext ctx: Context): SignInClient =
        Identity.getSignInClient(ctx)

    /** Identity API Authorization client (for Drive OAuth scopes) */
    @Provides
    @Singleton
    fun provideAuthorizationClient(@ApplicationContext ctx: Context): AuthorizationClient =
        Identity.getAuthorizationClient(ctx)
}