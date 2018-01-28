package com.wbrawner.simplemarkdown.presentation;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.OpenableColumns;

import com.commonsware.cwac.anddown.AndDown;
import com.wbrawner.simplemarkdown.Utils;
import com.wbrawner.simplemarkdown.model.MarkdownFile;
import com.wbrawner.simplemarkdown.view.MarkdownEditView;
import com.wbrawner.simplemarkdown.view.MarkdownPreviewView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MarkdownPresenterImpl implements MarkdownPresenter {
    private MarkdownFile file;
    private MarkdownEditView editView;
    private MarkdownPreviewView previewView;
    private Handler fileHandler = new Handler();

    public MarkdownPresenterImpl(MarkdownFile file) {
        this.file = file;
    }

    @Override
    public void loadMarkdown(String filePath) {
        File markdownFile = new File(filePath);
        loadMarkdown(markdownFile);
    }

    @Override
    public File getFile() {
        return new File(file.getFullPath());
    }

    @Override
    public void loadMarkdown() {
        Runnable fileLoader = () -> {
            boolean result = this.file.load();

            if (editView != null) {
                editView.onFileLoaded(result);
                editView.setMarkdown(getMarkdown());
                onMarkdownEdited();
            }
        };
        fileHandler.post(fileLoader);
    }

    @Override
    public void loadMarkdown(File file) {
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            loadMarkdown(in);
        } catch (FileNotFoundException e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace();
        } finally {
            Utils.closeQuietly(in);
        }
    }

    @Override
    public void loadMarkdown(InputStream in) {
        this.loadMarkdown(in, null);
    }

    @Override
    public void loadMarkdown(final InputStream in, final OnTempFileLoadedListener listener) {
        Runnable fileLoader = () -> {
            MarkdownFile tmpFile = new MarkdownFile();
            if (tmpFile.load(in)) {
                String html = generateHTML(tmpFile.getContent());
                if (listener != null) {
                    listener.onSuccess(html);
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
        if (this.editView != null) {
            this.file.setContent(this.editView.getMarkdown());
            this.editView.setTitle(newName);
            this.editView.setMarkdown("");
        }
        this.file = new MarkdownFile(newName, "", "");
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
            boolean result = file.save(filePath);
            if (listener != null) {
                listener.saveComplete(result);
            }
            if (editView != null) {
                editView.onFileSaved(result);
            }
        };
        fileHandler.post(fileSaver);
    }

    @Override
    public void onMarkdownEdited(String markdown) {
        setMarkdown(markdown);
        Runnable generateMarkdown = () -> {
            if (previewView != null)
                previewView.updatePreview(generateHTML());
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
        return file.getName();
    }

    @Override
    public void setFileName(String name) {
        file.setName(name);
    }

    @Override
    public void setRootDir(String path) {
        MarkdownFile.setDefaultRootDir(path);
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
                    retCur.close();
                }
            } else if (fileUri.getScheme().equals("file")) {
                setFileName(fileUri.getLastPathSegment());
            }
            loadMarkdown(in);
        } catch (Exception e) {
            if (editView != null) {
                editView.onFileLoaded(false);
            }
        }
    }
}
