package com.witleaf.step;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by witwave on 2015/1/7.
 */
public class SettingsManager {

    public static final String APP_NAME = "Step";
    private final String tag = "SettingsManager";

    private static SettingsManager sSettingsManager;
    // XMPP connection
    public String serverHost;
    public String serviceName;
    public boolean debug = true;
    public int serverPort;
    public int pingIntervalInSec;
    private final SharedPreferences mSharedPreferences;
    private final Context mContext;

    private String _login;


    public SettingsManager(Context context) {
        mContext = context;
        mSharedPreferences = mContext.getSharedPreferences("Step", 1);
       // serverHost = getString("serverHost", "115.29.17.154");//set the host
        serverHost = getString("serverHost", "192.168.80.88");
        serverPort = getInt("serverPort", 5222);
        serviceName = getString("ServiceName", "lovejog.com");
        pingIntervalInSec = getInt("pingIntervalInSec", 600);
    }

    private String getString(String key, String defaultValue) {
        try {
            if (mSharedPreferences.contains(key)) {
                return mSharedPreferences.getString(key, defaultValue);
            }
        } catch (ClassCastException e) {
            Log.e(tag, "Failed to retrieve setting " + key, e);
        }
        saveSetting(key, defaultValue);
        return defaultValue;
    }

    private int getInt(String key, int defaultValue) {
        try {
            if (mSharedPreferences.contains(key)) {
                return mSharedPreferences.getInt(key, defaultValue);
            }
        } catch (ClassCastException e) {
            Log.e(tag, "Failed to retrieve setting " + key, e);
        }
        saveSetting(key, defaultValue);
        return defaultValue;
    }

    public String getLogin() {
        return _login;
    }

    public void setLogin(String value) {
        _login = saveSetting("login", value);
    }


    private String _password;

    public String getPassword() {
        return _password;
    }

    public void setPassword(String value) {
        _password = saveSetting("password", value);
    }

    public static SettingsManager getSettingsManager(Context context) {
        if (sSettingsManager == null) {
            sSettingsManager = new SettingsManager(context);
        }
        return sSettingsManager;
    }


    public Boolean saveSetting(String key, Boolean value) {
        getEditor().putBoolean(key, value).commit();
        OnPreferencesUpdated(key);
        return value;
    }

    public String saveSetting(String key, String value) {
        getEditor().putString(key, value).commit();
        OnPreferencesUpdated(key);
        return value;
    }

    public Integer saveSetting(String key, Integer value) {
        getEditor().putInt(key, value).commit();
        OnPreferencesUpdated(key);
        return value;
    }

    private void OnPreferencesUpdated(String key) {
    }

    private SharedPreferences.Editor getEditor() {
        return mSharedPreferences.edit();
    }
}
