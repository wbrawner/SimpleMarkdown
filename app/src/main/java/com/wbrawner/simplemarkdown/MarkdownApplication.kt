package com.wbrawner.simplemarkdown

import android.app.Application
import android.os.StrictMode
import androidx.preference.PreferenceManager
import com.wbrawner.simplemarkdown.utility.ErrorHandler
import com.wbrawner.simplemarkdown.utility.SentryErrorHandler

class MarkdownApplication : Application() {
    val errorHandler: ErrorHandler by lazy {
        SentryErrorHandler()
    }

    override fun onCreate() {
        val enableErrorReports = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.error_reports_enabled), true)
        errorHandler.init(this, enableErrorReports)
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
}
