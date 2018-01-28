package com.wbrawner.simplemarkdown.utility;

import com.crashlytics.android.Crashlytics;
import com.wbrawner.simplemarkdown.BuildConfig;
import com.wbrawner.simplemarkdown.presentation.MarkdownPresenter;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MarkdownObserver implements Observer<String> {
    private MarkdownPresenter presenter;
    private Observable<String> obs;

    public MarkdownObserver(MarkdownPresenter presenter, Observable<String> obs) {
        this.presenter = presenter;
        this.obs = obs;
    }

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onNext(String markdown) {
        presenter.onMarkdownEdited(markdown);
    }

    @Override
    public void onError(Throwable e) {
        System.err.println("An error occurred while handling the markdown");
        e.printStackTrace();
        // TODO: Remove this once the error is confirmed to have disappeared
        if (!BuildConfig.DEBUG) {
            Crashlytics.logException(e);
        }
        obs.subscribe(this);
    }

    @Override
    public void onComplete() {

    }
}
