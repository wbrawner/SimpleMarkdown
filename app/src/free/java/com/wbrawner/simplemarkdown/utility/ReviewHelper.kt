package com.wbrawner.simplemarkdown.utility

import android.app.Activity
import android.content.Context
import androidx.preference.PreferenceManager
import com.google.android.play.core.review.ReviewManagerFactory
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object ReviewHelper {
    // No review library for F-droid, so this is a no-op
    fun init(application: Application, errorHandler: ErrorHandler) {
        Log.w("ReviewHelper", "ReviewHelper not enabled for free builds")
    }
}