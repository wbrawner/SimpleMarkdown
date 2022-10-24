package com.wbrawner.simplemarkdown.utility

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseAnalyticsHelper(context: Context): AnalyticsHelper() {
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    override fun setUserProperty(name: String, value: String) {
        firebaseAnalytics.setUserProperty(name, value)
    }

    override fun trackPageView(name: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply { putString(FirebaseAnalytics.Param.SCREEN_NAME, name) })
    }
}

fun AnalyticsHelper.Companion.init(context: Context) = FirebaseAnalyticsHelper(context)
