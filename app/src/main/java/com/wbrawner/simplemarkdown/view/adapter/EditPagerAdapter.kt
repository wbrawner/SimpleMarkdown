package com.wbrawner.simplemarkdown.view.adapter

import android.content.Context
import android.content.res.Configuration
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.wbrawner.simplemarkdown.R
import com.wbrawner.simplemarkdown.view.fragment.EditFragment
import com.wbrawner.simplemarkdown.view.fragment.PreviewFragment

class EditPagerAdapter(fm: FragmentManager, private val context: Context)
    : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT), ViewPager.OnPageChangeListener {

    private val editFragment = EditFragment()
    private val previewFragment = PreviewFragment()

    override fun getItem(position: Int): Fragment {
        return when (position) {
            FRAGMENT_EDIT -> editFragment
            FRAGMENT_PREVIEW -> previewFragment
            else -> throw IllegalStateException("Attempting to get fragment for invalid page number")
        }
    }

    override fun getCount(): Int {
        return NUM_PAGES
    }

    override fun getPageTitle(position: Int): CharSequence? {
        var stringId = 0
        when (position) {
            FRAGMENT_EDIT -> stringId = R.string.action_edit
            FRAGMENT_PREVIEW -> stringId = R.string.action_preview
        }
        return context.getString(stringId)
    }

    override fun getPageWidth(position: Int): Float {
        return if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            0.5f
        } else {
            super.getPageWidth(position)
        }
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        when (position) {
            FRAGMENT_EDIT -> {
                editFragment.onSelected()
            }
            FRAGMENT_PREVIEW -> {
                editFragment.onDeselected()
            }
        }
    }

    companion object {
        const val FRAGMENT_EDIT = 0
        const val FRAGMENT_PREVIEW = 1
        const val NUM_PAGES = 2
    }
}
