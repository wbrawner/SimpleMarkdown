package com.wbrawner.simplemarkdown.presentation;

import android.content.Context;
import android.net.Uri;

import com.wbrawner.simplemarkdown.view.MarkdownEditView;
import com.wbrawner.simplemarkdown.view.MarkdownPreviewView;

import java.io.InputStream;
import java.io.OutputStream;

public interface MarkdownPresenter {
    void loadMarkdown(String fileName, InputStream in);
    void loadFromUri(Context context, Uri fileUri);

    void loadMarkdown(String fileName, InputStream in, FileLoadedListener listener,
                      boolean replaceCurrentFile);

    void newFile(String newName);
    void setEditView(MarkdownEditView editView);
    void setPreviewView(MarkdownPreviewView previewView);

    void saveMarkdown(MarkdownSavedListener listener, String name, OutputStream outputStream);
    void onMarkdownEdited();
    void onMarkdownEdited(String markdown);
    String getFileName();
    void setFileName(String name);
    String generateHTML();
    String generateHTML(String markdown);
    String getMarkdown();
    void setMarkdown(String markdown);

    interface FileLoadedListener {
        void onSuccess(String markdown);

        void onError();
    }

    interface MarkdownSavedListener {
        void saveComplete(boolean success);
    }
}
