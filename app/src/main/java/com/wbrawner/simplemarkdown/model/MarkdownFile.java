package com.wbrawner.simplemarkdown.model;

import com.wbrawner.simplemarkdown.utility.ErrorHandler;
import com.wbrawner.simplemarkdown.utility.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * This class serves as a wrapper to manage the manage the file input and output operations, as well
 * as to keep track of the data itself in memory.
 */
public class MarkdownFile {
    private String name;
    private String content;
    private final ErrorHandler errorHandler;

    public MarkdownFile(ErrorHandler errorHandler, String name, String content) {
        this.errorHandler = errorHandler;
        this.name = name;
        this.content = content;
    }


    public MarkdownFile(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        this.name = "Untitled.md";
        this.content = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean load(String name, InputStream in) {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            this.name = name;
            this.content = sb.toString();
            return true;
        } catch (IOException ignored) {
            return false;
        } finally {
            Utils.closeQuietly(reader);
        }
    }

    public boolean save(String name, OutputStream outputStream) {
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(outputStream);
            writer.write(this.content);
            this.name = name;
        } catch (IOException ignored) {
            return false;
        } finally {
            Utils.closeQuietly(writer);
        }
        return true;
    }
}
