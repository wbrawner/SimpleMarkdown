package com.wbrawner.simplemarkdown.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.wbrawner.simplemarkdown.utility.getName
import com.wbrawner.simplemarkdown.view.activity.KEY_AUTOSAVE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
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
    val fileName = MutableStateFlow("Untitled.md")
    val markdownUpdates = MutableStateFlow("")
    val editorActions = MutableLiveData<EditorAction>()
    val uri = MutableLiveData<Uri?>()
    private val isDirty = AtomicBoolean(false)
    private val saveMutex = Mutex()

    init {
        markdownUpdates
    }

    fun updateMarkdown(markdown: String?) = viewModelScope.launch {
        markdownUpdates.emit(markdown ?: "")
        isDirty.set(true)
    }

    suspend fun load(
            context: Context,
            uri: Uri?,
            sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    ): Boolean {
        if (uri == null) {
            timber.i("No URI provided to load, attempting to load last autosaved file")
            sharedPrefs.getString(PREF_KEY_AUTOSAVE_URI, null)
                    ?.let {
                        Timber.d("Using uri from shared preferences: $it")
                        return load(context, Uri.parse(it), sharedPrefs)
                    } ?: return false
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
                    markdownUpdates.emit(content)
                    this@MarkdownViewModel.fileName.emit(fileName)
                    this@MarkdownViewModel.uri.postValue(uri)
                    timber.i("Loaded file $fileName from $fileInput")
                    timber.v("File contents:\n$content")
                    isDirty.set(false)
                    timber.i("Persisting autosave uri in shared prefs: $uri")
                    sharedPrefs.edit()
                            .putString(PREF_KEY_AUTOSAVE_URI, uri.toString())
                            .apply()
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

    suspend fun save(
            context: Context,
            givenUri: Uri? = null,
            sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    ): Boolean = saveMutex.withLock {
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
                this@MarkdownViewModel.fileName.emit(fileName)
                this@MarkdownViewModel.uri.postValue(uri)
                isDirty.set(false)
                timber.i("Saved file $fileName to uri $uri")
                timber.i("Persisting autosave uri in shared prefs: $uri")
                sharedPrefs.edit()
                        .putString(PREF_KEY_AUTOSAVE_URI, uri.toString())
                        .apply()
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
        val isAutoSaveEnabled = sharedPrefs.getBoolean(KEY_AUTOSAVE, true)
        timber.d("Autosave called. isEnabled? $isAutoSaveEnabled")
        if (!isDirty.get() || !isAutoSaveEnabled) {
            timber.i("Ignoring call to autosave. Contents haven't changed or autosave not enabled")
            return
        }

        if (save(context)) {
            timber.i("Autosave with cached uri succeeded: ${uri.value}")
        } else {
            // The user has left the app, with autosave enabled, and we don't already have a
            // Uri for them or for some reason we were unable to save to the original Uri. In
            // this case, we need to just save to internal file storage so that we can recover
            val fileUri = Uri.fromFile(File(context.filesDir, fileName.value ?: "Untitled.md"))
            timber.i("No cached uri for autosave, saving to $fileUri instead")
            save(context, fileUri)
        }
    }

    fun reset(untitledFileName: String, sharedPrefs: SharedPreferences) = viewModelScope.launch{
        timber.i("Resetting view model to default state")
        fileName.tryEmit(untitledFileName)
        uri.postValue(null)
        markdownUpdates.emit("")
        editorActions.postValue(EditorAction.Load(""))
        isDirty.set(false)
        timber.i("Removing autosave uri from shared prefs")
        sharedPrefs.edit {
            remove(PREF_KEY_AUTOSAVE_URI)
        }
    }

    fun shouldPromptSave() = isDirty.get()

    sealed class EditorAction {
        val consumed = AtomicBoolean(false)

        data class Load(val markdown: String) : EditorAction()
    }
}
