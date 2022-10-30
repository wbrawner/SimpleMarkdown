package com.wbrawner.simplemarkdown.view.activity

import android.content.Context
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import com.wbrawner.plausible.android.Plausible
import com.wbrawner.simplemarkdown.R

class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val preferences = mutableMapOf<String, String>()
        preferences["Autosave"] = sharedPreferences.getBoolean("autosave", true).toString()
        val usingCustomCss = !getStringPref(R.string.pref_custom_css, null).isNullOrBlank()
        preferences["Custom CSS"] = usingCustomCss.toString()
        val darkModeSetting = getStringPref(R.string.pref_key_dark_mode, "auto").toString()
        preferences["Dark Mode"] = darkModeSetting
        preferences["Error Reports"] =
            getBooleanPref(R.string.pref_key_error_reports_enabled, true).toString()
        preferences["Readability"] = getBooleanPref(R.string.readability_enabled, false).toString()
        Plausible.event("settings", props = preferences, url = "/")
    }

    override fun onBackPressed() {
        if (!findNavController(R.id.content).navigateUp()) {
            super.onBackPressed()
        }
    }
}

fun Context.getBooleanPref(@StringRes key: Int, defaultValue: Boolean) = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
    getString(key),
    defaultValue
)

fun Context.getStringPref(@StringRes key: Int, defaultValue: String?) = PreferenceManager.getDefaultSharedPreferences(this).getString(
    getString(key),
    defaultValue
)