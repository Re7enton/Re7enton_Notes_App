package com.example.re7entonnotesapp.di

import android.content.Context
import androidx.credentials.CredentialManager
import com.google.android.gms.auth.api.identity.AuthorizationClient
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides only the AndroidX CredentialManager & Identity clients
 * for our new non‑deprecated sign‑in + Drive‑consent flows.
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    /** AndroidX CredentialManager for one‑tap sign‑in, passkeys, passwords */
    @Provides
    @Singleton
    fun provideCredentialManager(
        @ApplicationContext ctx: Context
    ): CredentialManager = CredentialManager.create(ctx)

    /** Identity API Sign‑In client (GetSignInWithGoogleOption / GetGoogleIdOption) */
    @Provides
    @Singleton
    fun provideSignInClient(
        @ApplicationContext ctx: Context
    ): SignInClient = Identity.getSignInClient(ctx)

    /** Identity API Authorization client (for Drive appDataFolder) */
    @Provides
    @Singleton
    fun provideAuthorizationClient(
        @ApplicationContext ctx: Context
    ): AuthorizationClient = Identity.getAuthorizationClient(ctx)
}
