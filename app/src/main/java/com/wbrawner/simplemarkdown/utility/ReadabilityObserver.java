package com.wbrawner.simplemarkdown.utility;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.wbrawner.simplemarkdown.model.Readability;
import com.wbrawner.simplemarkdown.model.Sentence;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class ReadabilityObserver implements Observer<String> {
    private EditText text;
    private String previousValue = "";

    public ReadabilityObserver(EditText text) {
        this.text = text;
    }

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onNext(String markdown) {
        long start = System.currentTimeMillis();
        if (markdown.length() < 1) return;
        if (previousValue.equals(markdown)) return;
        Readability readability = new Readability(markdown);
        SpannableString span = new SpannableString(markdown);
        for (Sentence sentence : readability.sentences()) {
            int color = Color.TRANSPARENT;
            if (sentence.syllableCount() > 25) color = Color.argb(100, 229, 232, 42);
            if (sentence.syllableCount() > 35) color = Color.argb(100, 193, 66, 66);
            span.setSpan(new BackgroundColorSpan(color), sentence.start(), sentence.end(), 0);
        }
        text.setTextKeepState(span, TextView.BufferType.SPANNABLE);
        previousValue = markdown;
        long timeTakenMs = System.currentTimeMillis() - start;
        Log.d("SimpleMarkdown", "Handled markdown in " + timeTakenMs + "ms");
    }

    @Override
    public void onError(Throwable e) {
        System.err.println("An error occurred while handling the markdown");
        e.printStackTrace();
        // TODO: report this?
    }

    @Override
    public void onComplete() {

    }
}
