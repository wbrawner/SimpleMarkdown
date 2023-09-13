package com.wbrawner.simplemarkdown

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.wbrawner.simplemarkdown.utility.FileHelper
import com.wbrawner.simplemarkdown.utility.Preference
import com.wbrawner.simplemarkdown.utility.PreferenceHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.io.File
import java.net.URI
import java.util.concurrent.atomic.AtomicBoolean

class MarkdownViewModel(
    private val fileHelper: FileHelper,
    private val preferenceHelper: PreferenceHelper
) : ViewModel() {
    private val _fileName = MutableStateFlow("Untitled.md")
    val fileName = _fileName.asStateFlow()
    private val _markdown = MutableStateFlow("")
    val markdown = _markdown.asStateFlow()
    private val path = MutableStateFlow<URI?>(null)
    private val isDirty = AtomicBoolean(false)
    private val _effects = MutableSharedFlow<Effect>()
    val effects = _effects.asSharedFlow()
    private val saveMutex = Mutex()

    suspend fun updateMarkdown(markdown: String?) {
        this@MarkdownViewModel._markdown.emit(markdown ?: "")
        isDirty.set(true)
    }

    suspend fun load(loadPath: String?) {
        if (loadPath.isNullOrBlank()) {
            Timber.i("No URI provided to load, attempting to load last autosaved file")
            preferenceHelper[Preference.AUTOSAVE_URI]
                ?.let {
                    val autosaveUri = it as? String
                    if (autosaveUri.isNullOrBlank()) {
                        preferenceHelper[Preference.AUTOSAVE_URI] = null
                    } else {
                        Timber.d("Using uri from shared preferences: $it")
                        load(autosaveUri)
                    }
                }
            return
        }
        try {
            val uri = URI.create(loadPath)
            fileHelper.open(uri)
                ?.let { (name, content) ->
                    path.emit(uri)
                    _effects.emit(Effect.ClearText)
                    _fileName.emit(name)
                    _markdown.emit(content)
                    isDirty.set(false)
                    preferenceHelper[Preference.AUTOSAVE_URI] = loadPath
                } ?: _effects.emit(Effect.Error("Failed to open file at path: $loadPath"))
        } catch (e: Exception) {
            Timber.e(e, "Failed to open file at path: $loadPath")
        }
    }

    suspend fun save(savePath: URI? = path.value, promptSavePath: Boolean = true): Boolean {
        return saveMutex.withLock {
            if (savePath == null) {
                Timber.w("Attempted to save file with empty path")
                if (promptSavePath) {
                    _effects.emit(Effect.OpenSaveDialog {})
                }
                return false
            }
            try {
                val name = fileHelper.save(savePath, markdown.value)
                _fileName.emit(name)
                path.emit(savePath)
                isDirty.set(false)
                Timber.i("Saved file ${fileName.value} to uri $savePath")
                Timber.i("Persisting autosave uri in shared prefs: $savePath")
                preferenceHelper[Preference.AUTOSAVE_URI] = savePath
                true
            } catch (e: Exception) {
                val message = "Failed to save file to $savePath"
                Timber.e(e, message)
                _effects.emit(Effect.Error(message))
                false
            }
        }
    }

    suspend fun autosave() {
        if (!isDirty.get()) {
            Timber.d("Ignoring autosave as contents haven't changed")
            return
        }
        if (saveMutex.isLocked) {
            Timber.i("Ignoring autosave since manual save is already in progress")
            return
        }
        val isAutoSaveEnabled = preferenceHelper[Preference.AUTOSAVE_ENABLED] as Boolean
        if (!isAutoSaveEnabled) {
            Timber.i("Ignoring autosave as autosave not enabled")
            return
        }
        if (!save(promptSavePath = false)) {
            // The user has left the app, with autosave enabled, and we don't already have a
            // Uri for them or for some reason we were unable to save to the original Uri. In
            // this case, we need to just save to internal file storage so that we can recover
            val file = File(fileHelper.defaultDirectory, fileName.value).toURI()
            Timber.i("No cached uri for autosave, saving to $file instead")
            save(file)
        }
    }

    suspend fun reset(untitledFileName: String, force: Boolean = false) {
        Timber.i("Resetting view model to default state")
        if (!force && isDirty.get()) {
            _effects.emit(Effect.Prompt(
                "Would you like to save your changes?",
                confirm = {
                    viewModelScope.launch {
                        _effects.emit(Effect.OpenSaveDialog {
                            reset(untitledFileName, false)
                        })
                    }
                },
                cancel = {
                    viewModelScope.launch {
                        reset(untitledFileName, true)
                    }
                }
            ))
            return
        }
        _fileName.emit(untitledFileName)
        _markdown.emit("")
        path.emit(null)
        _effects.emit(Effect.ClearText)
        isDirty.set(false)
        Timber.i("Removing autosave uri from shared prefs")
        preferenceHelper[Preference.AUTOSAVE_URI] = null
    }

    companion object {
        fun factory(fileHelper: FileHelper, preferenceHelper: PreferenceHelper): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                return MarkdownViewModel(fileHelper, preferenceHelper) as T
            }
        }
    }

    sealed interface Effect {
        data class OpenSaveDialog(val postSaveBlock: suspend () -> Unit) : Effect
        data class Prompt(val text: String, val confirm: () -> Unit, val cancel: () -> Unit) :
            Effect

        data object ClearText : Effect
        data class Error(val text: String) : Effect
    }
}
