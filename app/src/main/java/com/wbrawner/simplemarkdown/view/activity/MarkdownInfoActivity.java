package com.wbrawner.simplemarkdown.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.webkit.WebView;

import com.wbrawner.simplemarkdown.R;
import com.wbrawner.simplemarkdown.view.fragment.PreviewFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MarkdownInfoActivity extends AppCompatActivity {

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
                PreviewFragment.style + intent.getStringExtra("html"),
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
