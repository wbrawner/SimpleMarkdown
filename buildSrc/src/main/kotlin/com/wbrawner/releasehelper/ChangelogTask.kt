package com.wbrawner.releasehelper

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

private const val CHANGELOG_PATH = "src/play/play/release-notes/en-US/default.txt"

abstract class ChangelogTask @Inject constructor(objectFactory: ObjectFactory) : DefaultTask() {
    @get:OutputFile
    val changelogFile: RegularFileProperty = objectFactory.fileProperty()

    init {
        changelogFile.set(project.layout.projectDirectory.file(CHANGELOG_PATH))
    }

    @TaskAction
    fun execute() {
        val latestTag = "git describe --tags --abbrev=0".execute()
        val changelog = "git log --format=\"%B\" ${latestTag.first().trim()}..".execute()
        logger.info("Latest tag: $latestTag")
        logger.info("Changelog: ${changelog.joinToString("\n")}")
        changelogFile.get().asFile.writer().use { writer ->
            writer.write(
                changelog.joinToString("\n") { it.trim('"') }
            )
        }
    }

    private fun String.execute(): List<String> = ProcessBuilder()
        .command(this.split(" "))
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .start()
        .inputReader()
        .readLines()
}