package com.wbrawner.simplemarkdown.model

import java.io.InputStream
import java.io.OutputStream
import java.io.Reader

/**
 * This class serves as a wrapper to manage the manage the file input and output operations, as well
 * as to keep track of the data itself in memory.
 */
class MarkdownFile(var name: String = "Untitled.md", var content: String = "") {

    fun load(name: String, inputStream: InputStream): Boolean {
        this.name = name
        return try {
            this.content = inputStream.reader().use(Reader::readText)
            true
        } catch (e: Throwable) {
            false
        }
    }

    fun save(name: String, outputStream: OutputStream): Boolean {
        this.name = name
        return try {
            outputStream.writer().use {
                it.write(this.content)
            }
            true
        } catch (e: Throwable) {
            false
        }
    }
}
