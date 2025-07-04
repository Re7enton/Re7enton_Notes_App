plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Kotlin serialization plugin for type safe routes and navigation arguments
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.gms)
    alias(libs.plugins.gradle.versions)

}

android {
    namespace = "com.example.re7entonnotesapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.re7entonnotesapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    packaging {
        resources {
            // exclude that file entirely:
            excludes.add("META-INF/*")
            // if you run into other duplicates you can add more patterns:
            // excludes.add("META-INF/DEPENDENCIES")
            // excludes.add("META-INF/LICENSE*")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.navigation.compose)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.play.services.auth)
    ksp(libs.room.compiler)
//    kapt(libs.room.runtime) // Use appropriate KAPT dependency for Room compiler if needed
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.work.manager)
//    implementation(libs.kotlin.kapt)
    implementation(libs.ksp.api)
    implementation(libs.hilt.work)
    ksp(libs.hilt.compiler)
    implementation(libs.google.drive)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.google.drive.services)      // ← DriveScopes lives here
    implementation(libs.google.api.client.gson)
    // Preferences DataStore (key‑value)
    implementation(libs.androidx.datastore.preferences)
    // The “core” support that supplies booleanPreferencesKey & edit {…}
    implementation(libs.androidx.datastore.preferences.core)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)


    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

//// Allow references to generated code
//kapt {
//    correctErrorTypes = true
//}