package com.wbrawner.simplemarkdown;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.commonsware.cwac.anddown.AndDown;

public class MarkdownViewModel extends ViewModel {

    private MutableLiveData<String> markdownLiveData;
    private MutableLiveData<String> htmlLiveData = new MutableLiveData<>();;

    public MarkdownViewModel() {
        markdownLiveData = new MutableLiveData<>();
    }

    public void updateMarkdown(String data) {
        if (markdownLiveData == null)
            markdownLiveData = new MutableLiveData<>();
        markdownLiveData.postValue(data);
        Runnable generateMarkdown = () -> {
            AndDown andDown = new AndDown();
            int hoedownFlags =
                    AndDown.HOEDOWN_EXT_STRIKETHROUGH | AndDown.HOEDOWN_EXT_TABLES |
                            AndDown.HOEDOWN_EXT_UNDERLINE | AndDown.HOEDOWN_EXT_SUPERSCRIPT |
                            AndDown.HOEDOWN_EXT_FENCED_CODE;
            htmlLiveData.postValue(andDown.markdownToHtml(markdownLiveData.getValue(), hoedownFlags, 0));
        };
        if (markdownLiveData.getValue() != null)
            generateMarkdown.run();
    }

    public LiveData<String> getHtml() {
        return htmlLiveData;
    }
}
