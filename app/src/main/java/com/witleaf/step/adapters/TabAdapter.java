package com.witleaf.step.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;

import java.util.ArrayList;

/**
 * Created by witwave on 2015/1/4.
 */
public class TabAdapter extends FragmentPagerAdapter {

    final ArrayList<Fragment> mFragments;


    public TabAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
        super(fm);
        this.mFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        if (mFragments.size() <= position) {
            return mFragments.get(position - 1);
        }
        if (position < 0) {
            return mFragments.get(0);
        }
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }
}
