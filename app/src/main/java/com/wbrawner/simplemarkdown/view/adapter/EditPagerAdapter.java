package com.wbrawner.simplemarkdown.view.adapter;

/**
 * Created by billy on 7/29/2017.
 */

import android.content.Context;
import android.content.res.Configuration;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.wbrawner.simplemarkdown.R;
import com.wbrawner.simplemarkdown.view.fragment.EditFragment;
import com.wbrawner.simplemarkdown.view.fragment.PreviewFragment;

public class EditPagerAdapter extends FragmentPagerAdapter {
    public static final int FRAGMENT_EDIT = 0;
    public static final int FRAGMENT_PREVIEW = 1;
    public static final int NUM_PAGES = 2;

    private Context mContext;

    public EditPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case FRAGMENT_EDIT:
                return new EditFragment();
            case FRAGMENT_PREVIEW:
                return new PreviewFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        int stringId = 0;
        switch (position) {
            case FRAGMENT_EDIT:
                stringId = R.string.action_edit;
                break;
            case FRAGMENT_PREVIEW:
                stringId = R.string.action_preview;
                break;
        }
        return mContext.getString(stringId);
    }

    @Override
    public float getPageWidth(int position) {
        if (mContext.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE) {
            return 0.5f;
        }
        return super.getPageWidth(position);
    }
}
