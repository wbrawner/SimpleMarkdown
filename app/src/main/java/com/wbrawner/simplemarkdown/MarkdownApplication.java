package com.wbrawner.simplemarkdown;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraHttpSender;
import org.acra.data.StringFormat;
import org.acra.sender.HttpSender;

import static com.wbrawner.simplemarkdown.BuildConfig.ACRA_PASS;
import static com.wbrawner.simplemarkdown.BuildConfig.ACRA_URL;
import static com.wbrawner.simplemarkdown.BuildConfig.ACRA_USER;
import static org.acra.ReportField.ANDROID_VERSION;
import static org.acra.ReportField.APP_VERSION_CODE;
import static org.acra.ReportField.APP_VERSION_NAME;
import static org.acra.ReportField.AVAILABLE_MEM_SIZE;
import static org.acra.ReportField.BRAND;
import static org.acra.ReportField.BUILD_CONFIG;
import static org.acra.ReportField.CRASH_CONFIGURATION;
import static org.acra.ReportField.CUSTOM_DATA;
import static org.acra.ReportField.INITIAL_CONFIGURATION;
import static org.acra.ReportField.PACKAGE_NAME;
import static org.acra.ReportField.PHONE_MODEL;
import static org.acra.ReportField.SHARED_PREFERENCES;
import static org.acra.ReportField.STACK_TRACE;
import static org.acra.ReportField.STACK_TRACE_HASH;
import static org.acra.ReportField.THREAD_DETAILS;
import static org.acra.ReportField.TOTAL_MEM_SIZE;
import static org.acra.ReportField.USER_APP_START_DATE;
import static org.acra.ReportField.USER_CRASH_DATE;

@AcraCore(
        buildConfigClass = BuildConfig.class,
        reportContent = {
                ANDROID_VERSION,
                APP_VERSION_CODE,
                APP_VERSION_NAME,
                AVAILABLE_MEM_SIZE,
                BRAND,
                BUILD_CONFIG,
                CRASH_CONFIGURATION,
                CUSTOM_DATA, // Not currently used, but might be useful in the future
                INITIAL_CONFIGURATION,
                PACKAGE_NAME,
                PHONE_MODEL,
                SHARED_PREFERENCES,
                STACK_TRACE,
                STACK_TRACE_HASH,
                THREAD_DETAILS,
                TOTAL_MEM_SIZE,
                USER_APP_START_DATE,
                USER_CRASH_DATE,
        },
        reportFormat = StringFormat.JSON
)
@AcraHttpSender(uri = ACRA_URL,
        basicAuthLogin = ACRA_USER,
        basicAuthPassword = ACRA_PASS,
        httpMethod = HttpSender.Method.POST)
public class MarkdownApplication extends Application {

    private AppComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        component = DaggerAppComponent.builder()
                .appModule(new AppModule())
                .build();
    }

    public AppComponent getComponent() {
        return component;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }
}
