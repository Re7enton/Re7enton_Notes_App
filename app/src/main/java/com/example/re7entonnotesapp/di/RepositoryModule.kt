package com.example.re7entonnotesapp.di

import com.example.re7entonnotesapp.data.repository.NoteRepositoryImpl
import com.example.re7entonnotesapp.domain.repository.NoteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNoteRepository(
        impl: NoteRepositoryImpl
    ): NoteRepository
}