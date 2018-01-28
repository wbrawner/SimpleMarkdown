package com.wbrawner.simplemarkdown.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class DisableableViewPager extends ViewPager {
    private boolean isSwipeLocked = false;

    public DisableableViewPager(@NonNull Context context) {
        super(context);
    }

    public DisableableViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return !isSwipeLocked && super.onInterceptTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return !isSwipeLocked && super.onTouchEvent(ev);
    }

    public void setSwipeLocked(boolean locked) {
        this.isSwipeLocked = locked;
    }
}
