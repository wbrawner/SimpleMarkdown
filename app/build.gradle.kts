import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.Properties

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.osacky.fladle")
    id("com.github.triplet.play") version "3.8.4"
    id("com.wbrawner.releasehelper")
}

val keystoreProperties = Properties()
try {
    val keystorePropertiesFile = rootProject.file("keystore.properties")
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
} catch (ignored: FileNotFoundException) {
    logger.warn("Unable to load keystore properties. Automatic signing won't be available")
    keystoreProperties["keyAlias"] = ""
    keystoreProperties["keyPassword"] = ""
    keystoreProperties["storeFile"] = File.createTempFile("temp", ".tmp").absolutePath
    keystoreProperties["storePassword"] = ""
}

android {
    packagingOptions {
        resources {
            excludes += listOf(
                "META-INF/LICENSE-LGPL-2.1.txt",
                "META-INF/LICENSE-LGPL-3.txt",
                "META-INF/LICENSE-W3C-TEST",
                "META-INF/LICENSE",
                "META-INF/DEPENDENCIES"
            )
        }
    }
    compileSdk = 34
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    defaultConfig {
        applicationId = "com.wbrawner.simplemarkdown"
        minSdk = 23
        targetSdk = 34
        versionCode = 41
        versionName = "0.8.16"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
        buildConfigField("boolean", "ENABLE_CUSTOM_CSS", "true")
    }
    signingConfigs {
        create("playRelease") {
            keyAlias = keystoreProperties["keyAlias"].toString()
            keyPassword = keystoreProperties["keyPassword"].toString()
            storeFile = file(keystoreProperties["storeFile"].toString())
            storePassword = keystoreProperties["storePassword"].toString()
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("boolean", "ENABLE_CUSTOM_CSS", "false")
        }
    }
    flavorDimensions.add("platform")
    productFlavors {
        create("free") {
            applicationIdSuffix = ".free"
            versionNameSuffix = "-free"
        }
        create("play") {
            signingConfig = signingConfigs["playRelease"]
        }
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }
    namespace = "com.wbrawner.simplemarkdown"
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    playConfigs {
        register("play") {
            enabled.set(true)
            commit.set(true)
        }
    }
}

play {
    commit.set(false)
    enabled.set(false)
    track.set("production")
    defaultToAppBundles.set(true)
}

dependencies {
    "freeImplementation"(project(":free"))
    "playImplementation"(project(":non-free"))
    implementation("androidx.compose.material3:material3-window-size-class-android:1.2.0")
    val navigationVersion = "2.7.2"
    implementation("androidx.navigation:navigation-compose:$navigationVersion")
    testImplementation("junit:junit:4.13.2")
    testRuntimeOnly("org.robolectric:robolectric:4.2.1")
    val espressoVersion = "3.5.1"
    androidTestImplementation("androidx.test.espresso:espresso-core:$espressoVersion")
    androidTestImplementation("androidx.test.espresso:espresso-web:$espressoVersion")
    androidTestImplementation("androidx.test.espresso:espresso-intents:$espressoVersion")
    androidTestRuntimeOnly("androidx.test:runner:1.5.2")
    androidTestUtil("androidx.test:orchestrator:1.4.2")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.browser:browser:1.6.0")
    val commonMarkVersion = "0.22.0"
    implementation("org.commonmark:commonmark:$commonMarkVersion")
    implementation("org.commonmark:commonmark-ext-gfm-tables:$commonMarkVersion")
    implementation("org.commonmark:commonmark-ext-gfm-strikethrough:$commonMarkVersion")
    implementation("org.commonmark:commonmark-ext-autolink:$commonMarkVersion")
    implementation("org.commonmark:commonmark-ext-task-list-items:$commonMarkVersion")
    implementation("org.commonmark:commonmark-ext-yaml-front-matter:$commonMarkVersion")
    implementation("org.commonmark:commonmark-ext-image-attributes:$commonMarkVersion")
    implementation("org.commonmark:commonmark-ext-heading-anchor:$commonMarkVersion")
    val composeBom = platform("androidx.compose:compose-bom:2023.08.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.activity:activity-compose")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.foundation:foundation-layout")
    implementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    val coroutinesVersion = "1.7.1"
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    implementation("eu.crydee:syllable-counter:4.0.2")
    androidTestImplementation("androidx.compose.ui:ui-test:1.6.0")
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test:monitor:1.6.1")
    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("org.hamcrest:hamcrest-core:1.3")
    implementation("androidx.activity:activity-ktx:1.8.0")
    implementation("androidx.activity:activity:1.8.0")
    implementation("androidx.compose.animation:animation-core:1.6.0")
    implementation("androidx.compose.animation:animation:1.6.0")
    implementation("androidx.compose.material:material-icons-core:1.6.0")
    implementation("androidx.compose.ui:ui-graphics:1.6.0")
    implementation("androidx.compose.ui:ui-text:1.6.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.0")
    implementation("androidx.compose.ui:ui-unit:1.6.0")
    implementation("androidx.core:core:1.13.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.2")
    implementation("androidx.navigation:navigation-common:2.7.2")
    implementation("androidx.navigation:navigation-runtime:2.7.2")
    implementation("androidx.preference:preference:1.2.1")
    implementation("ch.acra:acra-core:5.11.3")
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation(project(":core"))
}

fladle {
    variant.set("playDebug")
    useOrchestrator.set(true)
    environmentVariables.put("clearPackageData", "true")
    testTimeout.set("7m")
    devices.add(
            mapOf("model" to "NexusLowRes", "version" to "29")
    )
    projectId.set("simplemarkdown")
}

tasks.register<Exec>("pullLogFiles") {
    commandLine = listOf(
        "adb", "pull",
        "/storage/emulated/0/Android/data/com.wbrawner.simplemarkdown/files/logs"
    )
}