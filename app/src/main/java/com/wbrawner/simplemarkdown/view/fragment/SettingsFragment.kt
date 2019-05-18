package com.wbrawner.simplemarkdown.view.fragment

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
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

class SettingsFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.pref_general)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        setListPreferenceSummary(
                sharedPreferences,
                findPreference(getString(R.string.pref_key_dark_mode))
        )
        if (!BuildConfig.ENABLE_CUSTOM_CSS) {
            preferenceScreen.removePreference(findPreference(getString(R.string.pref_custom_css)))
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
        val preference = findPreference(key)
        if (preference is ListPreference) {
            setListPreferenceSummary(sharedPreferences, preference)
        }
        if (preference.key == getString(R.string.pref_key_dark_mode)) {
            var darkMode: Int = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                AppCompatDelegate.MODE_NIGHT_AUTO
            } else {
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            val darkModeValue = sharedPreferences.getString(preference.key, null)
            if (darkModeValue != null && !darkModeValue.isEmpty()) {
                if (darkModeValue.equals(getString(R.string.pref_value_light), ignoreCase = true)) {
                    darkMode = AppCompatDelegate.MODE_NIGHT_NO
                } else if (darkModeValue.equals(getString(R.string.pref_value_dark), ignoreCase = true)) {
                    darkMode = AppCompatDelegate.MODE_NIGHT_YES
                }
            }
            AppCompatDelegate.setDefaultNightMode(darkMode)
            activity?.recreate()
        }
    }

    private fun setListPreferenceSummary(sharedPreferences: SharedPreferences, preference: Preference) {
        val listPreference = preference as ListPreference
        val storedValue = sharedPreferences.getString(preference.getKey(), null) ?: return
        val index = listPreference.findIndexOfValue(storedValue)
        if (index < 0) return
        preference.setSummary(listPreference.entries[index].toString())
    }
}
