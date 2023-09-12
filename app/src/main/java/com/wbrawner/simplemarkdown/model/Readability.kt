package com.wbrawner.simplemarkdown.model

import java.util.*

class Readability(private val content: String) {

    fun sentences(): List<Sentence> {
        val list = ArrayList<Sentence>()
        var startOfSentance = 0
        var lineBuilder = StringBuilder()
        for (i in content.indices) {
            val c = content[i] + ""
            if (DELIMS.contains(c)) {
                list.add(Sentence(content, startOfSentance, i))
                startOfSentance = i + 1
                lineBuilder = StringBuilder()
            } else {
                lineBuilder.append(c)
            }
        }
        val line = lineBuilder.toString()
        if (line.isNotEmpty()) list.add(Sentence(content, startOfSentance, content.length))
        return list
    }

    companion object {
        private const val DELIMS = ".!?\n"
    }
}
