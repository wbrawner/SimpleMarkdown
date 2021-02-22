package com.wbrawner.simplemarkdown.view.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.wbrawner.simplemarkdown.R
import kotlinx.coroutines.*
import timber.log.Timber

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val darkMode = withContext(Dispatchers.IO) {
                val darkModeValue = PreferenceManager.getDefaultSharedPreferences(this@SplashActivity)
                        .getString(
                                getString(R.string.pref_key_dark_mode),
                                getString(R.string.pref_value_auto)
                        )

                return@withContext when {
                    darkModeValue.equals(getString(R.string.pref_value_light), ignoreCase = true) -> AppCompatDelegate.MODE_NIGHT_NO
                    darkModeValue.equals(getString(R.string.pref_value_dark), ignoreCase = true) -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> {
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                        } else {
                            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                        }
                    }
                }

            }

            AppCompatDelegate.setDefaultNightMode(darkMode)
            val uri = intent?.data?.let {
                Timber.d("Using uri from intent: $it")
                it
            } ?: run {
                Timber.d("No intent provided to load data from")
                null
            }

            val startIntent = Intent(this@SplashActivity, MainActivity::class.java)
                    .apply {
                        data = uri
                    }
            startActivity(startIntent)
            finish()
        }
    }
}
