// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Root build.gradle.kts
plugins {
    id("androidx.navigation.safeargs.kotlin") version "2.7.6" apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

