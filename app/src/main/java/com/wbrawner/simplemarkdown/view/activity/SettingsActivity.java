package com.wbrawner.simplemarkdown.view.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.wbrawner.simplemarkdown.R;

/**
 * Created by billy on 1/14/2018.
 */

public class SettingsActivity extends AppCompatActivity {

    public static final String KEY_DOCS_PATH = "defaultRootDir";

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
