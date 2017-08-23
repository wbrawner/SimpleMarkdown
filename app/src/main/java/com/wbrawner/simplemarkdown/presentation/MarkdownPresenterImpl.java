package com.wbrawner.simplemarkdown.presentation;

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

    public MarkdownPresenterImpl(MarkdownFile file) {
        this.file = file;
    }

    @Override
    public void resume() {}

    @Override
    public void pause() {
        saveMarkdown("");
    }

    @Override
    public void loadMarkdown(String filePath) {
        Runnable fileLoader = () -> {
            int result = file.load(filePath);
            if (result == MarkdownFile.SUCCESS) {
                editView.setMarkdown(getMarkdown());
                onMarkdownEdited();
            } else {
                editView.showFileLoadeddError(result);
            }
        };
        fileLoader.run();
    }

    @Override
    public void loadMarkdown(InputStream in) {
        Runnable fileLoader = () -> {
            int result = file.load(in);
            if (result == MarkdownFile.SUCCESS) {
                editView.setMarkdown(getMarkdown());
                onMarkdownEdited();
            } else {
                editView.showFileLoadeddError(result);
            }
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
            if (code == MarkdownFile.SUCCESS) {
                editView.showFileSavedMessage();
            } else {
                editView.showFileSavedError(code);
            }
        };
        fileSaver.run();
    }

    @Override
    public void onMarkdownEdited(String markdown) {
        setMarkdown(markdown);
        Runnable generateMarkdown = () -> {
            AndDown andDown = new AndDown();
            int hoedownFlags =
                    AndDown.HOEDOWN_EXT_STRIKETHROUGH | AndDown.HOEDOWN_EXT_TABLES |
                            AndDown.HOEDOWN_EXT_UNDERLINE | AndDown.HOEDOWN_EXT_SUPERSCRIPT |
                            AndDown.HOEDOWN_EXT_FENCED_CODE;
            if (previewView != null)
                previewView.updatePreview(andDown.markdownToHtml(markdown, hoedownFlags, 0));
        };
        generateMarkdown.run();
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
}
