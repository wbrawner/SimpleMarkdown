plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.fladle) apply false
    alias(libs.plugins.dependency.analysis)
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.baselineprofile) apply false
}