package com.witleaf.step.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;
import com.witleaf.step.R;
import com.witleaf.step.adapters.CardPagerAdapter;
import com.witleaf.step.models.UserCard;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * A simple {@link Fragment} subclass.
 */
public class BuddiesFragment extends Fragment {
    private CardPagerAdapter cardPagerAdapter = null;
    private ArrayList<UserCard> mCardInfoList = null;

    @InjectView(R.id.tabs)
    PagerSlidingTabStrip mTabs;
    @InjectView(R.id.buddiesPager)
    ViewPager mBuddiesPager;
    private final String tag = "BuddiesFragment";

    public BuddiesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(tag, "进入好友列表");
        final View rootView = inflater.inflate(R.layout.fragment_buddies, container, false);
        ButterKnife.inject(this, rootView);
        initUserCardList();
        cardPagerAdapter = new CardPagerAdapter(getChildFragmentManager(), mCardInfoList);
        mBuddiesPager.setAdapter(cardPagerAdapter);
        mTabs.setViewPager(mBuddiesPager);
        return rootView;
    }

    private void setPager() {
        mBuddiesPager.setAdapter(cardPagerAdapter);
        mTabs.setViewPager(mBuddiesPager);
    }

    private void initUserCardList() {
        mCardInfoList = new ArrayList<UserCard>();
        mCardInfoList.add(new UserCard("1", "老公"));
        mCardInfoList.add(new UserCard("2", "老妈"));
        mCardInfoList.add(new UserCard("3", "女儿"));
    }

    private void addCardFragment() {
        mCardInfoList.add(new UserCard("4", "铁哥们"));
        cardPagerAdapter = new CardPagerAdapter(getChildFragmentManager(), mCardInfoList);
        mBuddiesPager.setAdapter(cardPagerAdapter);
        mTabs.setViewPager(mBuddiesPager);
    }
}
