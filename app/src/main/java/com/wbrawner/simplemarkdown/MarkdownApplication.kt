package com.wbrawner.simplemarkdown

import android.app.Application
import android.os.StrictMode
import com.wbrawner.simplemarkdown.utility.CrashlyticsErrorHandler
import com.wbrawner.simplemarkdown.utility.ErrorHandler

class MarkdownApplication : Application() {
    val errorHandler: ErrorHandler by lazy {
        CrashlyticsErrorHandler()
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
}
