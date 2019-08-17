package com.wbrawner.simplemarkdown

import com.wbrawner.simplemarkdown.model.Readability
import com.wbrawner.simplemarkdown.model.Sentence
import eu.crydee.syllablecounter.SyllableCounter
import org.junit.Assert.assertEquals
import org.junit.Test

class ReadabilityTest {

    @Test
    fun break_content_into_sentances() {
        val sc = SyllableCounter()
        assertEquals(4, sc.count("facility").toLong())
    }

    @Test
    fun can_break_text_into_sentences_with_indexes() {
        val content = "Hop on pop. I am a fish. This is a test."
        val readability = Readability(content)
        val sentenceList = readability.sentences()

        assertEquals(3, sentenceList.size.toLong())

        val hopOnPop = sentenceList[0]
        assertEquals(hopOnPop.toString(), "Hop on pop")
        assertEquals(0, hopOnPop.start().toLong())
        assertEquals(10, hopOnPop.end().toLong())

        val iAmAFish = sentenceList[1]
        assertEquals(iAmAFish.toString(), "I am a fish")
        assertEquals(12, iAmAFish.start().toLong())
        assertEquals(23, iAmAFish.end().toLong())

        val thisIsATest = sentenceList[2]
        assertEquals(thisIsATest.toString(), "This is a test")
        assertEquals(25, thisIsATest.start().toLong())
        assertEquals(39, thisIsATest.end().toLong())
    }

    @Test
    fun get_syllable_count_for_sentence() {
        assertEquals(8, Sentence("This is the song that never ends").syllableCount().toLong())
        assertEquals(10, Sentence("facility facility downing").syllableCount().toLong())
    }
}
