package com.wbrawner.simplemarkdown;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.commonsware.cwac.anddown.AndDown;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PreviewFragment extends Fragment {
    private static final String TAG = PreviewFragment.class.getSimpleName();
    private static final int INTERNET_REQUEST = 0;
    private WebView mMarkdownView;

    @BindView(R.id.markdown_view)
    WebView markdownView;

    public static final String SCROLL_ACTION = "com.wbrawner.simplemarkdown.scroll";
    public static final String PREVIEW_ACTION = "com.wbrawner.simplemarkdown.preview";

    public PreviewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter();
        filter.addAction(PREVIEW_ACTION);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(
                new MarkdownBroadcastSender(),
                filter
        );
        checkPermissions();
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.INTERNET},
                    INTERNET_REQUEST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_preview, container, false);
        ButterKnife.bind(this, view);
        mMarkdownView = markdownView;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        return view;
    }

    private class MarkdownBroadcastSender extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Intent received: " + intent.getAction());
            switch (intent.getAction()) {
                case PREVIEW_ACTION:
                    if (intent.hasExtra("markdownData")) {
                        String data = intent.getStringExtra("markdownData");
                        int yPos = 0;
                        if (intent.hasExtra("scrollY")) {
                            float yPercent = intent.getFloatExtra("scrollY", 0);
                            Log.d(TAG, "Scrolling to: " + yPercent);
                            yPos = Math.round(mMarkdownView.getContentHeight() * yPercent);
                        }
                        markdown(data, yPos);
                    }
                    break;
            }
        }
    }

    private void markdown(final String text, final int scrollY) {
        Thread setMarkdown = new Thread() {
            @Override
            public void run() {
                AndDown andDown = new AndDown();
                int hoedownFlags =
                        AndDown.HOEDOWN_EXT_STRIKETHROUGH | AndDown.HOEDOWN_EXT_TABLES |
                        AndDown.HOEDOWN_EXT_UNDERLINE | AndDown.HOEDOWN_EXT_SUPERSCRIPT |
                        AndDown.HOEDOWN_EXT_FENCED_CODE;
                String html = andDown.markdownToHtml(text, hoedownFlags, 0);
                mMarkdownView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
                mMarkdownView.scrollTo(0, scrollY);
            }
        };
        setMarkdown.run();
    }
}
