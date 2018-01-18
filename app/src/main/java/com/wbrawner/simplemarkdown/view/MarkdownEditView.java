package com.wbrawner.simplemarkdown.view;

/**
 * Created by billy on 8/22/17.
 */

public interface MarkdownEditView {
    String getMarkdown();
    void setMarkdown(String markdown);

    void showFileSavedMessage();
    void showFileSavedError(int code);
    void showFileLoadedMessage();
    void showFileLoadeddError(int code);
}
