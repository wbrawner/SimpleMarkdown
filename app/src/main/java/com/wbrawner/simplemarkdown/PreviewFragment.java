package com.wbrawner.simplemarkdown;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import us.feras.mdv.MarkdownView;

public class PreviewFragment extends Fragment {
    private static final String TAG = PreviewFragment.class.getSimpleName();
    @BindView(R.id.markdown_view)
    MarkdownView markdownView;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_preview, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    private class MarkdownBroadcastSender extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("markdownData")) {
                String data = intent.getStringExtra("markdownData");
                Log.d(TAG, "Markdown Data: " + data);
                markdownView.loadMarkdown(data);
            }
        }
    }

}
