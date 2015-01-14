package com.witleaf.step.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.witleaf.step.LoginActivity;
import com.witleaf.step.R;
import com.witleaf.step.SettingsManager;
import com.witleaf.xmpp.XmppService;


public class LoginFragment extends Fragment {
    private static final String tag = "LoginFragment";
    public static final String EXTRA_USERNAME = "com.witleaf.step.activity.extra.USERNAME";
    public static final String EXTRA_PASSWORD = "com.witleaf.step.activity.extra.PASSWORD";

    private EditText mUserNameEditText;
    private EditText mPasswordEditText;
    private Context mContext;

    private LoginActivity mLoginActivity;

    public static Fragment newInstance() {
        return new LoginFragment();
    }

    public LoginFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mLoginActivity = (LoginActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        mContext = view.getContext();

        mUserNameEditText = (EditText) view.findViewById(R.id.username);
        mPasswordEditText = (EditText) view.findViewById(R.id.password);

        mPasswordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLoginOrRigister(true);
                    return true;
                }
                return false;
            }
        });

        view.findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLoginOrRigister(true);
            }
        });

        view.findViewById(R.id.register_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLoginOrRigister(false);
            }
        });
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mUserNameEditText.setText(savedInstanceState.getString(EXTRA_USERNAME));
            mPasswordEditText.setText(savedInstanceState.getString(EXTRA_PASSWORD));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_USERNAME, mUserNameEditText.getText().toString());
        outState.putString(EXTRA_PASSWORD, mPasswordEditText.getText().toString());
    }

    public void attemptLoginOrRigister(Boolean isLogin) {

        clearErrors();

        // Store values at the time of the login attempt.
        String username = mUserNameEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(password)) {
            mPasswordEditText.setError(getString(R.string.error_field_required));
            focusView = mPasswordEditText;
            cancel = true;
        } else if (password.length() < 4) {
            mPasswordEditText.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordEditText;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUserNameEditText.setError(getString(R.string.error_field_required));
            focusView = mUserNameEditText;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            // perform the user login attempt.
            SettingsManager settings = SettingsManager.getSettingsManager(mContext.getApplicationContext());
            settings.saveSetting("serverHost", "192.168.80.88");
            settings.setLogin(username);
            settings.setPassword(password);

            mLoginActivity.show(true, "登录中..");

            Log.d(tag, "点击Login" + settings.serverHost);
            String action = isLogin ? XmppService.ACTION_LOGIN : XmppService.ACTION_REGISTER;
            Intent i = new Intent(action, null, mContext.getApplicationContext(), XmppService.class);
            mContext.startService(i);
        }
    }

    private void clearErrors() {
        mUserNameEditText.setError(null);
        mPasswordEditText.setError(null);
    }
}
