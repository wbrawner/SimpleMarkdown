package com.wbrawner.simplemarkdown.core

import android.app.Application
import android.util.Log
import org.acra.ACRA
import org.acra.config.httpSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import org.acra.ktx.sendSilentlyWithAcra
import org.acra.sender.HttpSender
import timber.log.Timber

class ErrorReporterTree private constructor(): Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority != Log.ERROR) return
        t?.sendSilentlyWithAcra()
    }

    companion object {
        fun create(application: Application): ErrorReporterTree {
            application.createErrorReporterTree()
            return ErrorReporterTree()
        }
    }
}

private fun Application.createErrorReporterTree() {
    initAcra {
        reportFormat = StringFormat.JSON
        httpSender {
            uri = "${BuildConfig.ACRA_URL}/report" /*best guess, you may need to adjust this*/
            basicAuthLogin = BuildConfig.ACRA_USER
            basicAuthPassword = BuildConfig.ACRA_PASS
            httpMethod = HttpSender.Method.POST
        }
    }
}