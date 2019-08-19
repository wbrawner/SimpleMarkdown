package com.wbrawner.simplemarkdown.view.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProviders
import com.wbrawner.simplemarkdown.MarkdownApplication
import com.wbrawner.simplemarkdown.R
import com.wbrawner.simplemarkdown.viewmodel.MarkdownViewModel
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class SplashActivity : AppCompatActivity(), CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Main

    lateinit var viewModel: MarkdownViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(
                this,
                (application as MarkdownApplication).viewModelFactory
        ).get(MarkdownViewModel::class.java)

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
                            AppCompatDelegate.MODE_NIGHT_AUTO
                        } else {
                            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                        }
                    }
                }

            }

            AppCompatDelegate.setDefaultNightMode(darkMode)
            withContext(Dispatchers.IO) {
                var uri = intent?.data
                if (uri == null) {
                    uri = PreferenceManager.getDefaultSharedPreferences(this@SplashActivity)
                            .getString(
                                    getString(R.string.pref_key_autosave_uri),
                                    null
                            )?.let {
                                Uri.parse(it)
                            }
                }

                viewModel.load(this@SplashActivity, uri)
            }
            val startIntent = Intent(this@SplashActivity, MainActivity::class.java)
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
