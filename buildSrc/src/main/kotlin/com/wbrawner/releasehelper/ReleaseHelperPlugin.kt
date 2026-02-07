package com.wbrawner.releasehelper

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.register
import java.io.File


class ReleaseHelperPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.tasks.register<ChangelogTask>("changelog")

        target.tasks.register(name = "majorRelease", type = Exec::class) {
            dependsOn("changelog", "getLatestTag")
            val latestTag: String by target.project.extra
            val newVersion = latestTag.incrementVersion(ReleaseType.MAJOR)
            target.updateVersionName(latestTag, newVersion)
            commandLine = "git tag $newVersion".split(" ")
        }

        target.tasks.register(name = "minorRelease", type = Exec::class) {
            dependsOn("changelog", "getLatestTag")
            val latestTag: String by target.project.extra
            val newVersion = latestTag.incrementVersion(ReleaseType.MINOR)
            target.updateVersionName(latestTag, newVersion)
            commandLine = "git tag $newVersion".split(" ")
        }

        target.tasks.register(name = "patchRelease", type = Exec::class) {
            dependsOn("changelog", "getLatestTag")
            val latestTag: String by target.project.extra
            val newVersion = latestTag.incrementVersion(ReleaseType.PATCH)
            target.updateVersionName(latestTag, newVersion)
            commandLine = "git tag $newVersion".split(" ")
        }
    }
}

private enum class ReleaseType {
    MAJOR,
    MINOR,
    PATCH
}

private fun String.incrementVersion(releaseType: ReleaseType) = split(".")
    .mapIndexed { index, numberString ->
        val number = numberString.toInt()
        return@mapIndexed if (index == releaseType.ordinal) {
            number + 1
        } else if (index > releaseType.ordinal) {
            0
        } else {
            number
        }
    }
    .joinToString(".")

private fun Project.updateVersionName(oldVersionName: String, newVersionName: String) {
    File(projectDir, "build.gradle.kts").apply {
        writeText(
            readText().replace(
                "versionName = \"$oldVersionName\"",
                "versionName = \"$newVersionName\""
            )
        )
    }
}