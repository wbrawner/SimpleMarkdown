package com.wbrawner.simplemarkdown.utility

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.wbrawner.simplemarkdown.BuildConfig
import kotlin.reflect.KProperty

class CrashlyticsErrorHandler : ErrorHandler {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun enable(enable: Boolean) {
        crashlytics.setCrashlyticsCollectionEnabled(enable)
    }

    override fun reportException(t: Throwable, message: String?) {
        @Suppress("ConstantConditionIf")
        if (BuildConfig.DEBUG) {
            Log.e("CrashlyticsErrorHandler", "Caught exception: $message", t)
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
