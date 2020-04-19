package com.wbrawner.simplemarkdown

import android.app.Application
import android.content.Context
import android.os.StrictMode
import androidx.preference.PreferenceManager
import com.wbrawner.simplemarkdown.utility.AcraErrorHandler
import com.wbrawner.simplemarkdown.utility.ErrorHandler

class MarkdownApplication : Application() {
    val errorHandler: ErrorHandler by lazy {
        AcraErrorHandler()
    }

    override fun onCreate() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())
        }
        super.onCreate()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        val enableErrorLogging = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.pref_key_error_reports_enabled), true)
        if (enableErrorLogging) errorHandler.init(this)
    }
}
