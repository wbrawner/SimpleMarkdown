package com.wbrawner.simplemarkdown.utility

import android.content.Context
import com.wbrawner.simplemarkdown.BuildConfig
import timber.log.Timber

class NoopAnalyticsHelper(context: Context): AnalyticsHelper() {
    override fun setUserProperty(name: String, value: String) {
        if (BuildConfig.DEBUG) {
            Timber.tag("NoopAnalyticsHelper").d("setting user property $name to $value")
        }
    }

    override fun trackPageView(name: String) {
        Timber.tag("NoopAnalyticsHelper").d("user viewed $name page")
    }
}

fun AnalyticsHelper.Companion.init(context: Context) = NoopAnalyticsHelper(context)
