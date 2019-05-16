package com.wbrawner.simplemarkdown.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.wbrawner.simplemarkdown.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MarkdownInfoActivity extends AppCompatActivity {
    public static String FORMAT_CSS = "<style>" +
            "%s" +
            "</style>";

    @BindView(R.id.info_webview)
    WebView infoWebview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_markdown_info);
        ButterKnife.bind(this);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra("title") || !intent.hasExtra("html")) {
            finish();
            return;
        }
        setTitle(intent.getStringExtra("title"));
        infoWebview.loadDataWithBaseURL(
                null,
                String.format(FORMAT_CSS,
                        getString(R.string.pref_custom_css_default)
                ) + intent.getStringExtra("html"),
                "text/html",
                "UTF-8",
                null
        );
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
