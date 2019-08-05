package com.wbrawner.simplemarkdown.view.adapter

/**
 * Created by billy on 7/29/2017.
 */

import android.content.Context
import android.content.res.Configuration

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import com.wbrawner.simplemarkdown.R
import com.wbrawner.simplemarkdown.view.fragment.EditFragment
import com.wbrawner.simplemarkdown.view.fragment.PreviewFragment

class EditPagerAdapter(fm: FragmentManager, private val mContext: Context) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        when (position) {
            FRAGMENT_EDIT -> return EditFragment()
            FRAGMENT_PREVIEW -> return PreviewFragment()
        }
        return null
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
        return mContext.getString(stringId)
    }

    override fun getPageWidth(position: Int): Float {
        return if (mContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            0.5f
        } else super.getPageWidth(position)
    }

    companion object {
        val FRAGMENT_EDIT = 0
        val FRAGMENT_PREVIEW = 1
        val NUM_PAGES = 2
    }
}
