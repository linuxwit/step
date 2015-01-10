package com.witleaf.step.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.witleaf.step.R;
import com.witleaf.xmpp.XmppTools;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by witwave on 2014/12/18.
 */
public class CardFragment extends Fragment {

    private static final String ARG_POSITION = "position";

    private static final String tag = "CardFragment";

    @InjectView(R.id.textView)
    TextView textView;

    @InjectView(R.id.textView)
    TextView textView2;
    @InjectView(R.id.circleLinearLayout)
    LinearLayout circleLinearLayout;
    @InjectView(R.id.btnMic)
    ImageButton btnMic;

    private int position;

    public static CardFragment newInstance(int position) {
        CardFragment f = new CardFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);
        Log.d(tag, "进入用户卡");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(tag, "创建用户卡");
        final View rootView = inflater.inflate(R.layout.fragment_card, container, false);
        ButterKnife.inject(this, rootView);

        circleLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XmppTools.send("hello,I am here", "bobo@lovejog.com", v.getContext());
                Toast.makeText(v.getContext(), "onclick", Toast.LENGTH_SHORT).show();
            }
        });


/*        circleLinearLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                textView.setVisibility(View.GONE);
                textView2.setVisibility(View.GONE);

                btnMic.setVisibility(View.VISIBLE);
                Toast.makeText(v.getContext(), "long click", Toast.LENGTH_SHORT).show();
                AsyncMicTask task = new AsyncMicTask(rootView.getContext(), btnMic, textView, textView2, new UserCard("1", "hh"));
                task.execute();
                return true;
            }


        });

        circleLinearLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                btnMic.setVisibility(View.GONE);
                textView.setVisibility(View.VISIBLE);
                textView2.setVisibility(View.VISIBLE);
                Toast.makeText(v.getContext(), "onFocusChange", Toast.LENGTH_SHORT).show();
            }
        });*/


/*
        circleLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnMic.setVisibility(View.GONE);
                textView.setVisibility(View.VISIBLE);
                textView2.setVisibility(View.VISIBLE);
                Toast.makeText(v.getContext(), "click", Toast.LENGTH_SHORT).show();
            }
        });
*/

        //  ViewCompat.setElevation(rootView, 50);
        textView.setText("1,000");
        return rootView;
    }


}
