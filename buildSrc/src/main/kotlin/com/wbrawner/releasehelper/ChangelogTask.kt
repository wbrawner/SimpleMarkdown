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
    val latestTag: String = providers.exec {
        commandLine("git" , "describe", "--tags", "--abbrev=0")
    }.standardOutput.asText.get()

    init {
        changelogFile.set(project.layout.projectDirectory.file(CHANGELOG_PATH))
    }

    @TaskAction
    fun execute() {
        val gitLog = "git log --format=\"%al %B\" ${latestTag.trim()}..".execute()
        logger.info("Latest tag: $latestTag")
        logger.info("Changelog: ${gitLog.joinToString("\n")}")
        val humanUpdates = gitLog.mapNotNull { log ->
            val logParts = log.unquote().splitAuthor()
            if (logParts.size != 2) return@mapNotNull null
            val (author, message) = logParts
            when {
                author == "renovate-bot" -> null
                message.startsWith("fixup!") -> null
                message == "Bump version for release" -> null
                else -> "- $message"
            }
        }
        val botUpdates = gitLog.any { it.startsWith("\"renovate-bot") }
        val changelog = humanUpdates.plus(
            if (botUpdates) {
                "- Update dependencies"
            } else {
                ""
            }
        ).joinToString("\n")
        changelogFile.get().asFile.writer().use { writer ->
            writer.write(changelog)
        }
    }

    private fun String.asCommand() = this.split(" ")
        .fold(emptyList<String>()) { list, new ->
            if (list.lastOrNull()?.contains("\"") == true && new.contains("\"")) {
                list.subList(0, list.lastIndex).plus("${list.last()} $new")
            } else {
                list + new
            }
        }

    private fun String.execute(): List<String> = ProcessBuilder()
        .command(this.asCommand())
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .start()
        .inputReader()
        .readLines()

    private fun String.unquote() = trim('"')

    private fun String.splitAuthor() = split(" ", limit = 2)
}
