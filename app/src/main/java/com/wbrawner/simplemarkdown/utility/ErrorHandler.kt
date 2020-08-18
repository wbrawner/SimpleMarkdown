package com.wbrawner.simplemarkdown.utility

import android.app.Application
import android.app.job.JobInfo
import android.util.Log
import com.wbrawner.simplemarkdown.BuildConfig
import org.acra.ACRA
import org.acra.ReportField
import org.acra.config.CoreConfigurationBuilder
import org.acra.config.HttpSenderConfigurationBuilder
import org.acra.config.SchedulerConfigurationBuilder
import org.acra.data.StringFormat
import org.acra.sender.HttpSender

interface ErrorHandler {
    fun init(application: Application)
    fun reportException(t: Throwable, message: String? = null)
}

class AcraErrorHandler : ErrorHandler {

    override fun init(application: Application) {
        if (BuildConfig.ACRA_URL.isBlank()
                || BuildConfig.ACRA_USER.isBlank()
                || BuildConfig.ACRA_PASS.isBlank()) {
            return
        }
        val builder = CoreConfigurationBuilder(application)
                .setBuildConfigClass(BuildConfig::class.java)
                .setReportFormat(StringFormat.JSON)
                .setReportContent(
                        ReportField.ANDROID_VERSION,
                        ReportField.APP_VERSION_CODE,
                        ReportField.APP_VERSION_NAME,
                        ReportField.APPLICATION_LOG,
                        ReportField.AVAILABLE_MEM_SIZE,
                        ReportField.BRAND,
                        ReportField.BUILD_CONFIG,
                        ReportField.CRASH_CONFIGURATION,
                        ReportField.CUSTOM_DATA, // Not currently used, but might be useful in the future
                        ReportField.INITIAL_CONFIGURATION,
                        ReportField.PACKAGE_NAME,
                        ReportField.PHONE_MODEL,
                        ReportField.SHARED_PREFERENCES,
                        ReportField.STACK_TRACE,
                        ReportField.STACK_TRACE_HASH,
                        ReportField.THREAD_DETAILS,
                        ReportField.TOTAL_MEM_SIZE,
                        ReportField.USER_APP_START_DATE,
                        ReportField.USER_CRASH_DATE
                )
        builder.getPluginConfigurationBuilder(HttpSenderConfigurationBuilder::class.java)
                .setUri(BuildConfig.ACRA_URL)
                .setHttpMethod(HttpSender.Method.POST)
                .setBasicAuthLogin(BuildConfig.ACRA_USER)
                .setBasicAuthPassword(BuildConfig.ACRA_PASS)
                .setEnabled(true)
        builder.getPluginConfigurationBuilder(SchedulerConfigurationBuilder::class.java)
                .setRequiresNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .setRequiresBatteryNotLow(true)
                .setEnabled(true)
        ACRA.init(application, builder)
    }

    override fun reportException(t: Throwable, message: String?) {
        @Suppress("ConstantConditionIf")
        if (BuildConfig.DEBUG) {
            Log.e("AcraErrorHandler", "Caught exception: $message", t)
        }
        ACRA.getErrorReporter().handleException(t)
    }
}