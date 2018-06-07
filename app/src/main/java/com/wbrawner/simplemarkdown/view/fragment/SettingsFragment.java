package com.wbrawner.simplemarkdown.view.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wbrawner.simplemarkdown.R;
import com.wbrawner.simplemarkdown.utility.Constants;
import com.wbrawner.simplemarkdown.utility.Utils;
import com.wbrawner.simplemarkdown.view.activity.ExplorerActivity;

import java.io.File;

import static android.app.Activity.RESULT_OK;

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
        Preference defaultRoot = findPreference(Constants.KEY_DOCS_PATH);
        defaultRoot.setSummary(Utils.getDocsPath(getActivity()));
        defaultRoot.setOnPreferenceClickListener((preference) -> {
            Intent intent = new Intent(getActivity(), ExplorerActivity.class);
            intent.putExtra(Constants.EXTRA_REQUEST_CODE, Constants.REQUEST_ROOT_DIR);
            startActivityForResult(intent, Constants.REQUEST_ROOT_DIR);
            return true;
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.preference_list_fragment_safe, container, false);
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
        int index = 0;
        try {
            index = Integer.valueOf(storedValue);
        } catch (NumberFormatException e) {
            // TODO: Report this?
            Log.e("SimpleMarkdown", "Unable to parse " + storedValue + " to integer");
        }
        String summary = listPreference.getEntries()[index].toString();
        preference.setSummary(summary);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK || data == null) {
            // If the user cancelled the request, then we don't care about the response
            return;
        }

        switch (requestCode) {
            case Constants.REQUEST_ROOT_DIR:
                File root = (File) data.getSerializableExtra(Constants.EXTRA_FILE);
                if (root == null) {
                    // TODO: Report this?
//                    Crashlytics.logException(new RuntimeException("Got null/empty response from setting default root dir"));
                    return;
                }
                Preference defaultRoot = findPreference(Constants.KEY_DOCS_PATH);
                defaultRoot.setSummary(root.getAbsolutePath());
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .edit()
                        .putString(Constants.KEY_DOCS_PATH, root.getAbsolutePath())
                        .apply();
                break;
        }
    }
}
