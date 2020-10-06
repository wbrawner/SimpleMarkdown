package com.wbrawner.simplemarkdown.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wbrawner.simplemarkdown.utility.getName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.Reader
import java.util.concurrent.atomic.AtomicBoolean

class MarkdownViewModel : ViewModel() {
    val fileName = MutableLiveData<String>("Untitled.md")
    val markdownUpdates = MutableLiveData<String>()
    val editorActions = MutableLiveData<EditorAction>()
    val uri = MutableLiveData<Uri>()
    private val isDirty = AtomicBoolean(false)

    fun updateMarkdown(markdown: String?) {
        this.markdownUpdates.postValue(markdown ?: "")
        isDirty.set(true)
    }

    suspend fun load(context: Context, uri: Uri?): Boolean {
        if (uri == null) return false
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use {
                    val fileInput = FileInputStream(it.fileDescriptor)
                    val fileName = uri.getName(context)
                    val content = fileInput.reader().use(Reader::readText)
                    if (content.isBlank()) {
                        // If we don't get anything back, then we can assume that reading the file failed
                        return@withContext false
                    }
                    isDirty.set(false)
                    editorActions.postValue(EditorAction.Load(content))
                    markdownUpdates.postValue(content)
                    this@MarkdownViewModel.fileName.postValue(fileName)
                    this@MarkdownViewModel.uri.postValue(uri)
                    true
                } ?: false
            } catch (ignored: Exception) {
                false
            }
        }
    }

    suspend fun save(context: Context, givenUri: Uri? = this.uri.value): Boolean {
        val uri = givenUri ?: this.uri.value ?: return false
        return withContext(Dispatchers.IO) {
            try {
                val fileName = uri.getName(context)
                context.contentResolver.openOutputStream(uri, "rwt")
                        ?.writer()
                        ?.use {
                            it.write(markdownUpdates.value ?: "")
                        }
                        ?: return@withContext false
                this@MarkdownViewModel.fileName.postValue(fileName)
                this@MarkdownViewModel.uri.postValue(uri)
                true
            } catch (ignored: Exception) {
                false
            }
        }
    }

    fun reset(untitledFileName: String) {
        fileName.postValue(untitledFileName)
        markdownUpdates.postValue("")
        isDirty.set(false)
    }

    fun shouldPromptSave() = isDirty.get()

    sealed class EditorAction {
        val consumed = AtomicBoolean(false)

        data class Load(val markdown: String) : EditorAction()
    }
}
