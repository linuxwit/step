package com.witleaf.xmpp;

import android.content.Context;
import android.util.Log;

import com.witleaf.step.SettingsManager;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

/**
 * XMPP管理
 * XMPP处理流程：
 * 1. 启动服务=》调用OnStartCommand方法
 * 　a. 如果是action是广播消息，则广播给前台UI
 * b. 如果是连接信息，就调用hander去处理消息，Handler收到消息后，根据action进行处理，如果是连接信息，则进行重新
 * <p/>
 * 所有的消息都是从服务的Handler中发出
 * <p/>
 * Created by witwave on 2015/1/7.
 */
public class XmppManager {
    private final String tag = "XmppManager";
    private static XmppManager mXmppManager = null;
    private static SettingsManager mSettings = null;
    private final SmackAndroid mSmackAndroid;
    private XMPPConnection mConnection = null;
    private Context mContext;


    public static XmppManager getInstance(Context ctx) {
        if (mXmppManager == null) {
            mXmppManager = new XmppManager(ctx);
        }
        return mXmppManager;
    }

    private XmppManager(Context context) {
        mContext = context;
        mSmackAndroid = SmackAndroid.init(mContext);
        mSettings = SettingsManager.getSettingsManager(mContext);
        mConnection = null;
    }


    public void requestConnection() {


        XMPPConnection connection = createConnection(mSettings);
        if (!connectAndAuth(connection)) {
            return;
        }
        onConnectionEstablished(connection);


    }

    private void onConnectionEstablished(XMPPConnection connection) {
        mConnection = connection;

        mConnection.addConnectionListener(new ConnectionListener() {
            @Override
            public void connected(XMPPConnection xmppConnection) {
                Log.d(tag, "连接到服务器");
            }

            @Override
            public void authenticated(XMPPConnection xmppConnection) {
                Log.d(tag, "通过验证");
            }

            @Override
            public void connectionClosed() {
                Log.d(tag, "连接关闭");
            }

            @Override
            public void connectionClosedOnError(Exception e) {

            }

            @Override
            public void reconnectingIn(int i) {

            }

            @Override
            public void reconnectionSuccessful() {

            }

            @Override
            public void reconnectionFailed(Exception e) {

            }
        });
    }


    private XMPPConnection createConnection(SettingsManager settings) {
        Log.d(tag, "服务器信息" + settings.serverHost + ":" + settings.serverPort + ":" + settings.serviceName);
        ConnectionConfiguration conf = new ConnectionConfiguration(settings.serverHost, settings.serverPort, settings.serviceName);
        conf.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        conf.setSendPresence(false);
        conf.setDebuggerEnabled(settings.debug);
        return new XMPPTCPConnection(conf);
    }

    private boolean connectAndAuth(XMPPConnection connection) {
        try {
            Log.d(tag, "是安全连接吗？" + connection.isSecureConnection());
            connection.connect();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        if (connection.isAuthenticated())
            return true;

        try {
            String login = mSettings.getLogin();
            String password = mSettings.getPassword();
            connection.login(login, password, mSettings.APP_NAME);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
