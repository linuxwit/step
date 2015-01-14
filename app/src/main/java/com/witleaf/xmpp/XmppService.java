package com.witleaf.xmpp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.witleaf.step.SettingsManager;

/**
 * 本服务提供UI与XmppManager之间的桥梁
 * 处理二种信息，一种去连接服务器，一种是发送信息
 */
public class XmppService extends Service {
    public static final String SERVICE_THREAD_NAME = SettingsManager.APP_NAME + "Service";
    private static final String tag = "XmppService";
    public static final String ACTION_SEND = "com.witleaf.step.xmpp.ACTION_SEND";
    public static final String ACTION_XMPP_MESSAGE_RECEIVED = "com.wison.closer.action.XMPP.MESSAGE_RECEIVED";
    public static final String ACTION_XMPP_CONNECTION_CHAGE = "com.wison.closer.action.XMPP.CONNECTION_CHAGE";
    public static final String ACTION_REGISTER = "com.wison.closer.action.XMPP.REGISTER";
    public static final String ACTION_LOGIN = "com.wison.closer.action.XMPP.LOGIN";
    ;

    private static XmppService sIntance = null;
    private static XmppManager sXmppMgr = null;
    private final IBinder mBinder = new LocalBinder();
    private static volatile ServiceHandler sServiceHandler = null;
    private static volatile Looper sServiceLooper;


    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (sXmppMgr == null) {
                setupXmppManager();
            }
            Intent intent = (Intent) msg.obj;
            String action = intent.getAction();
            Log.i(tag, "onStartCommand(): Intent " + action);
            switch (action) {
                case ACTION_SEND:
                    Log.d(tag, "send");
                    XmppMsg xmppMsg = intent.getParcelableExtra("xmppMsg");
                    if (xmppMsg == null) {
                        xmppMsg = new XmppMsg(intent.getStringExtra("message"));
                    }
                    sXmppMgr.send(xmppMsg, intent.getStringExtra("to"));
                case ACTION_REGISTER:
                    Log.d(tag, "注册");
                    sXmppMgr.register();
                    break;
                case ACTION_LOGIN:
                    Log.d(tag, "登录");
                    sXmppMgr.requestConnection();
            }
        }
    }

    public XmppService() {
    }


    private void setupXmppManager() {
        sXmppMgr = XmppManager.getInstance(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sIntance = this;
        HandlerThread thread = new HandlerThread(SERVICE_THREAD_NAME);
        thread.start();
        sServiceLooper = thread.getLooper();
        sServiceHandler = new ServiceHandler(sServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(tag, "服务进入:onStartCommand");
        Message msg = sServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        msg.sendToTarget();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public XmppService getService() {
            return XmppService.this;
        }
    }


    public void send(String msg, String to) {
        send(new XmppMsg(msg), to);
    }


    public void send(XmppMsg msg, String to) {
        if (sXmppMgr != null) {
            sXmppMgr.send(msg, to);
        } else {
            Log.w(tag, "MainService send XmppMsg: _xmppMgr == null");
        }
    }

    public static boolean sendToServiceHandler(Intent intent) {
        if (sServiceHandler != null) {
            Message message = sServiceHandler.obtainMessage();
            message.obj = intent;
            return sServiceHandler.sendMessage(message);
        } else {
            Log.v(tag, "handler为空");
            return false;
        }
    }

}
