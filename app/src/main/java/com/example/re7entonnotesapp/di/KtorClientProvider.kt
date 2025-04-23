package com.example.re7entonnotesapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton
import io.ktor.client.engine.android.Android


@Module
@InstallIn(SingletonComponent::class)
object KtorClientProvider {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient =
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            defaultRequest {
                // set these on every request
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Accept,      ContentType.Application.Json)
            }
        }
}
