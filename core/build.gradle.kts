import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.Properties

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
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
    compileSdk = 34

    defaultConfig {
        minSdk = 23

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    val acraVersion = "5.11.3"
    api("ch.acra:acra-core:$acraVersion")
    implementation("ch.acra:acra-http:$acraVersion")
    implementation("ch.acra:acra-limiter:$acraVersion")
    implementation("ch.acra:acra-advanced-scheduler:$acraVersion")
    api("com.jakewharton.timber:timber:5.0.1")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}