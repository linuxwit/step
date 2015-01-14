package com.witleaf.xmpp;

import android.content.Context;
import android.content.Intent;


/**
 * Created by witwave on 2014/12/26.
 */
public class XmppTools {

    public static final String APP_NAME = "closer";

    public static boolean send(String msg, String to, Context ctx) {
        return send(new XmppMsg(msg), to, ctx);
    }


    public static boolean send(XmppMsg msg, String to, Context ctx) {
        Intent intent = new Intent(XmppService.ACTION_SEND);
        intent.setClass(ctx, XmppService.class);
        if (to != null) {
            intent.putExtra("to", to);
        }
        intent.putExtra("xmppMsg", msg);
        return XmppService.sendToServiceHandler(intent);
    }

    public static void startSvcXMPPMsg(final Context ctx, final String message, final String from) {
        final Intent i = new Intent(XmppService.ACTION_XMPP_MESSAGE_RECEIVED, null, ctx, XmppService.class);
        i.putExtra("message", message);
        i.putExtra("from", from);
        XmppService.sendToServiceHandler(i);
    }


    public static void startSvcIntent(final Context ctx, final String action) {
        final Intent i = newSvcIntent(ctx, action, null, null);
        ctx.startService(i);
    }

    public static Intent newSvcIntent(final Context ctx, final String action, final String message, final String to) {
        final Intent i = new Intent(action, null, ctx, XmppService.class);
        if (message != null) {
            i.putExtra("message", message);
        }
        if (to != null) {
            i.putExtra("to", to);
        }
        return i;
    }


    public static boolean register(final Context ctx) {
        Intent intent = new Intent(XmppService.ACTION_REGISTER);
        intent.setClass(ctx, XmppService.class);
        return XmppService.sendToServiceHandler(intent);
    }
}
