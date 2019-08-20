package com.wbrawner.simplemarkdown.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wbrawner.simplemarkdown.utility.getName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream
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
            context.contentResolver.openFileDescriptor(uri, "r")?.use {
                val fileInput = FileInputStream(it.fileDescriptor)
                val fileName = uri.getName(context)
                return@withContext if (load(fileInput)) {
                    this@MarkdownViewModel.fileName.postValue(fileName)
                    this@MarkdownViewModel.uri.postValue(uri)
                    true
                } else {
                    false
                }
            } ?: false
        }
    }

    suspend fun load(inputStream: InputStream): Boolean {
        return try {
            withContext(coroutineContext) {
                val content = inputStream.reader().use(Reader::readText)
                originalMarkdown.postValue(content)
                markdownUpdates.postValue(content)
            }
            true
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }

    suspend fun save(context: Context, givenUri: Uri? = this.uri.value): Boolean {
        val uri = givenUri ?: this.uri.value ?: return false
        return withContext(Dispatchers.IO) {
            val fileName = uri.getName(context)
            val outputStream = context.contentResolver.openOutputStream(uri)
                    ?: return@withContext false
            if (save(outputStream)) {
                this@MarkdownViewModel.fileName.postValue(fileName)
                this@MarkdownViewModel.uri.postValue(uri)
                true
            } else {
                false
            }
        }
    }

    suspend fun save(outputStream: OutputStream): Boolean {
        return try {
            withContext(coroutineContext) {
                outputStream.writer().use {
                    it.write(markdownUpdates.value)
                }
            }
            true
        } catch (e: Throwable) {
            false
        }
    }

    fun reset(untitledFileName: String) {
        fileName.postValue(untitledFileName)
        originalMarkdown.postValue("")
        markdownUpdates.postValue("")
    }
}

class MarkdownViewModelFactory : ViewModelProvider.Factory {
    private val markdownViewModel: MarkdownViewModel by lazy {
        MarkdownViewModel()
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return markdownViewModel as T
    }
}
