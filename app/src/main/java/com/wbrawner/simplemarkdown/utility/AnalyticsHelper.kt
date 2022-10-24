package com.wbrawner.simplemarkdown.utility

abstract class AnalyticsHelper {
    abstract fun setUserProperty(name: String, value: String)
    abstract fun trackPageView(name: String)

    companion object
}
