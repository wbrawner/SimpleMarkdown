package com.wbrawner.simplemarkdown.model

import eu.crydee.syllablecounter.SyllableCounter

class Sentence(content: String, private var start: Int = 0, private val end: Int = 0) {

    private var sentence = content.substring(start, end)
    private val sc = SyllableCounter()

    init {
        trimStart()
    }

    private fun trimStart() {
        while (sentence.startsWith(" ")) {
            this.start++
            sentence = sentence.substring(1)
        }
    }

    override fun toString(): String {
        return sentence
    }

    fun start(): Int {
        return start
    }

    fun end(): Int {
        return end
    }

    fun syllableCount(): Int {
        return sc.count(sentence)
    }
}
