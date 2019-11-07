package com.wbrawner.simplemarkdown

import android.app.Application
import android.os.StrictMode
import com.wbrawner.simplemarkdown.utility.CrashlyticsErrorHandler
import com.wbrawner.simplemarkdown.utility.ErrorHandler
import com.wbrawner.simplemarkdown.viewmodel.MarkdownViewModelFactory

class MarkdownApplication : Application() {
    val viewModelFactory: MarkdownViewModelFactory by lazy {
        MarkdownViewModelFactory()
    }
    val errorHandler: ErrorHandler by lazy {
        CrashlyticsErrorHandler()
    }

    override fun onCreate() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyDeath()
                    .build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDeath()
                    .build())
        }
        super.onCreate()
    }
}
