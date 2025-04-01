package com.wbrawner.simplemarkdown

import com.wbrawner.simplemarkdown.utility.FileHelper
import kotlinx.coroutines.delay
import java.io.File
import java.net.URI

class FakeFileHelper : FileHelper {
    override val defaultDirectory: File by lazy {
        File.createTempFile("simplemarkdown", null)
            .apply {
                delete()
                mkdir()
            }
    }

    var file: FileHelper.FileData = FileHelper.FileData(
        name = "Untitled.md",
        type = "text/plain",
        content = "This is a test file"
    )
    var openedUris = ArrayDeque<URI>()
    var savedData = ArrayDeque<SavedData>()
    @Volatile
    var errorOnOpen: Boolean = false
    @Volatile
    var errorOnSave: Boolean = false

    override suspend fun open(source: URI): FileHelper.FileData {
        delay(1000)
        if (errorOnOpen) error("errorOnOpen set to true")
        openedUris.addLast(source)
        return file
    }

    override suspend fun save(destination: URI, content: String): String {
        delay(1000)
        if (errorOnSave) error("errorOnSave set to true")
        savedData.addLast(SavedData(destination, content))
        return destination.path.substringAfterLast("/")
    }
}

data class SavedData(val uri: URI, val content: String)