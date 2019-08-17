package com.wbrawner.simplemarkdown.presentation

import android.content.Context
import android.net.Uri

import java.io.InputStream
import java.io.OutputStream

interface MarkdownPresenter {
    var fileName: String
    var markdown: String
    var editView: MarkdownEditView?
    var previewView: MarkdownPreviewView?
    suspend fun loadFromUri(context: Context, fileUri: Uri): String?
    suspend fun loadMarkdown(
            fileName: String,
            `in`: InputStream,
            replaceCurrentFile: Boolean = true
    ): String?
    fun newFile(newName: String)
    suspend fun saveMarkdown(name: String, outputStream: OutputStream): Boolean
    fun onMarkdownEdited(markdown: String? = null)
    fun generateHTML(markdown: String = ""): String
}

interface MarkdownEditView {
    var markdown: String
    fun setTitle(title: String)

    fun onFileSaved(success: Boolean)

    fun onFileLoaded(success: Boolean)
}

interface MarkdownPreviewView {
    fun updatePreview(html: String)
}


