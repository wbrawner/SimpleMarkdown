package com.wbrawner.simplemarkdown.view.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.wbrawner.simplemarkdown.R;

public class SettingsActivity extends AppCompatActivity {

    public static final String KEY_AUTOSAVE = "autosave";
    public static final String KEY_DOCS_PATH = "defaultRootDir";
    public static final String EDIT_VIEW = "0";
    public static final String FILE_VIEW = "1";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
