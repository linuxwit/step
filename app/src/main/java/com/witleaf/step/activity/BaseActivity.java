package com.witleaf.step.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.witleaf.step.R;


/**
 * Base activity class that handles the interaction of the Activity with the service bus. Also includes
 * an application wide implementation of the progress dialog.
 *
 *
 * @author Trey Robinson
 *
 */
public class BaseActivity extends FragmentActivity {

    protected StatusView mStatusView;
    protected View mMainView;

    @Override
    protected void onResume() {
        super.onResume();

        //required view components for the loading screen
        mMainView = findViewById(R.id.main_view);
        mStatusView = (StatusView) findViewById(R.id.statusView);
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    protected void showProgress(final boolean show, String message) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mStatusView.setVisibility(View.VISIBLE);
            mStatusView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            mMainView.setVisibility(View.VISIBLE);
            mMainView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mMainView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        } else {
            mStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mMainView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}
