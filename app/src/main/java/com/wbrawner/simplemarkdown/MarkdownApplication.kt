package com.wbrawner.simplemarkdown

import android.app.Application

class MarkdownApplication : Application() {

    lateinit var component: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        component = DaggerAppComponent.builder()
                .context(this)
                .build()
    }
}
