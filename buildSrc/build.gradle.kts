plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
}

gradlePlugin {
    plugins {
        register("release-helper") {
            id = "com.wbrawner.releasehelper"
            implementationClass = "com.wbrawner.releasehelper.ReleaseHelperPlugin"
        }
    }
}