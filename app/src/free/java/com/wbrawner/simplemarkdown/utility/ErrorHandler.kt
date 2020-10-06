package com.wbrawner.simplemarkdown.utility

import android.util.Log
import kotlin.reflect.KProperty

class errorHandlerImpl {
    operator fun getValue(thisRef: Any, property: KProperty<*>): ErrorHandler {
        return implementation
    }

    companion object {
        // Default no-op error handler for debugging
        val implementation: ErrorHandler = object : ErrorHandler {
            override fun enable(enable: Boolean) {
                // no-op
            }

            override fun reportException(t: Throwable, message: String?) {
                Log.e("ErrorHandler", "Caught non-fatal exception. Message: $message", t)
            }
        }
    }
}
