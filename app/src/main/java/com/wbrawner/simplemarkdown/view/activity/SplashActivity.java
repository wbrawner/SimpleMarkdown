package com.wbrawner.simplemarkdown.view.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;
import com.wbrawner.simplemarkdown.MarkdownApplication;
import com.wbrawner.simplemarkdown.R;
import com.wbrawner.simplemarkdown.presentation.MarkdownPresenter;
import com.wbrawner.simplemarkdown.utility.Constants;
import com.wbrawner.simplemarkdown.utility.Utils;

import javax.inject.Inject;

import io.fabric.sdk.android.Fabric;

public class SplashActivity extends AppCompatActivity {

    @Inject
    MarkdownPresenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean(getString(R.string.error_reports_enabled), true)) {
            Fabric.with(this, new Crashlytics());
        }
        ((MarkdownApplication) getApplication()).getComponent().inject(this);

        String defaultName = Utils.getDefaultFileName(this);

        Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            presenter.loadFromUri(getApplicationContext(), intent.getData());
        } else {
            presenter.setFileName(defaultName);
        }

        String defaultRootDir =
                sharedPreferences.getString(Constants.KEY_DOCS_PATH, Utils.getDocsPath(this));
        presenter.setRootDir(defaultRootDir);

        Intent startIntent = new Intent(this, MainActivity.class);
        String startScreen = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(
                        getString(R.string.key_default_view),
                        Constants.VALUE_EDIT_VIEW
                );
        switch (startScreen) {
            case Constants.VALUE_FILE_VIEW:
                startIntent.putExtra(Constants.EXTRA_EXPLORER, true);
                break;
        }
        startActivity(startIntent);
        finish();
    }
}
