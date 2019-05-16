package com.wbrawner.simplemarkdown.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

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
