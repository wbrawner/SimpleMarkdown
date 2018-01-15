package com.wbrawner.simplemarkdown.presentation;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.util.Log;

import com.commonsware.cwac.anddown.AndDown;
import com.wbrawner.simplemarkdown.model.MarkdownFile;
import com.wbrawner.simplemarkdown.view.MarkdownEditView;
import com.wbrawner.simplemarkdown.view.MarkdownPreviewView;

import java.io.File;
import java.io.InputStream;

public class MarkdownPresenterImpl implements MarkdownPresenter {
    private MarkdownFile file;
    private MarkdownEditView editView;
    private MarkdownPreviewView previewView;
    private String TAG = MarkdownPresenterImpl.class.getSimpleName();
    private int HOEDOWN_FLAGS =
            AndDown.HOEDOWN_EXT_STRIKETHROUGH | AndDown.HOEDOWN_EXT_TABLES |
                    AndDown.HOEDOWN_EXT_UNDERLINE | AndDown.HOEDOWN_EXT_SUPERSCRIPT |
                    AndDown.HOEDOWN_EXT_FENCED_CODE;
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
            int result = this.file.load();
            if (editView != null) {
                if (result == MarkdownFile.SUCCESS) {
                    editView.setMarkdown(getMarkdown());
                    onMarkdownEdited();
                } else {
                    editView.showFileLoadeddError(result);
                }
            }
        };
        fileHandler.post(fileLoader);
    }

    @Override
    public void loadMarkdown(File file) {
        Runnable fileLoader = () -> {
            int result = this.file.load(file);
            if (editView != null) {
                if (result == MarkdownFile.SUCCESS) {
                    editView.setMarkdown(getMarkdown());
                    onMarkdownEdited();
                } else {
                    editView.showFileLoadeddError(result);
                }
            }
        };
        fileHandler.post(fileLoader);
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
        fileHandler.post(fileLoader);
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
        };
        fileHandler.post(fileLoader);
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
        fileHandler.post(fileSaver);
    }

    @Override
    public void saveMarkdown() {
        file.save();
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

    @Override
    public void loadTempFile() {
        String tempFileName = "auto-" + getFileName();
        String tempFilePath = editView.getTempFilePath();
        loadMarkdown(tempFilePath + tempFileName);
        MarkdownFile.deleteTempFile(tempFilePath + tempFileName);
    }
}
