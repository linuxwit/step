package com.witleaf.step.fragments;


import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.witleaf.step.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddFriendFragment extends DialogFragment {


    public AddFriendFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_add_friend, container, false);
    }


}
