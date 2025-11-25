plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.proyectoappteam"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.proyectoappteam"
        minSdk = 24
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
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)

    implementation("androidx.activity:activity:1.7.2")

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Backendless
    implementation("com.backendless:backendless:6.2.0")
    // NUEVO: Dependencia requerida para la funcionalidad de tiempo real de Backendless
    implementation("io.socket:socket.io-client:2.0.0")

    // Google Maps & Location
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Google Places
    implementation("com.google.android.libraries.places:places:3.4.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
