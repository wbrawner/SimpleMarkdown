package com.wbrawner.simplemarkdown.view.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.wbrawner.simplemarkdown.MarkdownApplication
import com.wbrawner.simplemarkdown.R
import com.wbrawner.simplemarkdown.presentation.MarkdownPresenter
import com.wbrawner.simplemarkdown.utility.ErrorHandler
import kotlinx.coroutines.*
import java.io.File
import java.io.FileNotFoundException
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class SplashActivity : AppCompatActivity(), CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Main

    @Inject
    lateinit var presenter: MarkdownPresenter

    @Inject
    lateinit var errorHandler: ErrorHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        (application as MarkdownApplication).component.inject(this)
        if (sharedPreferences.getBoolean(getString(R.string.error_reports_enabled), true)) {
            errorHandler.init(this)
        }

        val darkModeValue = sharedPreferences.getString(
                getString(R.string.pref_key_dark_mode),
                getString(R.string.pref_value_auto)
        )

        var darkMode = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            AppCompatDelegate.MODE_NIGHT_AUTO
        } else {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        if (darkModeValue != null && !darkModeValue.isEmpty()) {
            if (darkModeValue.equals(getString(R.string.pref_value_light), ignoreCase = true)) {
                darkMode = AppCompatDelegate.MODE_NIGHT_NO
            } else if (darkModeValue.equals(getString(R.string.pref_value_dark), ignoreCase = true)) {
                darkMode = AppCompatDelegate.MODE_NIGHT_YES
            }
        }
        AppCompatDelegate.setDefaultNightMode(darkMode)

        if (intent?.data != null) {
            launch {
                presenter.loadFromUri(applicationContext, intent.data!!)
            }
        } else {
            presenter.fileName = "Untitled.md"
            val autosave = File(filesDir, "autosave.md")
            if (autosave.exists()) {
                try {
                    launch {
                        presenter.loadMarkdown(
                                "Untitled.md",
                                autosave.inputStream(),
                                true
                        )
                        autosave.delete()
                    }
                } catch (ignored: FileNotFoundException) {
                    return
                }
            }
        }

        val startIntent = Intent(this, MainActivity::class.java)
        startActivity(startIntent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext[Job]?.let {
            cancel()
        }
    }
}
