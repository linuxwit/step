package com.witleaf.xmpp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.witleaf.step.SettingsManager;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.util.dns.HostAddress;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jivesoftware.smackx.vcardtemp.provider.VCardProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
            mSettings.setTopLoginFlag(Boolean.FALSE);
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


    public void register() {
        XMPPConnection connection = mConnection;
        if (connection == null) {
            connection = createConnection(mSettings);
            try {
                connection.connect();
            } catch (Exception e) {
                e.printStackTrace();
                broadcastStatus(mContext, 0, 1, "网络连接失败");
                return;
            }
        }

        Registration reg = new Registration();
        reg.setType(IQ.Type.SET);
        reg.setTo(connection.getServiceName());

        String account = mSettings.getLogin();
        String password = mSettings.getPassword();

        Map<String, String> map = new HashMap<String, String>();
        map.put("username", account);
        map.put("password", password);
        reg.setAttributes(map);
        PacketFilter filter = new AndFilter(new PacketIDFilter(reg.getPacketID()), new PacketTypeFilter(IQ.class));
        PacketCollector collector = connection.createPacketCollector(filter);
        try {
            connection.sendPacket(reg);
            IQ result = (IQ) collector.nextResult(SmackConfiguration.getDefaultPacketReplyTimeout());
            System.out.println("-----------------result--------------------" + result);

            collector.cancel();// 停止请求results（是否成功的结果）
            String msg;
            if (result == null) {
                Log.e("RegistActivity", "No response from server.");
                msg = "服务器没有结果";
            } else if (result.getType() == IQ.Type.RESULT) {
                msg = "注册成功";
            } else {
                if (result.getError().toString().equalsIgnoreCase("conflict(409)")) {
                    Log.e("RegistActivity", "IQ.Type.ERROR: " + result.getError().toString());
                    msg = "账号已经存在";
                } else {
                    Log.e("RegistActivity", "IQ.Type.ERROR: " + result.getError().toString());
                    msg = "注册失败";
                }
            }
            broadcastStatus(mContext, 0, 1, "注册结果：" + msg);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
            broadcastStatus(mContext, 0, 1, "暂时无法连接服务器");
        }
    }

    /**
     * 添加一个好友
     *
     * @param userid
     */
    public void addFriend(String userid) {
        if (mConnection != null && mConnection.isAuthenticated()) {
            try {
                Roster roster = mConnection.getRoster();
                roster.createEntry(userid + mConnection.getServiceName(), null, new String[]{"friends"});
                broadcastStatus(mContext, 0, 1, "添加好友");
            } catch (SmackException.NotLoggedInException e) {
                e.printStackTrace();
                broadcastStatus(mContext, 0, 1, "添加好友" + e.getMessage());
            } catch (SmackException.NoResponseException e) {
                broadcastStatus(mContext, 0, 1, "添加好友" + e.getMessage());
                e.printStackTrace();
            } catch (XMPPException.XMPPErrorException e) {
                broadcastStatus(mContext, 0, 1, "添加好友" + e.getMessage());
                e.printStackTrace();
            } catch (SmackException.NotConnectedException e) {
                broadcastStatus(mContext, 0, 1, "添加好友" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取所有好友
     *
     * @return
     */
    public List<RosterEntry> getAllFriends() {
        List<RosterEntry> entries = new ArrayList<RosterEntry>();
        Collection<RosterEntry> roscol = mConnection.getRoster().getEntries();
        Iterator<RosterEntry> iter = roscol.iterator();
        while (iter.hasNext()) {
            entries.add(iter.next());
        }
        return entries;
    }

    /**
     * VCard
     */
    public VCard getVCard(String user) {
        ProviderManager.addIQProvider("vCard", "vcard-temp", new VCardProvider());
        VCard card = new VCard();
        try {
            card.load(mConnection, user);
            Log.d("*****", card.getFirstName() + card.getNickName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return card;
    }


}
