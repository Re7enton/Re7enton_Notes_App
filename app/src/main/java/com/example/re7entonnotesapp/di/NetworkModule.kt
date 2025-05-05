package com.example.re7entonnotesapp.di

import com.example.re7entonnotesapp.data.remote.DriveNotesApiImpl
import com.example.re7entonnotesapp.data.remote.NotesApi
import com.example.re7entonnotesapp.presentation.AuthState
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Single shared flow holding the current AuthState (ID token, Drive token, etc.).
     * AuthViewModel will update this, DriveNotesApiImpl will read it.
     */
    @Provides
    @Singleton
    fun provideAuthStateFlow(): MutableStateFlow<AuthState> =
        MutableStateFlow(AuthState())

    /** Expose read‑only view of the same flow */
    @Provides
    fun provideAuthStateView(flow: MutableStateFlow<AuthState>): StateFlow<AuthState> = flow

    /**
     * Provide the NotesApi implementation that reads from the shared AuthState flow.
     */
    @Provides
    @Singleton
    fun provideNotesApi(
        authStateFlow: StateFlow<AuthState>
    ): NotesApi = DriveNotesApiImpl(authStateFlow)
}
