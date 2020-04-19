package com.wbrawner.simplemarkdown.utility

import android.app.Application
import android.util.Log
import com.evernote.android.job.JobRequest
import com.wbrawner.simplemarkdown.BuildConfig
import org.acra.ACRA
import org.acra.config.CoreConfigurationBuilder
import org.acra.config.HttpSenderConfigurationBuilder
import org.acra.config.SchedulerConfigurationBuilder
import org.acra.data.StringFormat
import org.acra.sender.HttpSender
import java.util.concurrent.atomic.AtomicBoolean

interface ErrorHandler {
    fun init(application: Application)
    fun reportException(t: Throwable, message: String? = null)
}

class AcraErrorHandler : ErrorHandler {
    private val isInitialized = AtomicBoolean(false)

    override fun init(application: Application) {
        if (BuildConfig.ACRA_URL.isBlank()
                || BuildConfig.ACRA_USER.isBlank()
                || BuildConfig.ACRA_PASS.isBlank()) {
            return
        }
        if (!isInitialized.getAndSet(true)) {
            val builder = CoreConfigurationBuilder(application)
                    .setBuildConfigClass(BuildConfig::class.java)
                    .setReportFormat(StringFormat.JSON)
            builder.getPluginConfigurationBuilder(HttpSenderConfigurationBuilder::class.java)
                    .setUri(BuildConfig.ACRA_URL)
                    .setHttpMethod(HttpSender.Method.POST)
                    .setBasicAuthLogin(BuildConfig.ACRA_USER)
                    .setBasicAuthPassword(BuildConfig.ACRA_PASS)
                    .setEnabled(true)
            builder.getPluginConfigurationBuilder(SchedulerConfigurationBuilder::class.java)
                    .setRequiresNetworkType(JobRequest.NetworkType.UNMETERED)
                    .setRequiresBatteryNotLow(true)
                    .setEnabled(true)
            ACRA.init(application, builder)
        }
    }

    override fun reportException(t: Throwable, message: String?) {
        @Suppress("ConstantConditionIf")
        if (BuildConfig.DEBUG) {
            Log.e("AcraErrorHandler", "Caught exception: $message", t)
            return
        }
        if (!isInitialized.get()) return
        ACRA.getErrorReporter().handleException(t)
    }
}