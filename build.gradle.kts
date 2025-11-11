plugins {
    id("com.android.application") version "8.13.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    id("org.mozilla.rust-android-gradle.rust-android") version "0.9.3" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
