package com.wbrawner.simplemarkdown

import android.app.Application
import android.os.StrictMode
import com.wbrawner.simplemarkdown.viewmodel.MarkdownViewModelFactory

class MarkdownApplication : Application() {
    val viewModelFactory: MarkdownViewModelFactory by lazy {
        MarkdownViewModelFactory()
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
