package com.wbrawner.simplemarkdown.model;

import eu.crydee.syllablecounter.SyllableCounter;

public class Sentence {

    private String sentence = "";
    private int start = 0;
    private int end = 0;

    private final static SyllableCounter sc = new SyllableCounter();

    public Sentence(String content, int start, int end){
        this.start = start;
        this.end = end;
        this.sentence = content.substring(start, end);

        trimStart();
    }

    public Sentence(String sentence){
        this.sentence = sentence;
    }

    private void trimStart() {
        while(sentence.startsWith(" ")){
            this.start++;
            sentence = sentence.substring(1);
        }
    }

    @Override
    public String toString() {
        return sentence;
    }

    public int start(){
        return start;
    }

    public int end(){
        return end;
    }

    public int syllableCount(){
        return sc.count(sentence);
    }
}
