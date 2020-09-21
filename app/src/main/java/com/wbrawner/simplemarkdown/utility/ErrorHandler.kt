package com.wbrawner.simplemarkdown.utility

interface ErrorHandler {
    fun enable(enable: Boolean)
    fun reportException(t: Throwable, message: String? = null)
}
