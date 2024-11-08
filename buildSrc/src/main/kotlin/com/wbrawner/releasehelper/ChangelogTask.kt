package com.wbrawner.releasehelper

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

private const val CHANGELOG_PATH = "src/play/play/release-notes/en-US/default.txt"

abstract class ChangelogTask @Inject constructor(
    objectFactory: ObjectFactory,
    providers: ProviderFactory,
) : DefaultTask() {
    @get:OutputFile
    val changelogFile: RegularFileProperty = objectFactory.fileProperty()

    @get:Input
    @Suppress("UnstableApiUsage")
    val latestTag: String = providers.exec {
        commandLine("git" , "describe", "--tags", "--abbrev=0")
    }.standardOutput.asText.get()

    init {
        changelogFile.set(project.layout.projectDirectory.file(CHANGELOG_PATH))
    }

    @TaskAction
    fun execute() {
        val changelog = "git log --format=\"%B\" ${latestTag.trim()}..".execute()
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
