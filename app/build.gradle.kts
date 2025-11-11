plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.fortress.hypervisor"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.fortress.hypervisor"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Enable MultiDex to prevent OOM crashes
        multiDexEnabled = true

        // Pixel 7 Pro only supports ARM64 - remove other architectures to prevent crashes
        ndk {
            abiFilters.add("arm64-v8a")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        resources {
            // Pixel 7 Pro only supports ARM64 - only include ARM64 native library
            pickFirsts.add("lib/arm64-v8a/libfortress_hypervisor.so")
        }
    }
}

dependencies {
    implementation("androidx.multidex:multidex:2.0.1")

    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.7.2")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.0")

    // ViewModel and LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")

    // Work Manager for background tasks
    implementation("androidx.work:work-runtime-ktx:2.8.1")

    // Security and encryption
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("androidx.biometric:biometric:1.1.0")

    // Network monitoring
    implementation("androidx.core:core-ktx:1.10.1")

    // Notifications
    implementation("androidx.core:core:1.10.1")

    // AppCompat and legacy support
    implementation("androidx.appcompat:appcompat:1.6.1")

    // JSON serialization
    implementation("com.google.code.gson:gson:2.10.1")
}
