package com.wbrawner.releasehelper

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.provideDelegate
import java.io.ByteArrayOutputStream
import java.io.File

class ReleaseHelperPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.tasks.register("getLatestTag", Exec::class.java) {
            val latestTag = ByteArrayOutputStream()
            standardOutput = latestTag
            commandLine("git describe --tags --abbrev=0".split(" "))
            doLast {
                target.project.extra["latestTag"] = latestTag.toString().trim()
                logger.info("Latest tag: ${target.project.extra["latestTag"]}")
            }
        }

        target.tasks.register("changelog") {
            val changelogFile = File(target.projectDir, "src/main/play/release-notes/en-US/default.txt")
            inputs.property("tag", target.provider {
                target.project.extra["latestTag"]
            })
            outputs.file(changelogFile)
            dependsOn("getLatestTag")
            doLast {
                val latestTag: String by target.project.extra
                val changelog = ByteArrayOutputStream()
                target.exec {
                    standardOutput = changelog
                    commandLine = "git log --format=\"%B\" ${latestTag.trim()}..".split(" ")
                }
                changelogFile.writeText(
                        changelog.toString()
                                .split("\n")
                                .mapNotNull { it.trim('"').ifBlank { null } }
                                .joinToString("\n") { "- $it" }
                )
            }
        }

        target.tasks.register("majorRelease") {
            dependsOn("changelog", "getLatestTag")
            doLast {
                val latestTag: String by target.project.extra
                val newVersion = latestTag.incrementVersion(VersionPart.MAJOR)
                target.updateVersionName(latestTag, newVersion)
                target.exec {
                    commandLine = "git tag $newVersion".split(" ")
                }
            }
        }

        target.tasks.register("minorRelease") {
            dependsOn("changelog", "getLatestTag")
            doLast {
                val latestTag: String by target.project.extra
                val newVersion = latestTag.incrementVersion(VersionPart.MAJOR)
                target.updateVersionName(latestTag, newVersion)
                target.exec {
                    commandLine = "git tag $newVersion".split(" ")
                }
            }
        }

        target.tasks.register("patchRelease") {
            dependsOn("changelog", "getLatestTag")
            doLast {
                val latestTag: String by target.project.extra
                val newVersion = latestTag.incrementVersion(VersionPart.MAJOR)
                target.updateVersionName(latestTag, newVersion)
                target.exec {
                    commandLine = "git tag $newVersion".split(" ")
                }
            }
        }
    }
}

private enum class VersionPart {
    MAJOR,
    MINOR,
    PATCH
}

private fun String.incrementVersion(part: VersionPart) = split(".")
        .mapIndexed { index, numberString ->
            val number = numberString.toInt()
            return@mapIndexed if (index == part.ordinal) {
                number + 1
            } else if (index > part.ordinal) {
                0
            } else {
                number
            }
        }
        .joinToString(".")

private fun Project.updateVersionName(oldVersionName: String, newVersionName: String) {
    File(projectDir, "build.gradle.kts").apply {
        writeText(
                readText().replace("versionName = \"$oldVersionName\"", "versionName = \"$newVersionName\"")
        )
    }
}