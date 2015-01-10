package com.witleaf.step.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.witleaf.step.fragments.CardFragment;
import com.witleaf.step.models.UserCard;

import java.util.ArrayList;

public class CardPagerAdapter extends FragmentPagerAdapter {
    private final String tag = "CardPagerAdapter";

    private ArrayList<UserCard> cardInfoList = null;

    public CardPagerAdapter(FragmentManager fm, ArrayList<UserCard> cardInfoList) {
        super(fm);

        if (cardInfoList == null) {
            Log.d(tag, "cardInfoList is null");
            return;
        }
        Log.d(tag, "set size:" + cardInfoList.size());
        this.cardInfoList = cardInfoList;
    }


    @Override
    public CharSequence getPageTitle(int position) {
        Log.d(tag, "size:" + cardInfoList.size() + ",position:" + position);
        UserCard u = cardInfoList.get(position);
        if (u != null)
            return u.getName();
        return "Unknow";
    }

    @Override
    public int getCount() {
        if (cardInfoList != null)
            return cardInfoList.size();
        return 0;
    }

    @Override
    public Fragment getItem(int position) {
        return CardFragment.newInstance(position);
    }
}