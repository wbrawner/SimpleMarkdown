package com.wbrawner.simplemarkdown;

import androidx.multidex.MultiDexApplication;

public class MarkdownApplication extends MultiDexApplication {

    private AppComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        component = DaggerAppComponent.builder()
                .context(this)
                .build();
    }

    public AppComponent getComponent() {
        return component;
    }
}
