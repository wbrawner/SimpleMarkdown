package com.wbrawner.simplemarkdown.presentation;

import android.content.Context;
import android.net.Uri;

import com.wbrawner.simplemarkdown.view.MarkdownEditView;
import com.wbrawner.simplemarkdown.view.MarkdownPreviewView;

import java.io.File;
import java.io.InputStream;

public interface MarkdownPresenter {
    File getFile();
    void loadMarkdown();

    void loadMarkdown(String fileName, InputStream in);
    void loadMarkdown(File file);
    void loadFromUri(Context context, Uri fileUri);

    void loadMarkdown(String fileName, InputStream in, OnTempFileLoadedListener listener);
    void newFile(String path);
    void setEditView(MarkdownEditView editView);
    void setPreviewView(MarkdownPreviewView previewView);
    void saveMarkdown(MarkdownSavedListener listener, String filePath);
    void onMarkdownEdited();
    void onMarkdownEdited(String markdown);
    String getFileName();
    void setFileName(String name);
    void setRootDir(String path);
    String generateHTML();
    String generateHTML(String markdown);
    String getMarkdown();
    void setMarkdown(String markdown);

    interface OnTempFileLoadedListener {
        void onSuccess(String markdown);

        void onError();
    }

    interface MarkdownSavedListener {
        void saveComplete(boolean success);
    }
}
