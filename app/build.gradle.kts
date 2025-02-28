import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.fladle)
    alias(libs.plugins.triplet.play)
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
    keystoreProperties["publishCredentialsFile"] = ""
}

android {
    packaging {
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
    compileSdk = libs.versions.maxSdk.get().toInt()
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    defaultConfig {
        applicationId = "com.wbrawner.simplemarkdown"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.maxSdk.get().toInt()
        versionCode = 48
        versionName = "2025.1.0"
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
    playConfigs {
        register("play") {
            enabled.set(true)
            commit.set(true)
        }
    }
    lint {
        disable += listOf(
            "AndroidGradlePluginVersion",
            "GradleDependency",
            "ObsoleteLintCustomCheck"
        )
        warningsAsErrors = true
    }
}

play {
    commit.set(false)
    enabled.set(false)
    track.set("production")
    defaultToAppBundles.set(true)
    (keystoreProperties["publishCredentialsFile"] as? String)?.ifBlank { null }?.let {
        serviceAccountCredentials.set(file(it))
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    "freeImplementation"(project(":free"))
    "playImplementation"(project(":non-free"))
    implementation(libs.androidx.material3.windowsizeclass)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)
    testRuntimeOnly(libs.robolectric)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.espresso.web)
    androidTestImplementation(libs.androidx.espresso.intents)
    androidTestRuntimeOnly(libs.androidx.runner)
    androidTestUtil(libs.androidx.orchestrator)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.browser)
    implementation(libs.commonmark)
    implementation(libs.commonmark.ext.gfm.tables)
    implementation(libs.commonmark.ext.gfm.strikethrough)
    implementation(libs.commonmark.ext.autolink)
    implementation(libs.commonmark.ext.task.list.items)
    implementation(libs.commonmark.ext.yaml.front.matter)
    implementation(libs.commonmark.ext.image.attributes)
    implementation(libs.commonmark.ext.heading.anchor)
    val composeBom = enforcedPlatform(libs.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    runtimeOnly(libs.kotlinx.coroutines.android)
    testImplementation(libs.kotlinx.coroutines.test)
    implementation(libs.syllable.counter)
    androidTestImplementation(libs.androidx.ui.test)
    androidTestImplementation(libs.androidx.core)
    androidTestImplementation(libs.androidx.monitor)
    androidTestImplementation(libs.junit)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.animation.core)
    implementation(libs.androidx.animation)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.text)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.ui.unit)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.common)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.acra.core)
    implementation(libs.timber)
    implementation(libs.kotlinx.coroutines.core)
    implementation(project(":core"))
}

fladle {
    variant.set("playDebug")
    useOrchestrator.set(true)
    environmentVariables.put("clearPackageData", "true")
    testTimeout.set("7m")
    devices.add(
        mapOf("model" to "Pixel2.arm", "version" to "33")
    )
    projectId.set("simplemarkdown")
}

tasks.register<Exec>("pullLogFiles") {
    commandLine = listOf(
        "adb", "pull",
        "/storage/emulated/0/Android/data/com.wbrawner.simplemarkdown/files/logs"
    )
}