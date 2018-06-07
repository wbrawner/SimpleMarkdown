package com.wbrawner.simplemarkdown.view.overrides;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class SafeListView extends ListView {
    public SafeListView(Context context) {
        super(context);
    }

    public SafeListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SafeListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDetachedFromWindow() {
        try {
            super.onDetachedFromWindow();
        } catch (Exception ignored) {
            // TODO: report this?
        }
    }
}
