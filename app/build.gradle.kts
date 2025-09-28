plugins {
    id("com.android.application") version "8.13.0"
}

android {
    namespace = "com.example.proyectoappteam"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.proyectoappteam"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Backendless
    implementation("com.backendless:backendless:6.2.0")

    // Dependencias de Google Maps
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Dependencia de Google Places para el Autocomplete
    implementation("com.google.android.libraries.places:places:3.4.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}
