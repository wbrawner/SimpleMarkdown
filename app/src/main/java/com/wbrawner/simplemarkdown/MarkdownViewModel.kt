package com.wbrawner.simplemarkdown

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.wbrawner.simplemarkdown.core.LocalOnlyException
import com.wbrawner.simplemarkdown.model.Readability
import com.wbrawner.simplemarkdown.utility.FileHelper
import com.wbrawner.simplemarkdown.utility.Preference
import com.wbrawner.simplemarkdown.utility.PreferenceHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.net.URI

data class EditorState(
    val fileName: String = "Untitled.md",
    val markdown: TextFieldValue = TextFieldValue(""),
    val path: URI? = null,
    val toast: ParameterizedText? = null,
    val alert: AlertDialogModel? = null,
    val saveCallback: (() -> Unit)? = null,
    val lockSwiping: Boolean = false,
    val enableReadability: Boolean = false,
    val initialMarkdown: String = "",
) {
    val dirty: Boolean
        get() = markdown.text != initialMarkdown
}

class MarkdownViewModel(
    private val fileHelper: FileHelper,
    private val preferenceHelper: PreferenceHelper
) : ViewModel() {
    private val _state = MutableStateFlow(EditorState())
    val state = _state.asStateFlow()
    private val saveMutex = Mutex()

    init {
        preferenceHelper.observe<Boolean>(Preference.LOCK_SWIPING)
            .onEach {
                updateState { copy(lockSwiping = it) }
            }
            .launchIn(viewModelScope)
        preferenceHelper.observe<Boolean>(Preference.READABILITY_ENABLED)
            .onEach {
                updateState {
                    copy(
                        enableReadability = it,
                        markdown = markdown.copy(annotatedString = markdown.text.annotate(it)),
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun updateMarkdown(markdown: String?) = updateMarkdown(TextFieldValue(markdown.orEmpty()))

    fun updateMarkdown(markdown: TextFieldValue) {
        updateState {
            copy(
                markdown = markdown.copy(annotatedString = markdown.text.annotate(enableReadability)),
            )
        }
    }

    fun dismissToast() {
        updateState { copy(toast = null) }
    }

    fun dismissAlert() {
        updateState { copy(alert = null) }
    }

    private fun unsetSaveCallback() {
        updateState { copy(saveCallback = null) }
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
                        updateState {
                            copy(
                                path = uri,
                                fileName = name,
                                markdown = TextFieldValue(content),
                                initialMarkdown = content,
                                toast = ParameterizedText(R.string.file_loaded, arrayOf(name))
                            )
                        }
                        preferenceHelper[Preference.AUTOSAVE_URI] = actualLoadPath
                    } ?: throw IllegalStateException("Opened file was null")
            } catch (e: Exception) {
                Timber.e(LocalOnlyException(e), "Failed to open file at path: $actualLoadPath")
                updateState {
                    copy(
                        alert = AlertDialogModel(
                            text = ParameterizedText(R.string.file_load_error),
                            confirmButton = AlertDialogModel.ButtonModel(
                                ParameterizedText(R.string.ok),
                                onClick = ::dismissAlert
                            )
                        )
                    )
                }
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
                        updateState {
                            copy(saveCallback = ::unsetSaveCallback)
                        }
                    }
                    return@withLock false
                }
            try {
                Timber.i("Saving file to $actualSavePath...")
                val currentState = _state.value
                val name = fileHelper.save(actualSavePath, currentState.markdown.text)
                updateState {
                    currentState.copy(
                        fileName = name,
                        path = actualSavePath,
                        initialMarkdown = currentState.markdown.text,
                        toast = if (interactive) ParameterizedText(
                            R.string.file_saved,
                            arrayOf(name)
                        ) else null
                    )
                }
                Timber.i("Saved file $name to uri $actualSavePath")
                Timber.i("Persisting autosave uri in shared prefs: $actualSavePath")
                preferenceHelper[Preference.AUTOSAVE_URI] = actualSavePath
                true
            } catch (e: Exception) {
                Timber.e(e, "Failed to save file to $actualSavePath")
                updateState {
                    copy(
                        alert = AlertDialogModel(
                            text = ParameterizedText(R.string.file_save_error),
                            confirmButton = AlertDialogModel.ButtonModel(
                                text = ParameterizedText(R.string.ok),
                                onClick = ::dismissAlert
                            )
                        )
                    )
                }
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
                fileHelper.save(file, _state.value.markdown.text)
                preferenceHelper[Preference.AUTOSAVE_URI] = file
            }
        }
    }

    fun reset(untitledFileName: String, force: Boolean = false) {
        Timber.i("Resetting view model to default state")
        if (!force && _state.value.dirty) {
            updateState {
                copy(alert = AlertDialogModel(
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
            }
            return
        }
        updateState {
            EditorState(
                fileName = untitledFileName,
                lockSwiping = preferenceHelper[Preference.LOCK_SWIPING] as Boolean
            )
        }
        Timber.i("Removing autosave uri from shared prefs")
        preferenceHelper[Preference.AUTOSAVE_URI] = null
    }

    fun setLockSwiping(enabled: Boolean) {
        preferenceHelper[Preference.LOCK_SWIPING] = enabled
    }

    private fun updateState(block: EditorState.() -> EditorState) {
        _state.value = _state.value.block()
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

private fun String.annotate(enableReadability: Boolean): AnnotatedString {
    if (!enableReadability) return AnnotatedString(this)
    val readability = Readability(this)
    val annotated = AnnotatedString.Builder(this)
    for (sentence in readability.sentences()) {
        var color = Color.Transparent
        if (sentence.syllableCount() > 25) color = Color(229, 232, 42, 100)
        if (sentence.syllableCount() > 35) color = Color(193, 66, 66, 100)
        annotated.addStyle(SpanStyle(background = color), sentence.start(), sentence.end())
    }
    return annotated.toAnnotatedString()
}