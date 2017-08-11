package com.wbrawner.simplemarkdown;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.jakewharton.rxbinding2.widget.RxTextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static android.content.ContentValues.TAG;


public class EditFragment extends Fragment {
    public static final String SAVE_ACTION = "com.wbrawner.simplemarkdown.ACTION_SAVE";
    public static final String LOAD_ACTION = "com.wbrawner.simplemarkdown.ACTION_LOAD";
    private MarkdownViewModel markdownViewModel;

    @BindView(R.id.markdown_edit)
    EditText markdownEditor;

    public EditFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit, container, false);
        ButterKnife.bind(this, view);
        markdownViewModel = ViewModelProviders.of(getActivity()).get(MarkdownViewModel.class);
        Observable<String> obs = RxTextView.textChanges(markdownEditor)
                .debounce(50, TimeUnit.MILLISECONDS).map(editable ->  editable.toString());
        obs.subscribeOn(Schedulers.io());
        obs.observeOn(AndroidSchedulers.mainThread());
        obs.subscribe(data -> {
            markdownViewModel.updateMarkdown(data);
        });
        return view;
    }
}
