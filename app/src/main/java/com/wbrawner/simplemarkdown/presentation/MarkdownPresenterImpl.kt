package com.wbrawner.simplemarkdown.presentation

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.provider.OpenableColumns
import com.commonsware.cwac.anddown.AndDown
import com.wbrawner.simplemarkdown.model.MarkdownFile
import com.wbrawner.simplemarkdown.utility.ErrorHandler
import com.wbrawner.simplemarkdown.view.MarkdownEditView
import com.wbrawner.simplemarkdown.view.MarkdownPreviewView
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarkdownPresenterImpl @Inject
constructor(private val errorHandler: ErrorHandler) : MarkdownPresenter {
    private val fileLock = Any()
    private var file: MarkdownFile? = null
    @Volatile
    private var editView: MarkdownEditView? = null
    @Volatile
    private var previewView: MarkdownPreviewView? = null
    private val fileHandler = Handler()

    override var fileName: String
        get() = synchronized(fileLock) {
            return file!!.name
        }
        set(name) = synchronized(fileLock) {
            file!!.name = name
        }

    override var markdown: String
        get() = synchronized(fileLock) {
            return file!!.content
        }
        set(markdown) = synchronized(fileLock) {
            file!!.content = markdown
        }

    init {
        synchronized(fileLock) {
            this.file = MarkdownFile()
        }
    }

    override fun loadMarkdown(
            fileName: String,
            `in`: InputStream,
            listener: MarkdownPresenter.FileLoadedListener?,
            replaceCurrentFile: Boolean
    ) {
        fileHandler.post {
            val tmpFile = MarkdownFile()
            if (tmpFile.load(fileName, `in`)) {
                if (listener != null) {
                    val html = generateHTML(tmpFile.content)
                    listener.onSuccess(html)
                }
                if (replaceCurrentFile) {
                    synchronized(fileLock) {
                        this.file = tmpFile
                        val currentEditView = editView
                        if (currentEditView != null) {
                            currentEditView.onFileLoaded(true)
                            currentEditView.setTitle(fileName)
                            currentEditView.markdown = this.file!!.content
                            onMarkdownEdited(null)
                        }
                    }
                }
            } else {
                listener?.onError()
            }
        }
    }

    override fun newFile(newName: String) {
        synchronized(fileLock) {
            val currentEditView = editView
            if (currentEditView != null) {
                file!!.content = currentEditView.markdown
                currentEditView.setTitle(newName)
                currentEditView.markdown = ""
            }
            file = MarkdownFile(newName, "")
        }
    }

    override fun setEditView(editView: MarkdownEditView) {
        this.editView = editView
        onMarkdownEdited(null)
    }

    override fun setPreviewView(previewView: MarkdownPreviewView) {
        this.previewView = previewView
    }

    override fun saveMarkdown(listener: MarkdownPresenter.MarkdownSavedListener, name: String, outputStream: OutputStream) {
        val fileSaver = {
            val result: Boolean
            synchronized(fileLock) {
                result = file!!.save(name, outputStream)
            }
            listener?.saveComplete(result)
            val currentEditView = editView
            if (currentEditView != null) {
                synchronized(fileLock) {
                    currentEditView.setTitle(file!!.name)
                }
                currentEditView.onFileSaved(result)
            }
        }
        fileHandler.post(fileSaver)
    }

    override fun onMarkdownEdited(markdown: String?) {
        this.markdown = markdown ?: file?.content ?: ""
        fileHandler.post {
            val currentPreviewView = previewView
            currentPreviewView?.updatePreview(generateHTML(null))
        }
    }

    override fun generateHTML(markdown: String?): String {
        val andDown = AndDown()
        val HOEDOWN_FLAGS = AndDown.HOEDOWN_EXT_STRIKETHROUGH or AndDown.HOEDOWN_EXT_TABLES or
                AndDown.HOEDOWN_EXT_UNDERLINE or AndDown.HOEDOWN_EXT_SUPERSCRIPT or
                AndDown.HOEDOWN_EXT_FENCED_CODE
        return andDown.markdownToHtml(markdown, HOEDOWN_FLAGS, 0)
    }

    override fun loadFromUri(context: Context, fileUri: Uri) {
        try {
            val `in` = context.contentResolver.openInputStream(fileUri)
            var fileName: String? = null
            if ("content" == fileUri.scheme) {
                val retCur = context.contentResolver
                        .query(fileUri, null, null, null, null)
                if (retCur != null) {
                    val nameIndex = retCur
                            .getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    retCur.moveToFirst()
                    fileName = retCur.getString(nameIndex)
                    retCur.close()
                }
            } else if ("file" == fileUri.scheme) {
                fileName = fileUri.lastPathSegment
            }
            if (fileName == null) {
                fileName = "Untitled.md"
            }
            loadMarkdown(fileName, `in`!!, null, true)
        } catch (e: Exception) {
            errorHandler.reportException(e)
            val currentEditView = editView
            currentEditView?.onFileLoaded(false)
        }

    }
}
