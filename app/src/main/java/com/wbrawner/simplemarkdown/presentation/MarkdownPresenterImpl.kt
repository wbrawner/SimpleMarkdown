package com.wbrawner.simplemarkdown.presentation

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.commonsware.cwac.anddown.AndDown
import com.wbrawner.simplemarkdown.model.MarkdownFile
import com.wbrawner.simplemarkdown.utility.ErrorHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarkdownPresenterImpl @Inject constructor(private val errorHandler: ErrorHandler) : MarkdownPresenter {
    @Volatile
    private var file: MarkdownFile = MarkdownFile()
    @Volatile
    override var editView: MarkdownEditView? = null
        set(value) {
            field = value
            onMarkdownEdited(null)
        }
    @Volatile
    override var previewView: MarkdownPreviewView? = null

    override var fileName: String
        get() = file.name
        set(name) {
            file.name = name
        }

    override var markdown: String
        get() = file.content
        set(markdown) {
            file.content = markdown
        }

    override suspend fun loadMarkdown(
            fileName: String,
            `in`: InputStream,
            replaceCurrentFile: Boolean
    ): String? {
        val tmpFile = MarkdownFile()
        withContext(Dispatchers.IO) {
            if (!tmpFile.load(fileName, `in`)) {
                throw RuntimeException("Failed to load markdown")
            }
        }
        if (replaceCurrentFile) {
            this.file = tmpFile
            editView?.let {
                it.onFileLoaded(true)
                it.setTitle(fileName)
                it.markdown = file.content
                onMarkdownEdited(file.content)
            }
        }
        return generateHTML(tmpFile.content)
    }

    override fun newFile(newName: String) {
        editView?.let {
            file.content = it.markdown
            it.setTitle(newName)
            it.markdown = ""
        }
        file = MarkdownFile(newName, "")
    }

    override suspend fun saveMarkdown(name: String, outputStream: OutputStream): Boolean {
        val result = withContext(Dispatchers.IO) {
            file.save(name, outputStream)
        }
        editView?.let {
            it.setTitle(file.name)
            it.onFileSaved(result)
        }
        return result
    }

    override fun onMarkdownEdited(markdown: String?) {
        this.markdown = markdown ?: file.content
        previewView?.updatePreview(generateHTML(this.markdown))
    }

    override fun generateHTML(markdown: String): String {
        return AndDown().markdownToHtml(markdown, HOEDOWN_FLAGS, 0)
    }

    override suspend fun loadFromUri(context: Context, fileUri: Uri): String? {
        return try {
            var fileName: String? = null
            if ("content" == fileUri.scheme) {
                context.contentResolver
                        .query(
                                fileUri,
                                null,
                                null,
                                null,
                                null
                        )
                        ?.use {
                            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            it.moveToFirst()
                            fileName = it.getString(nameIndex)
                        }
            } else if ("file" == fileUri.scheme) {
                fileName = fileUri.lastPathSegment
            }
            val inputStream = context.contentResolver.openInputStream(fileUri) ?: return null
            loadMarkdown(fileName ?: "Untitled.md", inputStream, true)
        } catch (e: Exception) {
            errorHandler.reportException(e)
            editView?.onFileLoaded(false)
            null
        }
    }

    companion object {
        const val HOEDOWN_FLAGS = AndDown.HOEDOWN_EXT_STRIKETHROUGH or AndDown.HOEDOWN_EXT_TABLES or
                AndDown.HOEDOWN_EXT_UNDERLINE or AndDown.HOEDOWN_EXT_SUPERSCRIPT or
                AndDown.HOEDOWN_EXT_FENCED_CODE
    }
}
