package com.wbrawner.simplemarkdown.utility

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.Reader
import java.net.URI

interface FileHelper {
    val defaultDirectory: File

    /**
     * Opens a file at the given path
     * @param path The path of the file to open
     * @return A [Pair] of the file name to the file's contents
     */
    suspend fun open(source: URI): Pair<String, String>?

    /**
     * Saves the given content to the given path
     * @param path
     * @param content
     * @return The name of the saved file
     */
    suspend fun save(destination: URI, content: String): String
}

class AndroidFileHelper(private val context: Context) : FileHelper {
    override val defaultDirectory: File = context.filesDir

    override suspend fun open(source: URI): Pair<String, String>? = withContext(Dispatchers.IO) {
        val uri = source.toString().toUri()
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        context.contentResolver.openFileDescriptor(uri, "r")
            ?.use {
                uri.getName(context) to FileInputStream(it.fileDescriptor).reader()
                    .use(Reader::readText)
            }
    }

    override suspend fun save(destination: URI, content: String): String = withContext(Dispatchers.IO) {
        val uri = destination.toString().toUri()
        context.contentResolver.openOutputStream(uri, "rwt")
            ?.writer()
            ?.use {
                it.write(content)
            }
            ?: run {
                Timber.w("Open output stream returned null for uri: $uri")
                throw IOException("Failed to save to $destination")
            }
        return@withContext uri.getName(context)
    }
}