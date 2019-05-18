package com.wbrawner.simplemarkdown.presentation;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.OpenableColumns;

import com.commonsware.cwac.anddown.AndDown;
import com.wbrawner.simplemarkdown.model.MarkdownFile;
import com.wbrawner.simplemarkdown.utility.ErrorHandler;
import com.wbrawner.simplemarkdown.view.MarkdownEditView;
import com.wbrawner.simplemarkdown.view.MarkdownPreviewView;

import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MarkdownPresenterImpl implements MarkdownPresenter {
    private final Object fileLock = new Object();
    private MarkdownFile file;
    private volatile MarkdownEditView editView;
    private volatile MarkdownPreviewView previewView;
    private Handler fileHandler = new Handler();
    private final ErrorHandler errorHandler;

    @Inject
    public MarkdownPresenterImpl(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        synchronized (fileLock) {
            this.file = new MarkdownFile(errorHandler);
        }
    }

    @Override
    public void loadMarkdown(final String fileName, final InputStream in) {
        this.loadMarkdown(fileName, in, null);
    }

    @Override
    public void loadMarkdown(
            final String fileName,
            final InputStream in,
            final OnTempFileLoadedListener listener
    ) {
        Runnable fileLoader = () -> {
            MarkdownFile tmpFile = new MarkdownFile(errorHandler);
            if (tmpFile.load(fileName, in)) {
                if (listener != null) {
                    String html = generateHTML(tmpFile.getContent());
                    listener.onSuccess(html);
                } else {
                    synchronized (fileLock) {
                        this.file = tmpFile;
                        MarkdownEditView currentEditView = editView;
                        if (currentEditView != null) {
                            currentEditView.onFileLoaded(true);
                            currentEditView.setTitle(fileName);
                            currentEditView.setMarkdown(this.file.getContent());
                            onMarkdownEdited();
                        }
                    }
                }
            } else {
                if (listener != null) {
                    listener.onError();
                }
            }
        };
        fileHandler.post(fileLoader);
    }

    @Override
    public void newFile(String newName) {
        synchronized (fileLock) {
            MarkdownEditView currentEditView = editView;
            if (currentEditView != null) {
                file.setContent(currentEditView.getMarkdown());
                currentEditView.setTitle(newName);
                currentEditView.setMarkdown("");
            }
            file = new MarkdownFile(errorHandler, newName, "");
        }
    }

    @Override
    public void setEditView(MarkdownEditView editView) {
        this.editView = editView;
        onMarkdownEdited();
    }

    @Override
    public void setPreviewView(MarkdownPreviewView previewView) {
        this.previewView = previewView;
    }

    @Override
    public void saveMarkdown(MarkdownSavedListener listener, String name, OutputStream outputStream) {
        Runnable fileSaver = () -> {
            boolean result;
            synchronized (fileLock) {
                result = file.save(name, outputStream);
            }
            if (listener != null) {
                listener.saveComplete(result);
            }
            MarkdownEditView currentEditView = editView;
            if (currentEditView != null) {
                synchronized (fileLock) {
                    currentEditView.setTitle(file.getName());
                }
                currentEditView.onFileSaved(result);
            }
        };
        fileHandler.post(fileSaver);
    }

    @Override
    public void onMarkdownEdited(String markdown) {
        setMarkdown(markdown);
        Runnable generateMarkdown = () -> {
            MarkdownPreviewView currentPreviewView = previewView;
            if (currentPreviewView != null)
                currentPreviewView.updatePreview(generateHTML());
        };
        fileHandler.post(generateMarkdown);
    }

    @Override
    public String generateHTML() {
        return generateHTML(getMarkdown());
    }

    @Override
    public String generateHTML(String markdown) {
        AndDown andDown = new AndDown();
        int HOEDOWN_FLAGS = AndDown.HOEDOWN_EXT_STRIKETHROUGH | AndDown.HOEDOWN_EXT_TABLES |
                AndDown.HOEDOWN_EXT_UNDERLINE | AndDown.HOEDOWN_EXT_SUPERSCRIPT |
                AndDown.HOEDOWN_EXT_FENCED_CODE;
        return andDown.markdownToHtml(markdown, HOEDOWN_FLAGS, 0);
    }

    @Override
    public void onMarkdownEdited() {
        onMarkdownEdited(getMarkdown());
    }

    @Override
    public String getFileName() {
        synchronized (fileLock) {
            return file.getName();
        }
    }

    @Override
    public void setFileName(String name) {
        synchronized (fileLock) {
            file.setName(name);
        }
    }

    @Override
    public String getMarkdown() {
        synchronized (fileLock) {
            return file.getContent();
        }
    }

    @Override
    public void setMarkdown(String markdown) {
        synchronized (fileLock) {
            file.setContent(markdown);
        }
    }

    @Override
    public void loadFromUri(Context context, Uri fileUri) {
        try {
            InputStream in =
                    context.getContentResolver().openInputStream(fileUri);
            String fileName = null;
            if ("content".equals(fileUri.getScheme())) {
                Cursor retCur = context.getContentResolver()
                        .query(fileUri, null, null, null, null);
                if (retCur != null) {
                    int nameIndex = retCur
                            .getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    retCur.moveToFirst();
                    fileName = retCur.getString(nameIndex);
                    retCur.close();
                }
            } else if ("file".equals(fileUri.getScheme())) {
                fileName = fileUri.getLastPathSegment();
            }
            if (fileName == null) {
                fileName = "Untitled.md";
            }
            loadMarkdown(fileName, in);
        } catch (Exception e) {
            errorHandler.reportException(e);
            MarkdownEditView currentEditView = editView;
            if (currentEditView != null) {
                currentEditView.onFileLoaded(false);
            }
        }
    }
}
