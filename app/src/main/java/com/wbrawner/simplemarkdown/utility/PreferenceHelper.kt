package com.wbrawner.simplemarkdown.utility

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface PreferenceHelper {
    operator fun <T> get(preference: Preference<T>): T

    operator fun <T> set(preference: Preference<T>, value: T)

    fun <T> observe(preference: Preference<T>): StateFlow<T>
}

class AndroidPreferenceHelper(
    context: Context,
    private val coroutineScope: CoroutineScope = CoroutineScope(
        Dispatchers.IO
    )
) : PreferenceHelper {
    private val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }
    private val states: Map<String, ObservablePreference<*>> by lazy {
        val allPrefs: Map<String, Any?> = sharedPreferences.all
        listOf(
            Preference.AmoledDarkTheme,
            Preference.AnalyticsEnabled,
            Preference.AutosaveEnabled,
            Preference.AutosaveUri,
            Preference.CustomCSS,
            Preference.DarkMode,
            Preference.ErrorReportsEnabled,
            Preference.LockSwiping,
            Preference.Readability,
        ).associate { preference ->
            preference.key to ObservablePreference.create(preference, allPrefs)
        }
    }

    override fun <T> get(preference: Preference<T>): T =
        states[preference.key]?.value as? T ?: preference.default

    override fun <T> set(preference: Preference<T>, value: T) {
        sharedPreferences.edit {
            when (value) {
                is Boolean -> putBoolean(preference.key, value)
                is Float -> putFloat(preference.key, value)
                is Int -> putInt(preference.key, value)
                is Long -> putLong(preference.key, value)
                is String -> putString(preference.key, value)
                null -> remove(preference.key)
                else -> error("Unhandled preference type for key $preference")
            }
        }
        coroutineScope.launch {
            val observablePreference: ObservablePreference<T> =
                states[preference.key] as ObservablePreference<T>
            observablePreference.flow.emit(value)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> observe(preference: Preference<T>): StateFlow<T> =
        states[preference.key]!!.flow.asStateFlow() as StateFlow<T>
}

sealed interface Preference<T> {
    val key: String
    val default: T

    data object AmoledDarkTheme : Preference<Boolean> {
        override val key: String = "amoled_dark_theme"
        override val default: Boolean = false
    }

    data object AnalyticsEnabled : Preference<Boolean> {
        override val key: String = "analytics.enable"
        override val default: Boolean = true
    }

    data object AutosaveEnabled : Preference<Boolean> {
        override val key: String = "autosave"
        override val default: Boolean = true
    }

    data object AutosaveUri : Preference<String?> {
        override val key: String = "autosave.uri"
        override val default: String? = null
    }

    data object CustomCSS : Preference<String?> {
        override val key: String = "pref.custom_css"
        override val default: String? = null
    }

    data object DarkMode : Preference<String> {
        override val key: String = "DarkMode"
        override val default: String = "auto"
    }

    data object ErrorReportsEnabled : Preference<Boolean> {
        override val key: String = "acra.enable"
        override val default: Boolean = true
    }

    data object LockSwiping : Preference<Boolean> {
        override val key: String = "lockSwiping"
        override val default: Boolean = false
    }

    data object Readability : Preference<Boolean> {
        override val key: String = "readability.enable"
        override val default: Boolean = false
    }
}

data class ObservablePreference<T>(val preference: Preference<T>, val flow: MutableStateFlow<T>) {
    val value: T
        get() = flow.value

    companion object {
        fun <T> create(
            preference: Preference<T>,
            sharedPrefsMap: Map<String, Any?>
        ): ObservablePreference<T> {
            val defaultValue = sharedPrefsMap[preference.key] as? T ?: preference.default
            return ObservablePreference(
                preference,
                MutableStateFlow(defaultValue)
            )
        }
    }
}