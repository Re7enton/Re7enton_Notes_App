package com.example.re7entonnotesapp.di

import android.content.Context
import androidx.room.Room
import com.example.re7entonnotesapp.data.NoteDao
import com.example.re7entonnotesapp.data.NoteDatabase
import com.example.re7entonnotesapp.data.network.NoteApi
import com.example.re7entonnotesapp.repository.NoteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideNoteDatabase(@ApplicationContext context: Context): NoteDatabase =
        Room.databaseBuilder(
            context,
            NoteDatabase::class.java,
            "notes_db"
        ).build()

    @Singleton
    @Provides
    fun provideNoteDao(database: NoteDatabase) = database.noteDao()

    @Provides
    @Singleton
    fun provideHttpClient() = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    @Provides
    @Singleton
    fun provideNoteApi(client: HttpClient) = NoteApi(client)

    @Provides @Singleton
    fun provideRepository(dao: NoteDao, api: NoteApi) =
        NoteRepository(dao, api)
}