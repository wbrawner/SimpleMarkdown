import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.Properties

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
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
    val navigationVersion = "2.7.2"
    implementation("androidx.navigation:navigation-fragment-ktx:$navigationVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navigationVersion")
    implementation("androidx.navigation:navigation-compose:$navigationVersion")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.2.1")
    val espressoVersion = "3.5.1"
    androidTestImplementation("androidx.test.espresso:espresso-core:$espressoVersion")
    androidTestImplementation("androidx.test.espresso:espresso-web:$espressoVersion")
    androidTestImplementation("androidx.test.espresso:espresso-intents:$espressoVersion")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestUtil("androidx.test:orchestrator:1.4.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.wbrawner.plausible:plausible-android:0.1.0-SNAPSHOT")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.fragment:fragment-ktx:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.legacy:legacy-support-v13:1.0.0")
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.browser:browser:1.6.0")
    val composeBom = platform("androidx.compose:compose-bom:2023.08.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.activity:activity-compose")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.foundation:foundation-layout")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    val coroutinesVersion = "1.7.1"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    val lifecycleVersion = "2.2.0"
    implementation("androidx.lifecycle:lifecycle-extensions:$lifecycleVersion")
    kapt("androidx.lifecycle:lifecycle-common-java8:$lifecycleVersion")
    implementation("eu.crydee:syllable-counter:4.0.2")
}

android.productFlavors.forEach { flavor ->
    if (gradle.startParameter.taskRequests.toString().lowercase(Locale.getDefault()).contains(flavor.name)
        && flavor.name == "play"
    ) {
        apply(plugin = "com.google.gms.google-services")
        apply(plugin = "com.google.firebase.crashlytics")

        dependencies {
            implementation("com.android.billingclient:billing:6.0.1")
            implementation("com.google.android.play:core-ktx:1.8.1")
            implementation("com.google.firebase:firebase-crashlytics:18.4.1")
        }
    }
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