package com.wbrawner.simplemarkdown;

import android.app.Application;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class MarkdownApplication extends Application {

    private AppComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
        component = DaggerAppComponent.builder()
                .appModule(new AppModule())
                .build();
    }

    public AppComponent getComponent() {
        return component;
    }
}
