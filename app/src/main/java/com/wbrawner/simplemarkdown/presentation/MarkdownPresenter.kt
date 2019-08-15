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
    fun loadFromUri(context: Context, fileUri: Uri)

    fun loadMarkdown(
            fileName: String,
            `in`: InputStream,
            listener: FileLoadedListener? = null,
            replaceCurrentFile: Boolean = true
    )

    fun newFile(newName: String)
    fun setEditView(editView: MarkdownEditView)
    fun setPreviewView(previewView: MarkdownPreviewView)
    fun saveMarkdown(listener: MarkdownSavedListener, name: String, outputStream: OutputStream)
    fun onMarkdownEdited(markdown: String? = null)
    fun generateHTML(markdown: String? = null): String

    interface FileLoadedListener {
        fun onSuccess(markdown: String)
        fun onError()
    }

    interface MarkdownSavedListener {
        fun saveComplete(success: Boolean)
    }
}
