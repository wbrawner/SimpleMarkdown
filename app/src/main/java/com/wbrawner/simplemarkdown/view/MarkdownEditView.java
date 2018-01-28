package com.wbrawner.simplemarkdown.view;

public interface MarkdownEditView {
    String getMarkdown();
    void setMarkdown(String markdown);
    void setTitle(String title);

    void onFileSaved(boolean success);

    void onFileLoaded(boolean success);
}
