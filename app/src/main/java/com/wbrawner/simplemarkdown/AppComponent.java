package com.wbrawner.simplemarkdown;

import com.wbrawner.simplemarkdown.view.activity.MainActivity;
import com.wbrawner.simplemarkdown.view.activity.SplashActivity;
import com.wbrawner.simplemarkdown.view.fragment.EditFragment;
import com.wbrawner.simplemarkdown.view.fragment.PreviewFragment;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by billy on 8/22/17.
 */

@Singleton
@Component(modules = { AppModule.class })
public interface AppComponent {
    void inject(MarkdownApplication application);
    void inject(MainActivity activity);
    void inject(SplashActivity activity);
    void inject(EditFragment fragment);
    void inject(PreviewFragment fragment);
}
