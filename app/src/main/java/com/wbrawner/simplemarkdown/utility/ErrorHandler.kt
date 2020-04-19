package com.wbrawner.simplemarkdown.utility

import android.content.Context
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.wbrawner.simplemarkdown.BuildConfig
import io.fabric.sdk.android.Fabric
import java.util.concurrent.atomic.AtomicBoolean

interface ErrorHandler {
    fun init(context: Context)
    fun reportException(t: Throwable, message: String? = null)
}

class CrashlyticsErrorHandler : ErrorHandler {
    private val isInitialized = AtomicBoolean(false)

    override fun init(context: Context) {
        if (!isInitialized.getAndSet(true)) {
            Fabric.with(context, Crashlytics())
        }
    }

    override fun reportException(t: Throwable, message: String?) {
        @Suppress("ConstantConditionIf")
        if (BuildConfig.DEBUG) {
            Log.e("CrashlyticsErrorHandler", "Caught exception: $message", t)
            return
        }
        if (!isInitialized.get()) return
        Crashlytics.logException(t)
    }
}