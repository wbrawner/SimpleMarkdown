package com.wbrawner.simplemarkdown.utility

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.wbrawner.simplemarkdown.BuildConfig
import timber.log.Timber
import kotlin.reflect.KProperty

class CrashlyticsErrorHandler : ErrorHandler {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun enable(enable: Boolean) {
        crashlytics.setCrashlyticsCollectionEnabled(enable)
    }

    override fun reportException(t: Throwable, message: String?) {
        @Suppress("ConstantConditionIf")
        if (BuildConfig.DEBUG) {
            Timber.e(t, "Caught exception: $message")
        }
        crashlytics.recordException(t)
    }
}

class errorHandlerImpl {
    operator fun getValue(thisRef: Any, property: KProperty<*>): ErrorHandler {
        return impl
    }

    companion object {
        val impl = CrashlyticsErrorHandler()
    }
}
