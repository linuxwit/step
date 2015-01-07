package com.witleaf.xmpp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by witwave on 2015/1/7.
 */
public class XmppMsg implements Parcelable {


    private final StringBuilder mMessage = new StringBuilder();

    public XmppMsg(String msg) {
        mMessage.append(msg);
    }

    @Override
    public String toString() {
        return mMessage.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(toString());
    }
}
