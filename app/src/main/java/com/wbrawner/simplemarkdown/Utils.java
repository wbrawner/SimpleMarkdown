package com.wbrawner.simplemarkdown;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.wbrawner.simplemarkdown.view.activity.SettingsActivity;

/**
 * Created by billy on 1/15/2018.
 */

public class Utils {

    public static String getDocsPath(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(
                SettingsActivity.KEY_DOCS_PATH,
                Environment.getExternalStorageDirectory() + "/" +
                        Environment.DIRECTORY_DOCUMENTS + "/"
        );
    }

    public static boolean isAutosaveEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(
                "autosave",
                true
        );
    }

}
