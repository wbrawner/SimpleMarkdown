package com.wbrawner.simplemarkdown.utility

import android.content.Context
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.BuildConfig
import io.fabric.sdk.android.Fabric
import java.util.concurrent.atomic.AtomicBoolean

interface ErrorHandler {
    fun init(context: Context)
    fun reportException(t: Throwable)
}

class CrashlyticsErrorHandler : ErrorHandler {
    private val isInitialized = AtomicBoolean(false)

    override fun init(context: Context) {
        if (!isInitialized.getAndSet(true)) {
            Fabric.with(context, Crashlytics())
        }
    }

    override fun reportException(t: Throwable) {
        if (!isInitialized.get() || BuildConfig.DEBUG) return
        Crashlytics.logException(t)
    }
}