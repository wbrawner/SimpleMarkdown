package com.wbrawner.simplemarkdown;

/**
 * Created by billy on 7/29/2017.
 */

import android.content.Context;
import android.content.res.Configuration;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import static com.wbrawner.simplemarkdown.MainActivity.FRAGMENT_EDIT;
import static com.wbrawner.simplemarkdown.MainActivity.FRAGMENT_PREVIEW;
import static com.wbrawner.simplemarkdown.MainActivity.NUM_PAGES;

class EditPagerAdapter extends FragmentPagerAdapter {
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
