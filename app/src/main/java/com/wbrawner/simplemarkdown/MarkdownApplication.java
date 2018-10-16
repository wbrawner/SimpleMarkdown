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

@AcraCore(buildConfigClass = BuildConfig.class, reportFormat = StringFormat.JSON)
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
