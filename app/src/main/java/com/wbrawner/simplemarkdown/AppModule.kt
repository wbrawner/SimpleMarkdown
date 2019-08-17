package com.wbrawner.simplemarkdown

import com.wbrawner.simplemarkdown.presentation.MarkdownPresenter
import com.wbrawner.simplemarkdown.presentation.MarkdownPresenterImpl
import com.wbrawner.simplemarkdown.utility.CrashlyticsErrorHandler
import com.wbrawner.simplemarkdown.utility.ErrorHandler
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {
    @Provides
    @Singleton
    fun provideMarkdownPresenter(errorHandler: ErrorHandler): MarkdownPresenter {
        return MarkdownPresenterImpl(errorHandler)
    }

    @Provides
    @Singleton
    internal fun provideErrorHandler(): ErrorHandler {
        return CrashlyticsErrorHandler()
    }
}