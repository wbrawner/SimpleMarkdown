buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.2")
        classpath("com.google.gms:google-services:4.3.15")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.4")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.10")
        classpath("com.osacky.flank.gradle:fladle:0.17.4")
    }
}

plugins {
    id("com.autonomousapps.dependency-analysis") version "1.31.0"
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
