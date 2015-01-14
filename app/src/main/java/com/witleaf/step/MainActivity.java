package com.witleaf.step;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.witleaf.step.adapters.CardPagerAdapter;
import com.witleaf.step.adapters.TabAdapter;
import com.witleaf.step.fragments.AddFriendFragment;
import com.witleaf.step.fragments.BuddiesFragment;
import com.witleaf.step.fragments.LoginFragment;
import com.witleaf.step.models.UserCard;
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

    private ArrayList<UserCard> mCardInfoList;

    private final BroadcastReceiver mXmppReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // Toast.makeText(getApplicationContext(), action, Toast.LENGTH_SHORT).show();
            Toast.makeText(getApplicationContext(), intent.getStringExtra("current_action"), Toast.LENGTH_SHORT).show();

            Log.d(tag, "收到广播" + action + ":" + intent.getStringExtra("current_action"));

        /*    if (mFragments.size() > 1) {
                mFragments.remove(1);
                mPager.setAdapter(new TabAdapter(getSupportFragmentManager(), mFragments));
                mPager.getAdapter().notifyDataSetChanged();
            }*/
            onLogin();
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
    private PagerSlidingTabStrip mTabs;
    private Toolbar mToolbar;
    private SystemBarTintManager mTintManager;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    SettingsManager settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(tag, "onCreate");
        settings = SettingsManager.getSettingsManager(this);
        mPager = (ViewPager) findViewById(R.id.pager);
        mTabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (settings.getTopLoginFlag()) {
            Log.d(tag, "打开登录界面");
            Intent login = new Intent(this, LoginActivity.class);
            startActivity(login);
            Log.d(tag, "完成登录界面");
        } else {
            onLogin();
        }
    }


    public void onLogin() {
        setToolbar();

        mTintManager = new SystemBarTintManager(this);
        mTintManager.setStatusBarTintEnabled(true);
        changeColor(getResources().getColor(R.color.green));
        mCardInfoList = new ArrayList<UserCard>();
        mCardInfoList.add(new UserCard("1", "老公"));
        mCardInfoList.add(new UserCard("2", "老妈"));
        mCardInfoList.add(new UserCard("3", "女儿"));
        mPager.setAdapter(new CardPagerAdapter(getSupportFragmentManager(), mCardInfoList));
        mTabs.setViewPager(mPager);
    }

    public void onNotLogin() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_view, LoginFragment.newInstance());
        fragmentTransaction.commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        Log.d(tag, "onStart");
        super.onStart();
        Intent intent = new Intent(this, XmppService.class);
        bindService(intent, mXmppServiceConn, Service.BIND_AUTO_CREATE);

        IntentFilter intentFilter = new IntentFilter(XmppService.ACTION_XMPP_CONNECTION_CHAGE);
        registerReceiver(mXmppReceiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(tag, "onResume");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(tag, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        Log.d(tag, "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(tag, "onDestroy");
        super.onDestroy();
        unregisterReceiver(mXmppReceiver);
        unbindService(mXmppServiceConn);
    }

    private void setToolbar() {
        mToolbar.setTitle(R.string.app_name);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        // 實作 drawer toggle 並放入 toolbar
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_add_person:
                        AddFriendFragment addFriendFragment = new AddFriendFragment();
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        addFriendFragment.show(ft, "ft");
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    private void changeColor(int newColor) {
        mTabs.setBackgroundColor(newColor);
        mTintManager.setTintColor(newColor);
        Drawable colorDrawable = new ColorDrawable(newColor);
        Drawable bottomDrawable = new ColorDrawable(getResources().getColor(android.R.color.transparent));
        LayerDrawable ld = new LayerDrawable(new Drawable[]{colorDrawable, bottomDrawable});
        getSupportActionBar().setBackgroundDrawable(ld);
    }

    public void onClick(View view) {
        /*
        switch (view.getId()) {
            case R.id.btnLogin:
                SettingsManager settings = SettingsManager.getSettingsManager(this);
                settings.saveSetting("serverHost", "192.168.80.88");
                settings.setLogin("hanhan");
                settings.setPassword("123456");

                Log.d(tag, "点击Login" + settings.serverHost);
                Intent i = new Intent("Connect", null, getApplicationContext(), XmppService.class);
                startService(i);
                break;
            case R.id.btnSend:
                XmppTools.send("I am here", "bobo@lovejog.com", this);
                break;
        }*/
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
