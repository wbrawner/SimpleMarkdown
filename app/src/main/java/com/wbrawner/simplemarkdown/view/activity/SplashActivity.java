package com.wbrawner.simplemarkdown.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.wbrawner.simplemarkdown.MarkdownApplication;
import com.wbrawner.simplemarkdown.presentation.MarkdownPresenter;
import com.wbrawner.simplemarkdown.utility.Utils;

import javax.inject.Inject;

public class SplashActivity extends AppCompatActivity {

    @Inject
    MarkdownPresenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MarkdownApplication) getApplication()).getComponent().inject(this);

        String defaultName = Utils.getDefaultFileName(this);

        Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            presenter.loadFromUri(getApplicationContext(), intent.getData());
        } else {
            presenter.setFileName(defaultName);
        }

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
