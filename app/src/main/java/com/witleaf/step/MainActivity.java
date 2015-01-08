package com.witleaf.step;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.witleaf.step.adapters.TabAdapter;
import com.witleaf.step.fragments.BuddiesFragment;
import com.witleaf.step.fragments.LoginFragment;
import com.witleaf.xmpp.XmppService;
import com.witleaf.xmpp.XmppTools;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.util.dns.HostAddress;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.jivesoftware.smackx.ping.PingManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    private String tag = "MainActivity";
    private ViewPager mPager;
    private ArrayList<Fragment> mFragments = new ArrayList<Fragment>();

    private LoginFragment mLoginFragment = new LoginFragment();
    private BuddiesFragment mBuddiesFragment = new BuddiesFragment();

    private final BroadcastReceiver mXmppReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // Toast.makeText(getApplicationContext(), action, Toast.LENGTH_SHORT).show();
            Toast.makeText(getApplicationContext(), intent.getStringExtra("current_action"), Toast.LENGTH_SHORT).show();


            mFragments.remove(1);
            mPager.getAdapter().notifyDataSetChanged();

        }
    };


    private ServiceConnection mXmppServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(tag, "服务连接");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(tag, "服务连接断开");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPager = (ViewPager) findViewById(R.id.pager);
        mFragments.add(mLoginFragment);
        mFragments.add(mBuddiesFragment);

        mPager.setAdapter(new TabAdapter(getSupportFragmentManager(), mFragments));

    }


    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, XmppService.class);
        bindService(intent, mXmppServiceConn, Service.BIND_AUTO_CREATE);

        IntentFilter intentFilter = new IntentFilter(XmppService.ACTION_XMPP_CONNECTION_CHAGE);
        registerReceiver(mXmppReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mXmppReceiver);
        unbindService(mXmppServiceConn);
    }


    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btnLogin:
                SettingsManager settings = SettingsManager.getSettingsManager(this);
                settings.setLogin("hanhan");
                settings.setPassword("123456");
                Intent i = new Intent("Connect", null, getApplicationContext(), XmppService.class);
                startService(i);
                break;
            case R.id.btnSend:
                XmppTools.send("I am here", "bobo@lovejog.com", this);
                break;
        }


        // connect();

    }

    public void connect() {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                Context context = getApplicationContext();
                SmackAndroid.init(context);
                ConnectionConfiguration ConnectionConfiguration = new ConnectionConfiguration("115.29.17.154", 5222);
                ConnectionConfiguration.setDebuggerEnabled(true);
                ConnectionConfiguration.setSecurityMode(org.jivesoftware.smack.ConnectionConfiguration.SecurityMode.disabled);
                XMPPConnection connection = new XMPPTCPConnection(ConnectionConfiguration);
                PingManager mPingManager = PingManager.getInstanceFor(connection);
                mPingManager.registerPingFailedListener(new PingFailedListener() {

                    @Override
                    public void pingFailed() {
                        // Note: remember that maybeStartReconnect is called from a different thread (the PingTask) here, it may causes synchronization problems
                        long now = new Date().getTime();

                        Log.d(tag, "Ping失败");
                    }
                });
                try {
                    connection.connect();
                    Log.d(tag, "连接成功");
                } catch (SmackException.ConnectionException e) {
                    List<HostAddress> lists = e.getFailedAddresses();
                    for (HostAddress h : lists) {
                        Log.d(tag, h.getErrorMessage());
                    }

                    e.printStackTrace();
                } catch (SmackException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XMPPException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }
}
