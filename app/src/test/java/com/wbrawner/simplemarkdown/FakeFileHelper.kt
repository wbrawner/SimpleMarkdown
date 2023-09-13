package com.wbrawner.simplemarkdown

import com.wbrawner.simplemarkdown.utility.FileHelper
import java.io.File
import java.net.URI

class FakeFileHelper : FileHelper {
    override val defaultDirectory: File
        get() = File.createTempFile("sm", null)
            .apply {
                delete()
                mkdir()
            }

    var file: Pair<String, String> = "Untitled.md" to "This is a test file"
    var openedUris = ArrayDeque<URI>()
    var savedData = ArrayDeque<SavedData>()

    override suspend fun open(source: URI): Pair<String, String> {
        openedUris.addLast(source)
        return file
    }

    override suspend fun save(destination: URI, content: String): String {
        savedData.addLast(SavedData(destination, content))
        return file.first
    }
}

data class SavedData(val uri: URI, val content: String)