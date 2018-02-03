package com.wbrawner.simplemarkdown.view.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.wbrawner.simplemarkdown.R;

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        setListPreferenceSummary(
                sharedPreferences,
                findPreference(getString(R.string.key_default_view))
        );
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);

        if (preference instanceof ListPreference) {
            setListPreferenceSummary(sharedPreferences, preference);
        }
    }

    private void setListPreferenceSummary(SharedPreferences sharedPreferences, Preference preference) {
        ListPreference listPreference = (ListPreference) preference;
        String storedValue = sharedPreferences.getString(preference.getKey(), "");
        if (storedValue.isEmpty()) {
            return;
        }
        int index = Integer.valueOf(storedValue);
        String summary = listPreference.getEntries()[index].toString();
        preference.setSummary(summary);
    }
}
