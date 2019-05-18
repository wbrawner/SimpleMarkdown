package com.wbrawner.simplemarkdown.model;

import com.crashlytics.android.Crashlytics;
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

    public MarkdownFile(String name, String content) {
        this.name = name;
        this.content = content;
    }


    public MarkdownFile() {
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
        } catch (IOException e) {
            Crashlytics.logException(e);
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
        } catch (IOException e) {
            Crashlytics.logException(e);
            return false;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    Crashlytics.logException(e);
                }
            }
        }
        return true;
    }
}
