package com.wbrawner.simplemarkdown.view.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.wbrawner.simplemarkdown.R
import com.wbrawner.simplemarkdown.viewmodel.PREF_KEY_AUTOSAVE_URI
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class SplashActivity : AppCompatActivity(), CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launch {
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
            val uri = withContext(Dispatchers.IO) {
                intent?.data
                        ?: PreferenceManager.getDefaultSharedPreferences(this@SplashActivity)
                                .getString(PREF_KEY_AUTOSAVE_URI, null)
                                ?.let {
                                    Uri.parse(it)
                                }
            }

            val startIntent = Intent(this@SplashActivity, MainActivity::class.java)
                    .apply {
                        data = uri
                    }
            startActivity(startIntent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext[Job]?.let {
            cancel()
        }
    }
}
