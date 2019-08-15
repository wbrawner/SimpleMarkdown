package com.wbrawner.simplemarkdown.model

import com.wbrawner.simplemarkdown.utility.ErrorHandler
import com.wbrawner.simplemarkdown.utility.Utils
import java.io.*

/**
 * This class serves as a wrapper to manage the manage the file input and output operations, as well
 * as to keep track of the data itself in memory.
 */
class MarkdownFile {
    var name: String? = null
    var content: String? = null
    private val errorHandler: ErrorHandler

    constructor(errorHandler: ErrorHandler, name: String, content: String) {
        this.errorHandler = errorHandler
        this.name = name
        this.content = content
    }


    constructor(errorHandler: ErrorHandler) {
        this.errorHandler = errorHandler
        this.name = "Untitled.md"
        this.content = ""
    }

    fun load(name: String, `in`: InputStream): Boolean {
        val sb = StringBuilder()
        var reader: BufferedReader? = null
        try {
            reader = BufferedReader(InputStreamReader(`in`))
            var line: String
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n')
            }
            this.name = name
            this.content = sb.toString()
            return true
        } catch (ignored: IOException) {
            return false
        } finally {
            Utils.closeQuietly(reader)
        }
    }

    fun save(name: String, outputStream: OutputStream): Boolean {
        var writer: OutputStreamWriter? = null
        try {
            writer = OutputStreamWriter(outputStream)
            writer.write(this.content)
            this.name = name
        } catch (ignored: IOException) {
            return false
        } finally {
            Utils.closeQuietly(writer)
        }
        return true
    }
}
