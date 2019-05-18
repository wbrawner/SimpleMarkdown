package com.wbrawner.simplemarkdown;

import com.wbrawner.simplemarkdown.presentation.MarkdownPresenter;
import com.wbrawner.simplemarkdown.presentation.MarkdownPresenterImpl;
import com.wbrawner.simplemarkdown.utility.CrashlyticsErrorHandler;
import com.wbrawner.simplemarkdown.utility.ErrorHandler;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by billy on 8/22/17.
 */

@Module
public class AppModule {
    @Provides @Singleton
    public MarkdownPresenter provideMarkdownPresenter(ErrorHandler errorHandler) {
        return new MarkdownPresenterImpl(errorHandler);
    }

    @Provides
    @Singleton
    ErrorHandler provideErrorHandler() {
        return new CrashlyticsErrorHandler();
    }
}