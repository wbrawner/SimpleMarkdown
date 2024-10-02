import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

val acraProperties = Properties()
try {
    val acraPropertiesFile = project.file("acra.properties")
    acraProperties.load(FileInputStream(acraPropertiesFile))
} catch (ignored: FileNotFoundException) {
    logger.warn("Unable to load ACRA properties. Error reporting won't be available")
    acraProperties["url"] = ""
    acraProperties["user"] = ""
    acraProperties["pass"] = ""
}

android {
    namespace = "com.wbrawner.simplemarkdown.core"
    compileSdk = libs.versions.maxSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "ACRA_URL", "\"${acraProperties["url"]}\"")
        buildConfigField("String", "ACRA_USER", "\"${acraProperties["user"]}\"")
        buildConfigField("String", "ACRA_PASS", "\"${acraProperties["pass"]}\"")
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
    kotlinOptions {
        jvmTarget = "11"
    }
    lint {
        disable += listOf("AndroidGradlePluginVersion", "GradleDependency")
        warningsAsErrors = true
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.acra.core)
    implementation(libs.acra.http)
    runtimeOnly(libs.acra.limiter)
    runtimeOnly(libs.acra.advanced.scheduler)
    api(libs.timber)
}