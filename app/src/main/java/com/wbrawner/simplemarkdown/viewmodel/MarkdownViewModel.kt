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
import kotlin.coroutines.CoroutineContext

class MarkdownViewModel : ViewModel() {
    private val coroutineContext: CoroutineContext = Dispatchers.IO
    val fileName = MutableLiveData<String>().apply {
        postValue("Untitled.md")
    }
    val markdownUpdates = MutableLiveData<String>()
    val originalMarkdown = MutableLiveData<String>()
    val uri = MutableLiveData<Uri>()

    fun updateMarkdown(markdown: String?) {
        this.markdownUpdates.postValue(markdown ?: "")
    }

    suspend fun load(context: Context, uri: Uri?): Boolean {
        if (uri == null) return false
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use {
                    val fileInput = FileInputStream(it.fileDescriptor)
                    val fileName = uri.getName(context)
                    val content = fileInput.reader().use(Reader::readText)
                    originalMarkdown.postValue(content)
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
                val outputStream = context.contentResolver.openOutputStream(uri)
                        ?: return@withContext false
                outputStream.writer().use {
                    it.write(markdownUpdates.value ?: "")
                }
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
        originalMarkdown.postValue("")
        markdownUpdates.postValue("")
    }
}
