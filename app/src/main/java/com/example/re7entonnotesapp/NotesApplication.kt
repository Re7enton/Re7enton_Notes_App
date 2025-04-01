package com.example.re7entonnotesapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// Annotate the application class to trigger Hilt's code generation.
@HiltAndroidApp
class NotesApplication : Application() {
    // Initialize any global resources here if needed.
}