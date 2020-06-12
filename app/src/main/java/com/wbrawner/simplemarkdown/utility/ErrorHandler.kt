package com.wbrawner.simplemarkdown.utility

import android.content.Context
import android.util.Log
import com.wbrawner.simplemarkdown.BuildConfig
import io.sentry.android.core.SentryAndroid
import io.sentry.core.Sentry
import java.util.concurrent.atomic.AtomicBoolean

interface ErrorHandler {
    fun init(context: Context, enable: Boolean)
    fun enable(enable: Boolean)
    fun reportException(t: Throwable, message: String? = null)
}

class SentryErrorHandler : ErrorHandler {
    private lateinit var enabled: AtomicBoolean

    override fun init(context: Context, enable: Boolean) {
        enabled = AtomicBoolean(enable)
        SentryAndroid.init(context) { options ->
            options.setBeforeSend { event, _ ->
                if (enabled.get()) {
                    event
                } else {
                    null
                }
            }
        }
    }

    override fun enable(enable: Boolean) {
        enabled.set(enable)
    }

    override fun reportException(t: Throwable, message: String?) {
        @Suppress("ConstantConditionIf")
        if (BuildConfig.DEBUG) {
            Log.e("SentryErrorHandler", "Caught exception: $message", t)
        }
        Sentry.captureException(t)
    }
}