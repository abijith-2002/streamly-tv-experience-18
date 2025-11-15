plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.android.streamly"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.android.streamly"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // Helpful for diagnosing resource merge issues in CI
            isCrunchPngs = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        // Enable Jetpack Compose to allow composing the Home UI scaffolding and theme
        compose = true
    }

    // Configure Compose compiler extension version compatible with Kotlin 1.9.22
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }

    // Help deterministic resource processing
    packaging {
        resources {
            // Exclude common license resources to avoid merge noise
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Android TV Core (use latest stable Leanback 1.1.0-rc01 to stay AndroidX; avoid old support libs)
    implementation("androidx.leanback:leanback:1.1.0-rc01")
    implementation("androidx.tvprovider:tvprovider:1.0.0")

    // AndroidX Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Media3 (ExoPlayer and UI) - keep versions consistent
    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-ui:1.2.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.2.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.2.1")

    // Image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    // Coil for images (core + Compose integration) with explicit versions
    implementation("io.coil-kt:coil:2.6.0")
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Jetpack Compose (explicit versions compatible with Kotlin 1.9.22 / Compiler 1.5.10)
    implementation("androidx.compose.runtime:runtime:1.6.7")
    implementation("androidx.compose.ui:ui:1.6.7")
    implementation("androidx.compose.foundation:foundation:1.6.7")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.7")
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.7")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

// Provide a task to clean caches when resource merge errors happen in CI
tasks.register("ciClean") {
    group = "ci"
    description = "Cleans build directories and Gradle caches to fix potential corrupted intermediates."
    doLast {
        val appBuild = file("${project.projectDir}/build")
        if (appBuild.exists()) appBuild.deleteRecursively()
        val rootBuild = file("${rootProject.projectDir}/build")
        if (rootBuild.exists()) rootBuild.deleteRecursively()
        println("Cleaned module and root build directories.")
    }
}
