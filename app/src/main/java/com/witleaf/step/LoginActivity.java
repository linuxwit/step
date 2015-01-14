package com.witleaf.step;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;

import com.witleaf.step.activity.BaseActivity;
import com.witleaf.step.fragments.LoginFragment;
import com.witleaf.xmpp.XmppService;
import com.witleaf.xmpp.XmppTools;


public class LoginActivity extends BaseActivity {

    private final String tag = "LoginActivity";


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(tag, "收到广播" + action + ":" + intent.getStringExtra("current_action"));
            show(false, "登录成功");
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_view, LoginFragment.newInstance());
        fragmentTransaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(XmppService.ACTION_XMPP_CONNECTION_CHAGE);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }


    public void onClick(View view) {


        switch (view.getId()) {
            case R.id.sign_in_button:
                SettingsManager settings = SettingsManager.getSettingsManager(this);
                settings.saveSetting("serverHost", "192.168.80.88");
                settings.setLogin("hanhan");
                settings.setPassword("123456");

                Log.d(tag, "点击Login" + settings.serverHost);
                Intent i = new Intent("Connect", null, getApplicationContext(), XmppService.class);
                startService(i);
                break;
            case R.id.register_button:
                XmppTools.send("I am here", "bobo@lovejog.com", this);
                break;
        }
    }

    public void show(final boolean show, String message) {
        showProgress(show, message);
    }
}
