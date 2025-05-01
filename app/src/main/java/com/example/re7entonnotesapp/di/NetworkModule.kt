package com.example.re7entonnotesapp.di

import android.content.Context
import com.example.re7entonnotesapp.domain.model.AuthState
import com.example.re7entonnotesapp.data.remote.DriveNotesApiImpl
import com.example.re7entonnotesapp.data.remote.NotesApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideNotesApi(
        @ApplicationContext ctx: Context,
        authState: AuthState
    ): NotesApi = DriveNotesApiImpl(ctx, authState)

}