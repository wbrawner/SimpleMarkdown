package com.wbrawner.simplemarkdown;

import com.wbrawner.simplemarkdown.model.Readability;
import com.wbrawner.simplemarkdown.model.Sentence;
import eu.crydee.syllablecounter.SyllableCounter;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ReadabilityTest {

    @Test
    public void break_content_into_sentances() {
        SyllableCounter sc = new SyllableCounter();
        assertEquals(4, sc.count("facility"));
    }

    @Test
    public void can_break_text_into_sentences_with_indexes(){
        String content = "Hop on pop. I am a fish. This is a test.";
        Readability readability = new Readability(content);
        List<Sentence> sentenceList = readability.sentences();

        assertEquals(3, sentenceList.size());

        Sentence hopOnPop = sentenceList.get(0);
        assertEquals(hopOnPop.toString(), "Hop on pop");
        assertEquals(0, hopOnPop.start());
        assertEquals(10, hopOnPop.end());

        Sentence iAmAFish = sentenceList.get(1);
        assertEquals(iAmAFish.toString(), "I am a fish");
        assertEquals(12, iAmAFish.start());
        assertEquals(23, iAmAFish.end());

        Sentence thisIsATest = sentenceList.get(2);
        assertEquals(thisIsATest.toString(), "This is a test");
        assertEquals(25, thisIsATest.start());
        assertEquals(39, thisIsATest.end());
    }


    @Test
    public void get_syllable_count_for_sentence(){
        assertEquals(8, new Sentence("This is the song that never ends").syllableCount());
        assertEquals(10, new Sentence("facility facility downing").syllableCount());
    }


}
