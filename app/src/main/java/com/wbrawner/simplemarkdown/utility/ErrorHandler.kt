package com.wbrawner.simplemarkdown.utility

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.wbrawner.simplemarkdown.BuildConfig

interface ErrorHandler {
    fun enable(enable: Boolean)
    fun reportException(t: Throwable, message: String? = null)
}

class CrashlyticsErrorHandler : ErrorHandler {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun enable(enable: Boolean) {
        crashlytics.setCrashlyticsCollectionEnabled(enable)
    }

    override fun reportException(t: Throwable, message: String?) {
        @Suppress("ConstantConditionIf")
        if (BuildConfig.DEBUG) {
            Log.e("CrashlyticsErrorHandler", "Caught exception: $message", t)
            return
        }
        crashlytics.recordException(t)
    }
}