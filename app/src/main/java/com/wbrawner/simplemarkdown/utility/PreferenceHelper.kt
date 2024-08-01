package com.wbrawner.simplemarkdown.utility

import android.content.Context
import android.net.Uri
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException

interface PreferenceHelper {
    operator fun get(preference: Preference): Any?

    operator fun set(preference: Preference, value: Any?)

    fun <T> observe(preference: Preference): StateFlow<T>
}

class AndroidPreferenceHelper(context: Context, private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)): PreferenceHelper {
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val states = mapOf(
        Preference.ANALYTICS_ENABLED to MutableStateFlow(get(Preference.ANALYTICS_ENABLED)),
        Preference.AUTOSAVE_ENABLED to MutableStateFlow(get(Preference.AUTOSAVE_ENABLED)),
        Preference.AUTOSAVE_URI to MutableStateFlow(get(Preference.AUTOSAVE_URI)),
        Preference.CUSTOM_CSS to MutableStateFlow(get(Preference.CUSTOM_CSS)),
        Preference.DARK_MODE to MutableStateFlow(get(Preference.DARK_MODE)),
        Preference.ERROR_REPORTS_ENABLED to MutableStateFlow(get(Preference.ERROR_REPORTS_ENABLED)),
        Preference.READABILITY_ENABLED to MutableStateFlow(get(Preference.READABILITY_ENABLED)),
    )

    override fun get(preference: Preference): Any? = sharedPreferences.all[preference.key]?: preference.default

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

    override fun <T> observe(preference: Preference): StateFlow<T> = states[preference]!!.asStateFlow() as StateFlow<T>
}

enum class Preference(val key: String, val default: Any?) {
    ANALYTICS_ENABLED("analytics.enable", true),
    AUTOSAVE_ENABLED("autosave", true),
    AUTOSAVE_URI("autosave.uri", null),
    CUSTOM_CSS("pref.custom_css", null),
    DARK_MODE("darkMode", "Auto"),
    ERROR_REPORTS_ENABLED("acra.enable", true),
    READABILITY_ENABLED("readability.enable", false)
}