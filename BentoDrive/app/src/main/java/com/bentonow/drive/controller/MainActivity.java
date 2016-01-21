package com.bentonow.drive.controller;

import android.support.v4.app.FragmentActivity;

import com.bentonow.drive.util.SharedPreferencesUtil;

/**
 * Created by Jose Torres on 11/10/15.
 */
public class MainActivity extends FragmentActivity {

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferencesUtil.setAppPreference(MainActivity.this, SharedPreferencesUtil.IS_APP_IN_FRONT, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferencesUtil.setAppPreference(MainActivity.this, SharedPreferencesUtil.IS_APP_IN_FRONT, false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferencesUtil.setAppPreference(MainActivity.this, SharedPreferencesUtil.IS_APP_ALIVE, true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferencesUtil.setAppPreference(MainActivity.this, SharedPreferencesUtil.IS_APP_ALIVE, true);
    }
}
