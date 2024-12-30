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
    operator fun get(preference: Preference): Any?

    operator fun set(preference: Preference, value: Any?)

    fun <T> observe(preference: Preference): StateFlow<T>
}

class AndroidPreferenceHelper(context: Context, private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)): PreferenceHelper {
    private val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }
    private val states by lazy {
        val allPrefs: Map<String, Any?> = sharedPreferences.all
        Preference.entries.associateWith { preference ->
            MutableStateFlow(allPrefs[preference.key] ?: preference.default)
        }
    }

    override fun get(preference: Preference): Any? = states[preference]?.value

    override fun set(preference: Preference, value: Any?) {
        sharedPreferences.edit {
            when (value) {
                is Boolean -> putBoolean(preference.key, value)
                is Float -> putFloat(preference.key, value)
                is Int -> putInt(preference.key, value)
                is Long -> putLong(preference.key, value)
                is String -> putString(preference.key, value)
                null -> remove(preference.key)
            }
        }
        coroutineScope.launch {
            states[preference]!!.emit(value)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> observe(preference: Preference): StateFlow<T> = states[preference]!!.asStateFlow() as StateFlow<T>
}

enum class Preference(val key: String, val default: Any?) {
    AMOLED_DARK_THEME("amoled_dark_theme", false),
    ANALYTICS_ENABLED("analytics.enable", true),
    AUTOSAVE_ENABLED("autosave", true),
    AUTOSAVE_URI("autosave.uri", null),
    CUSTOM_CSS("pref.custom_css", null),
    DARK_MODE("darkMode", "Auto"),
    ERROR_REPORTS_ENABLED("acra.enable", true),
    LOCK_SWIPING("lockSwiping", false),
    READABILITY_ENABLED("readability.enable", false)
}