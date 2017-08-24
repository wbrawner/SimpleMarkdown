package com.wbrawner.simplemarkdown.presentation;

import android.content.Context;
import android.net.Uri;

import com.wbrawner.simplemarkdown.view.MarkdownEditView;
import com.wbrawner.simplemarkdown.view.MarkdownPreviewView;

import java.io.InputStream;

/**
 * Created by billy on 8/22/17.
 */

public interface MarkdownPresenter extends LifecyclePresenter {
    void loadMarkdown(String filePath);
    void loadMarkdown(InputStream in);
    void loadFromUri(Context context, Uri fileUri);
    void loadTempMarkdown(InputStream in, OnTempFileLoadedListener listener);
    void setEditView(MarkdownEditView editView);
    void setPreviewView(MarkdownPreviewView previewView);
    void saveMarkdown(String filePath);
    void onMarkdownEdited();
    void onMarkdownEdited(String markdown);
    String getFileName();
    void setFileName(String name);
    String generateHTML();
    String generateHTML(String markdown);
    String getMarkdown();
    void setMarkdown(String markdown);
    public abstract class OnTempFileLoadedListener {
        public abstract void onSuccess(String markdown);
        public abstract void onError(int code);
    }
}
