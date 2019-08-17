package com.wbrawner.simplemarkdown

import android.content.Context
import com.wbrawner.simplemarkdown.view.activity.MainActivity
import com.wbrawner.simplemarkdown.view.activity.SplashActivity
import com.wbrawner.simplemarkdown.view.fragment.EditFragment
import com.wbrawner.simplemarkdown.view.fragment.PreviewFragment
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(application: MarkdownApplication)
    fun inject(activity: MainActivity)
    fun inject(activity: SplashActivity)
    fun inject(fragment: EditFragment)
    fun inject(fragment: PreviewFragment)

    @Component.Builder
    abstract class Builder {
        @BindsInstance
        internal abstract fun context(context: Context): Builder

        internal abstract fun build(): AppComponent
    }
}
