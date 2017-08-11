package com.wbrawner.simplemarkdown;

import android.Manifest;
import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
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

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class PreviewFragment extends LifecycleFragment {
    private static final String TAG = PreviewFragment.class.getSimpleName();
    private static final int INTERNET_REQUEST = 0;
    private MarkdownViewModel markdownViewModel;

    @BindView(R.id.markdown_view)
    WebView markdownView;

    public static final String SCROLL_ACTION = "com.wbrawner.simplemarkdown.scroll";
    public static final String PREVIEW_ACTION = "com.wbrawner.simplemarkdown.preview";

    public PreviewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_preview, container, false);
        ButterKnife.bind(this, view);
        markdownViewModel = ViewModelProviders.of(getActivity()).get(MarkdownViewModel.class);
        markdownViewModel.getHtml().observe(this, s -> markdownView.loadData(s, "text/html", "UTF-8"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        return view;
    }
}
