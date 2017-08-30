package com.wbrawner.simplemarkdown.presentation;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import com.commonsware.cwac.anddown.AndDown;
import com.wbrawner.simplemarkdown.model.MarkdownFile;
import com.wbrawner.simplemarkdown.view.MarkdownEditView;
import com.wbrawner.simplemarkdown.view.MarkdownPreviewView;

import java.io.InputStream;

/**
 * Created by billy on 8/22/17.
 */

public class MarkdownPresenterImpl implements MarkdownPresenter {
    private MarkdownFile file;
    private MarkdownEditView editView;
    private MarkdownPreviewView previewView;
    private String TAG = MarkdownPresenterImpl.class.getSimpleName();
    private int HOEDOWN_FLAGS =
            AndDown.HOEDOWN_EXT_STRIKETHROUGH | AndDown.HOEDOWN_EXT_TABLES |
                    AndDown.HOEDOWN_EXT_UNDERLINE | AndDown.HOEDOWN_EXT_SUPERSCRIPT |
                    AndDown.HOEDOWN_EXT_FENCED_CODE;

    public MarkdownPresenterImpl(MarkdownFile file) {
        this.file = file;
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
        saveMarkdown("");
    }

    @Override
    public void loadMarkdown(String filePath) {
        Runnable fileLoader = () -> {
            int result = file.load(filePath);
            if (editView != null) {
                if (result == MarkdownFile.SUCCESS) {
                    editView.setMarkdown(getMarkdown());
                    onMarkdownEdited();
                } else {
                    editView.showFileLoadeddError(result);
                }
            }
        };
        fileLoader.run();
    }

    @Override
    public void loadMarkdown(InputStream in) {
        Runnable fileLoader = () -> {
            int result = file.load(in);
            if (result == MarkdownFile.SUCCESS) {
                if (editView != null)
                    editView.setMarkdown(getMarkdown());
                onMarkdownEdited();
            } else {
                if (editView != null)
                    editView.showFileLoadeddError(result);
            }
        };
        fileLoader.run();
    }

    @Override
    public void loadTempMarkdown(InputStream in, OnTempFileLoadedListener listener) {
        Runnable fileLoader = () -> {
            MarkdownFile tmpFile = new MarkdownFile();
            int result = tmpFile.load(in);
            if (result == MarkdownFile.SUCCESS) {
                String html = generateHTML(tmpFile.getContent());
                listener.onSuccess(html);
            } else {
                listener.onError(result);
            }
            tmpFile = null;
        };
        fileLoader.run();
    }

    @Override
    public void setEditView(MarkdownEditView editView) {
        this.editView = editView;
    }

    @Override
    public void setPreviewView(MarkdownPreviewView previewView) {
        this.previewView = previewView;
    }

    @Override
    public void saveMarkdown(String filePath) {
        Runnable fileSaver = () -> {
            int code;
            code = file.save(filePath);
            if (editView != null) {
                if (code == MarkdownFile.SUCCESS) {
                    editView.showFileSavedMessage();
                } else {
                    editView.showFileSavedError(code);
                }
            }
        };
        fileSaver.run();
    }

    @Override
    public void onMarkdownEdited(String markdown) {
        setMarkdown(markdown);
        Runnable generateMarkdown = () -> {
            if (previewView != null)
                previewView.updatePreview(generateHTML());
        };
        generateMarkdown.run();
    }

    @Override
    public String generateHTML() {
        return generateHTML(getMarkdown());
    }

    @Override
    public String generateHTML(String markdown) {
        AndDown andDown = new AndDown();
        return andDown.markdownToHtml(markdown, HOEDOWN_FLAGS, 0);
    }

    @Override
    public void onMarkdownEdited() {
        onMarkdownEdited(getMarkdown());
    }

    @Override
    public String getFileName() {
        return file.getName();
    }

    @Override
    public void setFileName(String name) {
        file.setName(name);
    }

    @Override
    public String getMarkdown() {
        return file.getContent();
    }

    @Override
    public void setMarkdown(String markdown) {
        file.setContent(markdown);
    }

    @Override
    public void loadFromUri(Context context, Uri fileUri) {
        try {
            InputStream in =
                    context.getContentResolver().openInputStream(fileUri);
            if (fileUri.getScheme().equals("content")) {
                Cursor retCur = context.getContentResolver()
                        .query(fileUri, null, null, null, null);
                if (retCur != null) {
                    int nameIndex = retCur
                            .getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    retCur.moveToFirst();
                    setFileName(retCur.getString(nameIndex));
                }
            } else if (fileUri.getScheme().equals("file")) {
                setFileName(fileUri.getLastPathSegment());
            }
            loadMarkdown(in);
        } catch (Exception e) {
            if (editView != null)
                editView.showFileLoadeddError(MarkdownFile.READ_ERROR);
        }
    }
}
