package com.example.re7entonnotesapp.di

import android.content.Context
import androidx.credentials.CredentialManager
import com.example.re7entonnotesapp.auth.AuthState
import com.google.android.gms.auth.api.identity.AuthorizationClient
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient

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