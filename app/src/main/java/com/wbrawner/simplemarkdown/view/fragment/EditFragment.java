package com.wbrawner.simplemarkdown.view.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.wbrawner.simplemarkdown.MarkdownApplication;
import com.wbrawner.simplemarkdown.R;
import com.wbrawner.simplemarkdown.presentation.MarkdownPresenter;
import com.wbrawner.simplemarkdown.utility.MarkdownObserver;
import com.wbrawner.simplemarkdown.utility.Utils;
import com.wbrawner.simplemarkdown.view.MarkdownEditView;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class EditFragment extends Fragment implements MarkdownEditView {
    @Inject
    MarkdownPresenter presenter;
    @BindView(R.id.markdown_edit)
    EditText markdownEditor;
    @BindView(R.id.markdown_edit_container)
    ScrollView markdownEditorScroller;

    private Unbinder unbinder;
    private int lastScrollEvent = -1;

    public EditFragment() {
        // Required empty public constructor
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit, container, false);
        unbinder = ButterKnife.bind(this, view);
        Activity activity = getActivity();
        if (activity != null) {
            ((MarkdownApplication) activity.getApplication()).getComponent().inject(this);
        }
        Observable<String> obs = RxTextView.textChanges(markdownEditor)
                .debounce(50, TimeUnit.MILLISECONDS)
                .map(CharSequence::toString)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        obs.subscribe(new MarkdownObserver(presenter, obs));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.setEditView(EditFragment.this);
        presenter.loadMarkdown();
        //noinspection AndroidLintClickableViewAccessibility
        markdownEditorScroller.setOnTouchListener((v, event) -> {
            // The focus should only be set if this was a click, and not a scroll
            if (lastScrollEvent == MotionEvent.ACTION_DOWN && event.getAction() == MotionEvent.ACTION_UP) {
                if (getActivity() == null) {
                    return false;
                }
                InputMethodManager imm = (InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm == null) {
                    return false;
                }
                imm.showSoftInput(markdownEditor, InputMethodManager.SHOW_IMPLICIT);
                markdownEditor.requestFocus();
            }
            lastScrollEvent = event.getAction();
            return false;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.setEditView(this);
        setMarkdown(presenter.getMarkdown());
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.setEditView(null);
    }

    @Override
    public String getMarkdown() {
        return markdownEditor.getText().toString();
    }

    @Override
    public void setMarkdown(String markdown) {
        markdownEditor.setText(markdown);
    }

    @Override
    public void setTitle(String title) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle(title);
        }
    }

    @Override
    public void onFileSaved(boolean success) {
        String location = Utils.getDocsPath(getActivity()) + presenter.getFileName();
        String message;
        if (success) {
            message = getString(R.string.file_saved, location);
        } else {
            message = getString(R.string.file_save_error);
        }
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFileLoaded(boolean success) {
        int message = success ? R.string.file_loaded : R.string.file_load_error;
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
