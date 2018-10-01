package com.wbrawner.simplemarkdown.model;

import com.wbrawner.simplemarkdown.utility.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * This class serves as a wrapper to manage the manage the file input and output operations, as well
 * as to keep track of the data itself in memory.
 */
public class MarkdownFile {
    private static String defaultRootDir = "";
    private String name;
    private String path;
    private String content;

    public MarkdownFile(String name, String path, String content) {
        this.name = name;
        if (path == null || path.isEmpty()) {
            path = defaultRootDir;
        }
        this.path = path;
        this.content = content;
    }

    public MarkdownFile(String path) {
        if (load(path)) {
            this.name = path.substring(
                    path.lastIndexOf("/") + 1
            );
            this.path = path.substring(
                    0,
                    path.lastIndexOf("/")
            );
            this.content = "";
        }
    }

    public MarkdownFile() {
        this.name = "Untitled.md";
        this.path = "";
        this.content = "";
    }

    public static void setDefaultRootDir(String defaultRootDir) {
        MarkdownFile.defaultRootDir = defaultRootDir;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFullPath() {
        String fullPath;

        if (this.path.isEmpty()) {
            this.path = defaultRootDir;
        }

        if (this.path.endsWith(this.name)) {
            return this.path;
        }

        if (!this.path.endsWith("/")) {
            fullPath = this.path + "/";
        } else {
            fullPath = this.path;
        }

        return fullPath + this.name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean load(InputStream in) {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            this.content = sb.toString();
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            Utils.closeQuietly(reader);
        }
    }

    private boolean load(String path) {
        return load(new File(path));
    }

    private boolean load(File markdownFile) {
        if (!markdownFile.exists() || !markdownFile.canRead()) {
            return false;
        }

        try {
            this.name = markdownFile.getName();
            this.path = markdownFile.getParentFile().getAbsolutePath();
            return load(new FileInputStream(markdownFile));
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    public boolean load() {
        return !this.name.isEmpty() && load(getFullPath());
    }

    public boolean save(String path) {
        if (path == null) {
            path = this.getFullPath();
        }

        File markdownFile = new File(path);
        File parentFile = markdownFile.getParentFile();
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            return false;
        }

        if (!markdownFile.exists()) {
            try {
                if (!markdownFile.createNewFile()) {
                    return false;
                }
            } catch (IOException e) {
                return false;
            }
        }

        if (!markdownFile.canWrite()) {
            return false;
        }

        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(
                    new FileOutputStream(markdownFile)
            );
            writer.write(this.content);
        } catch (IOException e) {
            return false;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // closing the reader failed
                }
            }
        }

        this.name = markdownFile.getName();
        this.path = markdownFile.getParentFile().getAbsolutePath();
        return true;
    }
}
