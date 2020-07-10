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

class MarkdownViewModel : ViewModel() {
    val fileName = MutableLiveData<String>("Untitled.md")
    val editorCommands = MutableLiveData<EditorCommand>()
    val markdown = MutableLiveData<String>()
    val uri = MutableLiveData<Uri>()

    fun updateMarkdown(markdown: String?) {
        this.markdown.postValue(markdown ?: "")
    }

    suspend fun load(context: Context, uri: Uri?): Boolean {
        if (uri == null) return false
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use {
                    val fileInput = FileInputStream(it.fileDescriptor)
                    val fileName = uri.getName(context)
                    val content = fileInput.reader().use(Reader::readText)
                    markdown.postValue(content)
                    editorCommands.postValue(EditorCommand.Load(content))
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
                            it.write(markdown.value ?: "")
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
        editorCommands.postValue(EditorCommand.Load(""))
        markdown.postValue("")
    }

    fun undo() {
        editorCommands.postValue(EditorCommand.Undo())
    }

    fun redo() {
        editorCommands.postValue(EditorCommand.Redo())
    }
}

sealed class EditorCommand {
    val consumed = false

    class Undo: EditorCommand()
    class Redo: EditorCommand()
    class Load(val text: String): EditorCommand()
}
