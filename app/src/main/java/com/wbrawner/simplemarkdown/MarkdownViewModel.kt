package com.wbrawner.simplemarkdown

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.wbrawner.simplemarkdown.utility.FileHelper
import com.wbrawner.simplemarkdown.utility.Preference
import com.wbrawner.simplemarkdown.utility.PreferenceHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.net.URI

data class EditorState(
    val fileName: String = "Untitled.md",
    val markdown: String = "",
    val path: URI? = null,
    val toast: ParameterizedText? = null,
    val alert: AlertDialogModel? = null,
    val saveCallback: (() -> Unit)? = null,
    /**
     * Used to signal to the view that it should reload due to an external change, like loading
     * a new file
     */
    val reloadToggle: Int = 0,
    private val initialMarkdown: String = "",
) {
    val dirty: Boolean
        get() = markdown != initialMarkdown
}

class MarkdownViewModel(
    private val fileHelper: FileHelper,
    private val preferenceHelper: PreferenceHelper
) : ViewModel() {
    private val _state = MutableStateFlow(EditorState())
    val state = _state.asStateFlow()
    private val saveMutex = Mutex()

    init {
        viewModelScope.launch {
            load(null)
        }
    }

    fun updateMarkdown(markdown: String?) {
        _state.value = _state.value.copy(
            markdown = markdown ?: "",
        )
    }

    fun dismissToast() {
        _state.value = _state.value.copy(toast = null)
    }

    fun dismissAlert() {
        _state.value = _state.value.copy(alert = null)
    }

    private fun unsetSaveCallback() {
        _state.value = _state.value.copy(saveCallback = null)
    }

    suspend fun load(loadPath: String?) {
        saveMutex.withLock {
            val actualLoadPath = loadPath
                ?.ifBlank { null }
                ?: preferenceHelper[Preference.AUTOSAVE_URI]
                    ?.let {
                        val autosaveUri = it as? String
                        if (autosaveUri.isNullOrBlank()) {
                            preferenceHelper[Preference.AUTOSAVE_URI] = null
                            null
                        } else {
                            Timber.d("Using uri from shared preferences: $it")
                            autosaveUri
                        }
                    } ?: return
            Timber.d("Loading file at $actualLoadPath")
            try {
                val uri = URI.create(actualLoadPath)
                fileHelper.open(uri)
                    ?.let { (name, content) ->
                        val currentState = _state.value
                        _state.value = currentState.copy(
                            path = uri,
                            fileName = name,
                            markdown = content,
                            initialMarkdown = content,
                            reloadToggle = currentState.reloadToggle.inv(),
                            toast = ParameterizedText(R.string.file_loaded, arrayOf(name))
                        )
                        preferenceHelper[Preference.AUTOSAVE_URI] = actualLoadPath
                    } ?: throw IllegalStateException("Opened file was null")
            } catch (e: Exception) {
                Timber.e(e, "Failed to open file at path: $actualLoadPath")
                _state.value = _state.value.copy(
                    alert = AlertDialogModel(
                        text = ParameterizedText(R.string.file_load_error),
                        confirmButton = AlertDialogModel.ButtonModel(ParameterizedText(R.string.ok), onClick = ::dismissAlert)
                    )
                )
            }
        }
    }

    suspend fun save(savePath: URI? = null, interactive: Boolean = true): Boolean =
        saveMutex.withLock {
            val actualSavePath = savePath
                ?: _state.value.path
                ?: run {
                    Timber.w("Attempted to save file with empty path")
                    if (interactive) {
                        _state.value = _state.value.copy(saveCallback = ::unsetSaveCallback)
                    }
                    return@withLock false
                }
            try {
                Timber.i("Saving file to $actualSavePath...")
                val currentState = _state.value
                val name = fileHelper.save(actualSavePath, currentState.markdown)
                _state.value = currentState.copy(
                    fileName = name,
                    path = actualSavePath,
                    initialMarkdown = currentState.markdown,
                    toast = if (interactive) ParameterizedText(R.string.file_saved, arrayOf(name)) else null
                )
                Timber.i("Saved file $name to uri $actualSavePath")
                Timber.i("Persisting autosave uri in shared prefs: $actualSavePath")
                preferenceHelper[Preference.AUTOSAVE_URI] = actualSavePath
                true
            } catch (e: Exception) {
                Timber.e(e, "Failed to save file to $actualSavePath")
                _state.value = _state.value.copy(
                    alert = AlertDialogModel(
                        text = ParameterizedText(R.string.file_save_error),
                        confirmButton = AlertDialogModel.ButtonModel(
                            text = ParameterizedText(R.string.ok),
                            onClick = ::dismissAlert
                        )
                    )
                )
                false
            }
        }

    suspend fun autosave() {
        val isAutoSaveEnabled = preferenceHelper[Preference.AUTOSAVE_ENABLED] as Boolean
        if (!isAutoSaveEnabled) {
            Timber.i("Ignoring autosave as autosave not enabled")
            return
        }
        if (!_state.value.dirty) {
            Timber.d("Ignoring autosave as contents haven't changed")
            return
        }
        if (saveMutex.isLocked) {
            Timber.i("Ignoring autosave since manual save is already in progress")
            return
        }
        Timber.d("Performing autosave")
        if (!save(interactive = false)) {
            withContext(Dispatchers.IO) {
                // The user has left the app, with autosave enabled, and we don't already have a
                // Uri for them or for some reason we were unable to save to the original Uri. In
                // this case, we need to just save to internal file storage so that we can recover
                val file = File(fileHelper.defaultDirectory, _state.value.fileName).toURI()
                Timber.i("No cached uri for autosave, saving to $file instead")
                // Here we call the fileHelper directly so that the file is still registered as dirty.
                // This prevents the user from ending up in a scenario where they've autosaved the file
                // to an internal storage location, thus marking it as not dirty, but no longer able to
                // access the file if the accidentally go to create a new file without properly saving
                // the current one
                fileHelper.save(file, _state.value.markdown)
                preferenceHelper[Preference.AUTOSAVE_URI] = file
            }
        }
    }

    fun reset(untitledFileName: String, force: Boolean = false) {
        Timber.i("Resetting view model to default state")
        if (!force && _state.value.dirty) {
            _state.value = _state.value.copy(alert = AlertDialogModel(
                text = ParameterizedText(R.string.prompt_save_changes),
                confirmButton = AlertDialogModel.ButtonModel(
                    text = ParameterizedText(R.string.yes),
                    onClick = {
                        _state.value = _state.value.copy(
                            saveCallback = {
                                reset(untitledFileName, false)
                            }
                        )
                    }
                ),
                dismissButton = AlertDialogModel.ButtonModel(
                    text = ParameterizedText(R.string.no),
                    onClick = {
                        reset(untitledFileName, true)
                    }
                )
            ))
            return
        }
        _state.value =
            EditorState(fileName = untitledFileName, reloadToggle = _state.value.reloadToggle.inv())
        Timber.i("Removing autosave uri from shared prefs")
        preferenceHelper[Preference.AUTOSAVE_URI] = null
    }

    companion object {
        fun factory(
            fileHelper: FileHelper,
            preferenceHelper: PreferenceHelper
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                return MarkdownViewModel(fileHelper, preferenceHelper) as T
            }
        }
    }
}

data class AlertDialogModel(
    val text: ParameterizedText,
    val confirmButton: ButtonModel,
    val dismissButton: ButtonModel? = null
) {
    data class ButtonModel(val text: ParameterizedText, val onClick: () -> Unit)
}

data class ParameterizedText(@StringRes val text: Int, val params: Array<Any> = arrayOf()) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParameterizedText

        if (text != other.text) return false
        if (!params.contentEquals(other.params)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = text
        result = 31 * result + params.contentHashCode()
        return result
    }
}