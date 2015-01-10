package com.witleaf.xmpp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.witleaf.step.SettingsManager;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.util.dns.HostAddress;

import java.util.List;

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

    public final static int XMPP_UNCONNECTED = 0;
    public final static int XMPP_CONNECTED = 1;
    public final static int XMPP_UNAUTHORED = 2;
    public final static int XMPP_AUTHORED = 3;

    private static XmppManager mXmppManager = null;
    private static SettingsManager mSettings = null;
    private final SmackAndroid mSmackAndroid;
    private XMPPConnection mConnection = null;
    private Context mContext;

    private int mXmppStatus = 0;


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
        if (mConnection == null || !mConnection.isAuthenticated()) {
            XMPPConnection connection = createConnection(mSettings);
            if (!connectAndAuth(connection)) {
                mXmppStatus = XMPP_CONNECTED;
                return;
            }
            mXmppStatus = XMPP_AUTHORED;
            Log.d(tag, "成功登录到服务器");
            broadcastStatus(mContext, 0, 1, "登录成功");
            onConnectionEstablished(connection);
        }
    }

    private void onConnectionEstablished(XMPPConnection connection) {
        mConnection = connection;

        mConnection.addConnectionListener(new ConnectionListener() {
            @Override
            public void connected(XMPPConnection xmppConnection) {
                Log.d(tag, "成功登录到服务器");
                broadcastStatus(mContext, 0, 1, "登录成功");
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

        try {
            PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
            connection.addPacketListener(new PacketListener() {
                @Override
                public void processPacket(Packet packet) throws SmackException.NotConnectedException {
                    Message message = (Message) packet;
                    Log.d(tag, packet.getFrom() + "：" + message.getBody());
                }
            }, filter);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static void broadcastStatus(Context ctx, int oldState, int newState, String currentAction) {
        Intent intent = new Intent(XmppService.ACTION_XMPP_CONNECTION_CHAGE);
        intent.putExtra("old_state", oldState);
        intent.putExtra("new_state", newState);
        intent.putExtra("current_action", currentAction);
        ctx.sendBroadcast(intent);
    }


    private XMPPConnection createConnection(SettingsManager settings) {
        Log.d(tag, "服务器信息" + settings.serverHost + ":" + settings.serverPort + ":" + settings.serviceName);
        ConnectionConfiguration conf = new ConnectionConfiguration(settings.serverHost, settings.serverPort, settings.serviceName);
        conf.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        conf.setSendPresence(true);
        conf.setDebuggerEnabled(settings.debug);
        return new XMPPTCPConnection(conf);
    }

    private boolean connectAndAuth(XMPPConnection connection) {
        try {
            connection.connect();
        } catch (SmackException.ConnectionException e) {
            List<HostAddress> lists = e.getFailedAddresses();
            for (HostAddress h : lists) {
                Log.d(tag, h.getErrorMessage());
            }
            broadcastStatus(mContext, 0, 1, "请检查您的网络链接后再重试");
            return false;
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
            broadcastStatus(mContext, 0, 1, "请检查用户名和密码后再重试");
            return false;
        }
        return true;
    }

    public void send(XmppMsg xmppMsg, String to) {
        Message msg = new Message();
        msg.setTo(to);
        msg.setType(Message.Type.chat);
        msg.setBody(xmppMsg.toString());
        if (mConnection != null && mConnection.isConnected()) {
            try {
                mConnection.sendPacket(msg);
            } catch (Exception ex) {
                Log.e(tag, "发送失败", ex);
            }
        } else {
            Log.w(tag, "当前连接不可用");
        }
    }
}
