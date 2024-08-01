package com.wbrawner.simplemarkdown.utility

import android.util.Log
import com.wbrawner.simplemarkdown.utility.PersistentTree.Companion.create
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.PrintStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * A [Timber.Tree] implementation that persists all logs to disk for retrieval later. Create
 * instances via [create] instead of calling the constructor directly.
 */
class PersistentTree private constructor(
    private val coroutineScope: CoroutineScope,
    private val logFile: File
) : Timber.Tree() {
    private val dateFormat = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat =
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val timestamp = dateFormat.get()!!.format(System.currentTimeMillis())
        coroutineScope.launch(Dispatchers.IO) {
            val priorityLetter = when (priority) {
                Log.ASSERT -> "A"
                Log.DEBUG -> "D"
                Log.ERROR -> "E"
                Log.INFO -> "I"
                Log.VERBOSE -> "V"
                Log.WARN -> "W"
                else -> "U"
            }
            FileOutputStream(logFile, true).use { stream ->
                stream.bufferedWriter().use {
                    it.appendLine("$timestamp $priorityLetter/${tag ?: "SimpleMarkdown"}: $message")
                }
                t?.let {
                    PrintStream(stream).use { pStream ->
                        it.printStackTrace(pStream)
                    }
                }
            }
        }
    }

    init {
        log(Log.INFO, "Persistent logging initialized, writing contents to ${logFile.absolutePath}")
    }

    companion object {
        /**
         * Create a new instance of a [PersistentTree].
         * @param logDir A [File] pointing to a directory where the log files should be stored. Will be
         * created if it doesn't exist.
         * @throws IllegalArgumentException if [logDir] is a file instead of a directory
         * @throws IOException if the directory does not exist or cannot be
         * created/written to
         */
        @Throws(IllegalArgumentException::class, IOException::class)
        suspend fun create(coroutineScope: CoroutineScope, logDir: File): PersistentTree = withContext(Dispatchers.IO) {
            if (!logDir.mkdirs() && !logDir.isDirectory)
                throw IllegalArgumentException("Unable to create log directory at ${logDir.absolutePath}")
            val timestamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(Date())
            val logFile = File(logDir, "persistent-log-$timestamp.log")
            if (!logFile.createNewFile())
                throw IOException("Unable to create logFile at ${logFile.absolutePath}")
            PersistentTree(coroutineScope, logFile)
        }
    }
}