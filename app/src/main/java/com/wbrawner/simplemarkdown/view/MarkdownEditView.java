package com.wbrawner.simplemarkdown.view;

public interface MarkdownEditView {
    String getMarkdown();
    void setMarkdown(String markdown);
    void setTitle(String title);
    void showFileSavedMessage();
    void showFileSavedError(int code);
    void showFileLoadedMessage();
    void showFileLoadeddError(int code);
}
