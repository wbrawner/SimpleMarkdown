package com.wbrawner.simplemarkdown.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import com.wbrawner.simplemarkdown.R
import com.wbrawner.simplemarkdown.utility.getName
import com.wbrawner.simplemarkdown.view.fragment.MainFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.Reader
import java.util.concurrent.atomic.AtomicBoolean

const val PREF_KEY_AUTOSAVE_URI = "autosave.uri"

class MarkdownViewModel : ViewModel() {
    val fileName = MutableLiveData<String?>("Untitled.md")
    val markdownUpdates = MutableLiveData<String>()
    val editorActions = MutableLiveData<EditorAction>()
    val uri = MutableLiveData<Uri?>()
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

    suspend fun autosave(context: Context, sharedPrefs: SharedPreferences) {
        val isAutoSaveEnabled = sharedPrefs.getBoolean(MainFragment.KEY_AUTOSAVE, true)
        if (!isDirty.get() || !isAutoSaveEnabled) {
            return
        }

        val uri = if (save(context)) {
            Log.d("SimpleMarkdown", "Saving file from onPause")
            uri.value
        } else {
            // The user has left the app, with autosave enabled, and we don't already have a
            // Uri for them or for some reason we were unable to save to the original Uri. In
            // this case, we need to just save to internal file storage so that we can recover
            val fileUri = Uri.fromFile(File(context.filesDir, fileName.value?: "Untitled.md"))
            Log.d("SimpleMarkdown", "Saving file from onPause failed, trying again")
            if (save(context, fileUri)) {
                fileUri
            } else {
                null
            }
        } ?: return
        sharedPrefs.edit()
                .putString(PREF_KEY_AUTOSAVE_URI, uri.toString())
                .apply()
    }

    fun reset(untitledFileName: String) {
        fileName.postValue(untitledFileName)
        uri.postValue(null)
        markdownUpdates.postValue("")
        editorActions.postValue(EditorAction.Load(""))
        isDirty.set(false)
    }

    fun shouldPromptSave() = isDirty.get()

    sealed class EditorAction {
        val consumed = AtomicBoolean(false)

        data class Load(val markdown: String) : EditorAction()
    }
}
