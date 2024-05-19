package com.wbrawner.simplemarkdown.utility

import android.app.Application
import timber.log.Timber

object ReviewHelper {
    // No review library for F-droid, so this is a no-op
    fun init(application: Application) {
        Timber.tag("ReviewHelper").w("ReviewHelper not enabled for free builds")
    }
}