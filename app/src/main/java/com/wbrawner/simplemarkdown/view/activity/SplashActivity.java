package com.wbrawner.simplemarkdown.view.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.wbrawner.simplemarkdown.MarkdownApplication;
import com.wbrawner.simplemarkdown.R;
import com.wbrawner.simplemarkdown.presentation.MarkdownPresenter;
import com.wbrawner.simplemarkdown.utility.ErrorHandler;
import com.wbrawner.simplemarkdown.utility.Utils;

import javax.inject.Inject;

public class SplashActivity extends AppCompatActivity {

    @Inject
    MarkdownPresenter presenter;

    @Inject
    ErrorHandler errorHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        ((MarkdownApplication) getApplication()).getComponent().inject(this);
        if (sharedPreferences.getBoolean(getString(R.string.error_reports_enabled), true)) {
            errorHandler.init(this);
        }

        String darkModeValue = sharedPreferences.getString(
                getString(R.string.pref_key_dark_mode),
                getString(R.string.pref_value_auto)
        );

        int darkMode;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            darkMode = AppCompatDelegate.MODE_NIGHT_AUTO;
        } else {
            darkMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }

        if (darkModeValue != null && !darkModeValue.isEmpty()) {
            if (darkModeValue.equalsIgnoreCase(getString(R.string.pref_value_light))) {
                darkMode = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (darkModeValue.equalsIgnoreCase(getString(R.string.pref_value_dark))) {
                darkMode = AppCompatDelegate.MODE_NIGHT_YES;
            }
        }
        AppCompatDelegate.setDefaultNightMode(darkMode);

        String defaultName = Utils.getDefaultFileName(this);

        Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            presenter.loadFromUri(getApplicationContext(), intent.getData());
        } else {
            presenter.setFileName(defaultName);
        }

        Intent startIntent = new Intent(this, MainActivity.class);
        startActivity(startIntent);
        finish();
    }
}
