package com.wbrawner.simplemarkdown.view;

import java.io.File;

public interface MarkdownEditView {
    String getMarkdown();
    void setMarkdown(String markdown);
    void setTitle(String title);

    void onFileSaved(boolean success);

    void onFileLoaded(boolean success);
}
