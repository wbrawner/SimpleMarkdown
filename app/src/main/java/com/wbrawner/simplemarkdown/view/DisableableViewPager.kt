package com.wbrawner.simplemarkdown.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class DisableableViewPager : ViewPager {
    private var isSwipeLocked = false

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return !isSwipeLocked && super.onInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return !isSwipeLocked && super.onTouchEvent(ev)
    }

    fun setSwipeLocked(locked: Boolean) {
        this.isSwipeLocked = locked
    }
}
