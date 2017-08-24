package com.wbrawner.simplemarkdown.view.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.wbrawner.simplemarkdown.R;

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra("title") || !intent.hasExtra("html"))
            finish();
        setTitle(intent.getStringExtra("title"));
        infoWebview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        infoWebview.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        infoWebview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        infoWebview.loadDataWithBaseURL(
                null,
                intent.getStringExtra("html"),
                "text/html",
                "UTF-8",
                null
        );
    }
}
