package com.wbrawner.simplemarkdown.view.fragment

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

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
                findPreference(getString(R.string.key_default_view))
        )
        if (!BuildConfig.ENABLE_CUSTOM_CSS) {
            preferenceScreen.removePreference(findPreference(getString(R.string.pref_custom_css)))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.preference_list_fragment_safe, container, false)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val preference = findPreference(key)

        if (preference is ListPreference) {
            setListPreferenceSummary(sharedPreferences, preference)
        }
    }

    private fun setListPreferenceSummary(sharedPreferences: SharedPreferences, preference: Preference) {
        val listPreference = preference as ListPreference
        val storedValue = sharedPreferences.getString(preference.getKey(), "")
        if (storedValue!!.isEmpty()) {
            return
        }
        var index = 0
        try {
            index = Integer.valueOf(storedValue)
        } catch (e: NumberFormatException) {
            // TODO: Report this?
            Log.e("SimpleMarkdown", "Unable to parse $storedValue to integer")
        }

        val summary = listPreference.entries[index].toString()
        preference.setSummary(summary)
    }
}
