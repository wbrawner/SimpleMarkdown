package com.wbrawner.simplemarkdown.presentation

import android.content.Context
import android.net.Uri

import com.wbrawner.simplemarkdown.view.MarkdownEditView
import com.wbrawner.simplemarkdown.view.MarkdownPreviewView

import java.io.InputStream
import java.io.OutputStream

interface MarkdownPresenter {
    var fileName: String
    var markdown: String
    fun loadMarkdown(fileName: String, `in`: InputStream)
    fun loadFromUri(context: Context, fileUri: Uri)

    fun loadMarkdown(fileName: String, `in`: InputStream, listener: FileLoadedListener,
                     replaceCurrentFile: Boolean)

    fun newFile(newName: String)
    fun setEditView(editView: MarkdownEditView)
    fun setPreviewView(previewView: MarkdownPreviewView)

    fun saveMarkdown(listener: MarkdownSavedListener, name: String, outputStream: OutputStream)
    fun onMarkdownEdited()
    fun onMarkdownEdited(markdown: String)
    fun generateHTML(): String
    fun generateHTML(markdown: String): String

    interface FileLoadedListener {
        fun onSuccess(markdown: String)

        fun onError()
    }

    interface MarkdownSavedListener {
        fun saveComplete(success: Boolean)
    }
}
