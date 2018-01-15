package com.wbrawner.simplemarkdown.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Locale;
import java.util.Scanner;

import static android.content.ContentValues.TAG;

/**
 * This class serves as a wrapper to manage the manage the file input and output operations, as well
 * as to keep track of the data itself in memory.
 */
public class MarkdownFile {
    public static final int SUCCESS = 0;
    public static final int FILE_EXISTS = 1;
    public static final int FILE_NOT_EXISTS = 2;
    public static final int READ_ERROR = 3;
    public static final int WRITE_ERROR = 4;
    public static final int PARAMETERS_MISSING = 5;

    public static void setDefaultRootDir(String defaultRootDir) {
        MarkdownFile.defaultRootDir = defaultRootDir;
    }

    private static String defaultRootDir = "";
    private String name;
    private String path;
    private String content;

    public MarkdownFile(String name, String path, String content) {
        this.name = name;
        this.path = path;
        this.content = content;
    }

    public MarkdownFile(String path) {
        int code = load(path);
        if (code != SUCCESS) {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
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

    public void setPath(String path) {
        this.path = path;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int load(InputStream in) {
        StringBuilder sb = new StringBuilder();
        Scanner s = new java.util.Scanner(in).useDelimiter("\\n");
        while (s.hasNext()) {
            sb.append(s.next() + "\n");
        }
        this.content = sb.toString();
        return SUCCESS;
    }

    public int load(String path) {
        return load(new File(path));
    }

    public int load(File markdownFile) {
        System.out.println("Attempting to load file from " + markdownFile.getAbsolutePath());
        int code;
        if (markdownFile.exists() && markdownFile.canRead()) {
            BufferedReader reader = null;
            try {
                this.name = markdownFile.getName();
                this.path = markdownFile.getParentFile().getAbsolutePath();
                StringBuilder sb = new StringBuilder();
                String line;
                reader = new BufferedReader(new FileReader(markdownFile));
                while ((line = reader.readLine()) != null)
                    sb.append(line + "\n");
                this.content = sb.toString();
                code = SUCCESS;
                System.out.println(String.format(
                        Locale.ENGLISH,
                        "Successfully loaded file from %s",
                        markdownFile.getAbsolutePath()
                ));
                System.out.println(String.format(
                        Locale.ENGLISH,
                        "File contents %s",
                        this.content
                ));
            } catch (FileNotFoundException e) {
                code = FILE_NOT_EXISTS;
            } catch (IOException e) {
                code = READ_ERROR;
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // closing the reader failed
                }
            }
        } else {
            code = READ_ERROR;
        }
        return code;
    }

    public int load() {
        if (this.name.isEmpty())
            return PARAMETERS_MISSING;
        return load(getFullPath());
    }

    public int save(String path) {
        System.out.println("Attempting to save file to " + path);
        int code;
        File markdownFile = new File(path);
        if (!markdownFile.exists()) {
            try {
                markdownFile.createNewFile();
            } catch (IOException e) {
                return WRITE_ERROR;
            }
        }
        if (markdownFile.canWrite()) {
            OutputStreamWriter writer = null;
            try {
                writer = new OutputStreamWriter(
                        new FileOutputStream(markdownFile)
                );
                writer.write(this.content);
                code = SUCCESS;
                System.out.println(String.format(
                        Locale.ENGLISH,
                        "File successfully saved to %s",
                        path
                ));
            } catch (IOException e) {
                code = WRITE_ERROR;
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // closing the reader failed
                }
            }
        } else {
            code = WRITE_ERROR;
        }
        this.name = markdownFile.getName();
        this.path = markdownFile.getParentFile().getAbsolutePath();
        return code;
    }

    public int save() {
        if (this.name.isEmpty())
            return PARAMETERS_MISSING;
        return save(this.getFullPath());
    }

    public static int fileExists(String path) {
        if (new File(path).exists())
            return FILE_EXISTS;
        return FILE_NOT_EXISTS;
    }

    public int fileExists() {
        if (!this.name.isEmpty())
            return fileExists(getFullPath());
        return PARAMETERS_MISSING;
    }

    public static void deleteTempFile(String s) {
        File tempFile = new File(s);
        if (tempFile.exists()) {
            try {
                tempFile.delete();
            } catch (Exception e) {
            }
        }
    }
}
