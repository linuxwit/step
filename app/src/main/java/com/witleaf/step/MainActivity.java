package com.witleaf.step;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;

import com.witleaf.xmpp.XmppService;

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
import java.util.Date;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    private String tag = "MainActivity";

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
    }


    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, XmppService.class);
        bindService(intent, mXmppServiceConn, Service.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mXmppServiceConn);
    }


    public void onClick(View view) {
        SettingsManager settings = SettingsManager.getSettingsManager(this);
        settings.setLogin("hanhan");
        settings.setPassword("123456");
        Intent i = new Intent("Connect", null, getApplicationContext(), XmppService.class);
        startService(i);

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
                    List<HostAddress> lists= e.getFailedAddresses();
                    for(HostAddress h:lists){
                        Log.d(tag,h.getErrorMessage());
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
