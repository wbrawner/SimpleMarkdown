package com.wbrawner.simplemarkdown.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wbrawner.simplemarkdown.utility.getName
import com.wbrawner.simplemarkdown.view.fragment.MainFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.Reader
import java.util.concurrent.atomic.AtomicBoolean

const val PREF_KEY_AUTOSAVE_URI = "autosave.uri"

class MarkdownViewModel(val timber: Timber.Tree = Timber.asTree()) : ViewModel() {
    val fileName = MutableLiveData<String?>("Untitled.md")
    val markdownUpdates = MutableLiveData<String>()
    val editorActions = MutableLiveData<EditorAction>()
    val uri = MutableLiveData<Uri?>()
    private val isDirty = AtomicBoolean(false)
    private val saveMutex = Mutex()

    fun updateMarkdown(markdown: String?) {
        this.markdownUpdates.postValue(markdown ?: "")
        isDirty.set(true)
    }

    suspend fun load(context: Context, uri: Uri?): Boolean {
        if (uri == null) {
            timber.i("Ignoring call to load null uri")
            return false
        }
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use {
                    val fileInput = FileInputStream(it.fileDescriptor)
                    val fileName = uri.getName(context)
                    val content = fileInput.reader().use(Reader::readText)
                    if (content.isBlank()) {
                        // If we don't get anything back, then we can assume that reading the file failed
                        timber.i("Ignoring load for empty file $fileName from $fileInput")
                        return@withContext false
                    }
                    editorActions.postValue(EditorAction.Load(content))
                    markdownUpdates.postValue(content)
                    this@MarkdownViewModel.fileName.postValue(fileName)
                    this@MarkdownViewModel.uri.postValue(uri)
                    timber.i("Loaded file $fileName from $fileInput")
                    timber.v("File contents:\n$content")
                    isDirty.set(false)
                    true
                } ?: run {
                    timber.w("Open file descriptor returned null for uri: $uri")
                    false
                }
            } catch (e: Exception) {
                timber.e(e, "Failed to open file descriptor for uri: $uri")
                false
            }
        }
    }

    suspend fun save(context: Context, givenUri: Uri? = null): Boolean = saveMutex.withLock {
        val uri = givenUri?.let {
            timber.i("Saving file with given uri: $it")
            it
        } ?: this.uri.value?.let {
            timber.i("Saving file with cached uri: $it")
            it
        } ?: run {
            timber.w("Save called with no uri")
            return@save false
        }
        return withContext(Dispatchers.IO) {
            try {
                val fileName = uri.getName(context)
                context.contentResolver.openOutputStream(uri, "rwt")
                        ?.writer()
                        ?.use {
                            it.write(markdownUpdates.value ?: "")
                        }
                        ?: run {
                            timber.w("Open output stream returned null for uri: $uri")
                            return@withContext false
                        }
                this@MarkdownViewModel.fileName.postValue(fileName)
                this@MarkdownViewModel.uri.postValue(uri)
                isDirty.set(false)
                timber.i("Saved file $fileName to uri $uri")
                true
            } catch (e: Exception) {
                timber.e(e, "Failed to save file at uri: $uri")
                false
            }
        }
    }

    suspend fun autosave(context: Context, sharedPrefs: SharedPreferences) {
        if (saveMutex.isLocked) {
            timber.i("Ignoring autosave since manual save is already in progress")
            return
        }
        val isAutoSaveEnabled = sharedPrefs.getBoolean(MainFragment.KEY_AUTOSAVE, true)
        timber.d("Autosave called. isEnabled? $isAutoSaveEnabled")
        if (!isDirty.get() || !isAutoSaveEnabled) {
            timber.i("Ignoring call to autosave. Contents haven't changed or autosave not enabled")
            return
        }

        val uri = if (save(context)) {
            timber.i("Autosave with cached uri succeeded: ${uri.value}")
            uri.value
        } else {
            // The user has left the app, with autosave enabled, and we don't already have a
            // Uri for them or for some reason we were unable to save to the original Uri. In
            // this case, we need to just save to internal file storage so that we can recover
            val fileUri = Uri.fromFile(File(context.filesDir, fileName.value ?: "Untitled.md"))
            timber.i("No cached uri for autosave, saving to $fileUri instead")
            if (save(context, fileUri)) {
                fileUri
            } else {
                null
            }
        } ?: run {
            timber.w("Unable to perform autosave, uri was null")
            return@autosave
        }
        timber.i("Persisting autosave uri in shared prefs: $uri")
        sharedPrefs.edit()
                .putString(PREF_KEY_AUTOSAVE_URI, uri.toString())
                .apply()
    }

    fun reset(untitledFileName: String) {
        timber.i("Resetting view model to default state")
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
