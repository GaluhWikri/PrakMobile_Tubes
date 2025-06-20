// app/build.gradle.kts
// Perbaikan cara loading local.properties

import java.io.FileInputStream
import java.util.Properties

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(FileInputStream(localPropertiesFile))
    }
}

fun getProperty(key: String, defaultValue: String): String {
    return localProperties.getProperty(key, defaultValue)
}

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("kotlin-parcelize")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.example.tugaspm"
    compileSdk = 35
    ksp {
        arg("ksp.kotlin.1.9.compatibility", "true")
    }
    defaultConfig {
        applicationId = "com.example.tugaspm"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Spotify API credentials - dari local.properties
//        buildConfigField("String", "SPOTIFY_CLIENT_ID", "\"${getProperty("spotify.client.id", "")}\"")
//        buildConfigField("String", "SPOTIFY_CLIENT_SECRET", "\"${getProperty("spotify.client.secret", "")}\"")
//        buildConfigField("String", "SPOTIFY_REDIRECT_URI", "\"${getProperty("spotify.redirect.uri", "")}\"")

        // YouTube API key - dari local.properties
//        buildConfigField("String", "YOUTUBE_API_KEY", "\"${getProperty("youtube.api.key", "")}\"")
//        manifestPlaceholders.put("redirectSchemeName", "com.virtualrealm.virtualrealmmusicplayer")
//        manifestPlaceholders.put("redirectHostName", "callback")
//        manifestPlaceholders.put("appAuthRedirectScheme", "com.virtualrealm.virtualrealmmusicplayer")

        manifestPlaceholders["redirectSchemeName"] = "com.example.tugaspm"
        manifestPlaceholders["redirectHostName"] = "callback"
        manifestPlaceholders["appAuthRedirectScheme"] = "com.example.tugaspm"
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

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    implementation("androidx.compose.material:material-icons-extended:1.6.7") // Ganti 1.6.7 dengan versi terbaru yang kompatibel dengan Compose BOM Anda
    //api
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    // Extended Icons
    implementation("androidx.compose.material:material-icons-extended:1.5.8")

    // Swipe actions
    implementation("me.saket.swipe:swipe:1.1.1")
    // Core Android & Kotlin
    implementation(libs.androidx.core.ktx.v1120)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx.v270)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Compose
    implementation(libs.androidx.activity.compose.v182)
    implementation(platform(libs.androidx.compose.bom.v20231001))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.lifecycle.service)
    debugImplementation(libs.ui.tooling)

    // Compose Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    // Animated Bottom Navigation Bar
    implementation("com.exyte:animated-navigation-bar:1.0.0")
//    implementation(libs.spotify.player) // Add this
    // Accompanist (Compose utilities)
    implementation(libs.accompanist.permissions)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.accompanist.webview)

    // Hilt Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler) // Added Hilt compiler dependency

    // Room for local database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Retrofit & OkHttp for API requests
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // Coroutines for asynchronous programming
    implementation(libs.kotlinx.coroutines.android)

    // Coil for image loading with Compose
    implementation(libs.coil.compose)

    // Spotify SDK
    implementation(libs.auth)

    // YouTube Player API for Compose
    implementation(libs.core)
    implementation(libs.chromecast.sender)

    // DataStore Preferences
    implementation(libs.androidx.datastore.preferences)

    // Add to dependencies section in app/build.gradle.kts
    implementation(libs.glide)
    implementation(libs.androidx.media)  // For MediaStyle notifications
    implementation(libs.appauth)
    implementation(libs.androidx.browser)

    // For JSON processing
    implementation(libs.converter.gson)

    // Skydoves Sandwich for API response handling
    implementation(libs.skydoves.sandwich)
    implementation(libs.skydoves.whatif)
    implementation(libs.skydoves.sandwich.retrofit)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v115)
    androidTestImplementation(libs.androidx.espresso.core.v351)
    androidTestImplementation(platform(libs.androidx.compose.bom.v20250500))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.test.manifest)

    implementation("com.google.accompanist:accompanist-systemuicontroller:0.33.2-alpha")


    // spotify aar
//    implementation(files("libs/spotify-app-remote-release-0.8.0.aar"))
//    implementation(files("libs/spotify-auth-release-2.1.0.aar"))
}