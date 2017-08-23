package com.wbrawner.simplemarkdown;

import com.wbrawner.simplemarkdown.model.MarkdownFile;
import com.wbrawner.simplemarkdown.presentation.MarkdownPresenter;
import com.wbrawner.simplemarkdown.presentation.MarkdownPresenterImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by billy on 8/22/17.
 */

@Module
public class AppModule {
    @Provides
    public MarkdownFile provideMarkdownFile() {
        return new MarkdownFile();
    }

    @Provides @Singleton
    public MarkdownPresenter provideMarkdownPresenter(MarkdownFile file) {
        return new MarkdownPresenterImpl(file);
    }
}