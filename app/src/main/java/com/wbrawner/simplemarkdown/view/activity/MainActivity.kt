package com.wbrawner.simplemarkdown.view.activity

import android.content.Context
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import com.wbrawner.simplemarkdown.R
import com.wbrawner.simplemarkdown.utility.AnalyticsHelper
import com.wbrawner.simplemarkdown.utility.init

class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {
    private val analyticsHelper = AnalyticsHelper.init(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        analyticsHelper.setUserProperty("autosave", sharedPreferences.getBoolean("autosave", true).toString())
        val usingCustomCss = !getStringPref(R.string.pref_custom_css, null).isNullOrBlank()
        analyticsHelper.setUserProperty("using_custom_css", usingCustomCss.toString())
        val darkModeSetting = getStringPref(R.string.pref_key_dark_mode, "auto").toString()
        analyticsHelper.setUserProperty("dark_mode", darkModeSetting)
        analyticsHelper.setUserProperty("error_reports_enabled", getBooleanPref(R.string.pref_key_error_reports_enabled, true).toString())
        analyticsHelper.setUserProperty("readability_enabled", getBooleanPref(R.string.readability_enabled, false).toString())
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