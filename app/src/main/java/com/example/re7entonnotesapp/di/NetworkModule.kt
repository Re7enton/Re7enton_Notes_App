package com.example.re7entonnotesapp.di

import com.example.re7entonnotesapp.data.remote.NotesApi
import com.example.re7entonnotesapp.data.remote.NotesApiImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "http://10.0.2.2:8080"  // our local Ktor server

    @Provides
    @Singleton
    fun provideBaseUrl(): String = BASE_URL

    @Provides
    @Singleton
    fun provideNotesApi(
        client: HttpClient,
        baseUrl: String
    ): NotesApi = NotesApiImpl(client, baseUrl)
}