package com.example.re7entonnotesapp.di

import android.content.Context
import androidx.room.Room
import com.example.re7entonnotesapp.data.local.NoteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideNoteDatabase(@ApplicationContext context: Context): NoteDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            NoteDatabase::class.java,
            "notes_db"
        ).build()

    @Singleton
    @Provides
    fun provideNoteDao(database: NoteDatabase) = database.noteDao()
}