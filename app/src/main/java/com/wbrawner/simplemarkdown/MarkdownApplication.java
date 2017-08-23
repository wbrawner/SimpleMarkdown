package com.wbrawner.simplemarkdown;

import android.app.Application;

/**
 * Created by billy on 8/22/17.
 */

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
}
