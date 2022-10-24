package com.wbrawner.simplemarkdown.view.fragment

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.wbrawner.simplemarkdown.BuildConfig
import com.wbrawner.simplemarkdown.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsFragment
    : PreferenceFragmentCompat(),
        SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_general)
        if (BuildConfig.DEBUG) {
            preferenceScreen.addPreference(Preference(preferenceScreen.context).apply {
                title = "Force crash"
                onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    throw RuntimeException("Forced crash from settings")
                }
            })
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        lifecycleScope.launch(context = Dispatchers.IO) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
            sharedPreferences.registerOnSharedPreferenceChangeListener(this@SettingsFragment)
            (findPreference(getString(R.string.pref_key_dark_mode)) as? ListPreference)?.let {
                setListPreferenceSummary(sharedPreferences, it)
            }
            @Suppress("ConstantConditionIf")
            if (!BuildConfig.ENABLE_CUSTOM_CSS) {
                findPreference<Preference>(getString(R.string.pref_custom_css))?.let {
                    preferenceScreen.removePreference(it)
                }
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (!isAdded) return
        val preference = findPreference(key) as? ListPreference ?: return
        setListPreferenceSummary(sharedPreferences, preference)
        if (preference.key != getString(R.string.pref_key_dark_mode)) {
            return
        }
        var darkMode: Int = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
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
