package com.wbrawner.simplemarkdown.view.fragment;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.wbrawner.simplemarkdown.BuildConfig;
import com.wbrawner.simplemarkdown.MarkdownApplication;
import com.wbrawner.simplemarkdown.R;
import com.wbrawner.simplemarkdown.presentation.MarkdownPresenter;
import com.wbrawner.simplemarkdown.view.MarkdownPreviewView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PreviewFragment extends Fragment implements MarkdownPreviewView {
    private static final String TAG = PreviewFragment.class.getSimpleName();

    @Inject
    MarkdownPresenter presenter;

    @BindView(R.id.markdown_view)
    WebView markdownPreview;

    public PreviewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_preview, container, false);
        ButterKnife.bind(this, view);
        ((MarkdownApplication) getActivity().getApplication()).getComponent().inject(this);
        if (BuildConfig.DEBUG)
            WebView.setWebContentsDebuggingEnabled(true);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }



    @Override
    public void updatePreview(String html) {
        markdownPreview.post(() -> {
            markdownPreview.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.setPreviewView(this);
        presenter.onMarkdownEdited();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.setPreviewView(null);
    }
}
