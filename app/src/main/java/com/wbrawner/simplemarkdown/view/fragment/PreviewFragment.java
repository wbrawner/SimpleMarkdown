package com.wbrawner.simplemarkdown.view.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.wbrawner.simplemarkdown.BuildConfig;
import com.wbrawner.simplemarkdown.MarkdownApplication;
import com.wbrawner.simplemarkdown.R;
import com.wbrawner.simplemarkdown.presentation.MarkdownPresenter;
import com.wbrawner.simplemarkdown.view.MarkdownPreviewView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class PreviewFragment extends Fragment implements MarkdownPreviewView {
    private static final String TAG = PreviewFragment.class.getSimpleName();
    public static String FORMAT_CSS = "<style>" +
            "%s" +
            "</style>";
    @Inject
    MarkdownPresenter presenter;
    @BindView(R.id.markdown_view)
    WebView markdownPreview;
    private Unbinder unbinder;
    private SharedPreferences sharedPreferences;

    public PreviewFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(container.getContext());
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_preview, container, false);
        unbinder = ButterKnife.bind(this, view);
        Activity activity = getActivity();
        if (activity != null) {
            ((MarkdownApplication) activity.getApplication()).getComponent().inject(this);
        }
        if (BuildConfig.DEBUG)
            WebView.setWebContentsDebuggingEnabled(true);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void updatePreview(String html) {
        if (markdownPreview == null) {
            return;
        }
        markdownPreview.post(() -> {
            if (markdownPreview == null) {
                return;
            }

            String style = String.format(
                    FORMAT_CSS,
                    sharedPreferences.getString(
                            getString(R.string.pref_custom_css),
                            ""
                    )
            );

            markdownPreview.loadDataWithBaseURL(
                    null,
                    style + html,
                    "text/html",
                    "UTF-8",
                    null
            );
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.setPreviewView(this);
        presenter.onMarkdownEdited();
    }

    @Override
    public void onDestroyView() {
        if (markdownPreview != null) {
            ((ViewGroup) markdownPreview.getParent()).removeView(markdownPreview);
            markdownPreview.destroy();
        }
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.setPreviewView(null);
    }
}
