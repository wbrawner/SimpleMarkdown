package com.wbrawner.simplemarkdown;

import android.support.multidex.MultiDexApplication;

public class MarkdownApplication extends MultiDexApplication {

    private AppComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        component = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }

    public AppComponent getComponent() {
        return component;
    }
}
