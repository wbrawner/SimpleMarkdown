package com.wbrawner.simplemarkdown.presentation;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.OpenableColumns;

import com.commonsware.cwac.anddown.AndDown;
import com.wbrawner.simplemarkdown.model.MarkdownFile;
import com.wbrawner.simplemarkdown.utility.Utils;
import com.wbrawner.simplemarkdown.view.MarkdownEditView;
import com.wbrawner.simplemarkdown.view.MarkdownPreviewView;

import org.acra.ACRA;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MarkdownPresenterImpl implements MarkdownPresenter {
    private final Object fileLock = new Object();
    private MarkdownFile file;
    private volatile MarkdownEditView editView;
    private volatile MarkdownPreviewView previewView;
    private Handler fileHandler = new Handler();

    public MarkdownPresenterImpl(MarkdownFile file) {
        synchronized (fileLock) {
            this.file = file;
        }
    }

    @Override
    public File getFile() {
        return new File(file.getFullPath());
    }

    @Override
    public void loadMarkdown() {
        Runnable fileLoader = () -> {
            boolean result;
            synchronized (fileLock) {
                result = this.file.load();
            }

            MarkdownEditView currentEditView = editView;
            if (currentEditView != null) {
                currentEditView.onFileLoaded(result);
                currentEditView.setMarkdown(getMarkdown());
                onMarkdownEdited();
            }
        };
        fileHandler.post(fileLoader);
    }

    @Override
    public void loadMarkdown(File file) {
        try {
            InputStream in = new FileInputStream(file);
            loadMarkdown(file.getName(), in);
        } catch (FileNotFoundException e) {
            ACRA.getErrorReporter().handleException(e, false);
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
            MarkdownFile tmpFile = new MarkdownFile();
            if (tmpFile.load(in)) {
                tmpFile.setName(fileName);
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
            file = new MarkdownFile(newName, "", "");
        }
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
    public void saveMarkdown(MarkdownSavedListener listener, String filePath) {
        Runnable fileSaver = () -> {
            boolean result;
            synchronized (fileLock) {
                result = file.save(filePath);
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
    public void setRootDir(String path) {
        MarkdownFile.setDefaultRootDir(path);
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
                fileName = Utils.getDefaultFileName(context);
            }
            loadMarkdown(fileName, in);
        } catch (Exception e) {
            ACRA.getErrorReporter().handleException(e, false);
            MarkdownEditView currentEditView = editView;
            if (currentEditView != null) {
                currentEditView.onFileLoaded(false);
            }
        }
    }
}
