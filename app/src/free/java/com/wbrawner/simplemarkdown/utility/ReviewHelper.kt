package com.wbrawner.simplemarkdown.utility

import android.app.Application
import android.util.Log

object ReviewHelper {
    // No review library for F-droid, so this is a no-op
    fun init(application: Application) {
        Timber.w("ReviewHelper", "ReviewHelper not enabled for free builds")
    }
}