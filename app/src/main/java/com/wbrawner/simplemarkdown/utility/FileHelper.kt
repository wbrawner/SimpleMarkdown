package com.wbrawner.simplemarkdown.utility

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.net.toUri
import com.wbrawner.simplemarkdown.utility.FileHelper.FileData
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
    suspend fun open(source: URI): FileData?

    /**
     * Saves the given content to the given path
     * @param path
     * @param content
     * @return The name of the saved file
     */
    suspend fun save(destination: URI, content: String): String

    data class FileData(val name: String, val type: String?, val content: String)
}

class AndroidFileHelper(private val context: Context) : FileHelper {
    override val defaultDirectory: File by lazy {
        context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: context.filesDir
    }

    override suspend fun open(source: URI): FileData? = withContext(Dispatchers.IO) {
        val uri = source.toString().toUri()
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            // We weren't granted the persistent read/write permission for this file.
            // TODO: Return whether or not we got the persistent permission in order to determine
            //      whether or not we should show this file in the recent files section
        }
        context.contentResolver.openFileDescriptor(uri, "r")
            ?.use {
                FileData(
                    name = uri.getName(context),
                    type = context.contentResolver.getType(uri),
                    content = FileInputStream(it.fileDescriptor).reader()
                        .use(Reader::readText)
                )
            }
    }

    override suspend fun save(destination: URI, content: String): String =
        withContext(Dispatchers.IO) {
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