package com.wbrawner.simplemarkdown.view.fragment

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import com.wbrawner.simplemarkdown.BuildConfig
import com.wbrawner.simplemarkdown.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import kotlin.coroutines.CoroutineContext

class SettingsFragment
    : PreferenceFragment(),
        SharedPreferences.OnSharedPreferenceChangeListener,
        CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        launch {
            withContext(Dispatchers.IO) {
                try {
                    // This can be thrown when recreating the activity for theme changes
                    addPreferencesFromResource(R.xml.pref_general)
                } catch (ignored: Exception) {
                    return@withContext
                }
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
                sharedPreferences.registerOnSharedPreferenceChangeListener(this@SettingsFragment)
                (findPreference(getString(R.string.pref_key_dark_mode)) as? ListPreference)?.let {
                    setListPreferenceSummary(sharedPreferences, it)
                }
                @Suppress("ConstantConditionIf")
                if (!BuildConfig.ENABLE_CUSTOM_CSS) {
                    preferenceScreen.removePreference(findPreference(getString(R.string.pref_custom_css)))
                }
            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.preference_list_fragment_safe, container, false)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (!isAdded) return
        val preference = findPreference(key) as? ListPreference ?: return
        setListPreferenceSummary(sharedPreferences, preference)
        if (preference.key != getString(R.string.pref_key_dark_mode)) {
            return
        }
        var darkMode: Int = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            AppCompatDelegate.MODE_NIGHT_AUTO
        } else {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        val darkModeValue = sharedPreferences.getString(preference.key, null)
        if (darkModeValue != null && darkModeValue.isNotEmpty()) {
            if (darkModeValue.equals(getString(R.string.pref_value_light), ignoreCase = true)) {
                darkMode = AppCompatDelegate.MODE_NIGHT_NO
            } else if (darkModeValue.equals(getString(R.string.pref_value_dark), ignoreCase = true)) {
                darkMode = AppCompatDelegate.MODE_NIGHT_YES
            }
        }
        AppCompatDelegate.setDefaultNightMode(darkMode)
        activity?.recreate()
    }

    private fun setListPreferenceSummary(sharedPreferences: SharedPreferences, preference: ListPreference) {
        val storedValue = sharedPreferences.getString(
                preference.key,
                null
        ) ?: return
        val index = preference.findIndexOfValue(storedValue)
        if (index < 0) return
        preference.summary = preference.entries[index].toString()
    }
}
