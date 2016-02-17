package com.bentonow.drive.controller;

import android.support.v4.app.FragmentActivity;

import com.bentonow.drive.util.MixpanelUtils;
import com.bentonow.drive.util.SharedPreferencesUtil;

/**
 * Created by Jose Torres on 11/10/15.
 */
public class MainActivity extends FragmentActivity {

    @Override
    protected void onResume() {
        SharedPreferencesUtil.setAppPreference(MainActivity.this, SharedPreferencesUtil.IS_APP_IN_FRONT, true);
        super.onResume();
    }

    @Override
    protected void onPause() {
        SharedPreferencesUtil.setAppPreference(MainActivity.this, SharedPreferencesUtil.IS_APP_IN_FRONT, false);
        super.onPause();
    }

    @Override
    protected void onStart() {
        SharedPreferencesUtil.setAppPreference(MainActivity.this, SharedPreferencesUtil.IS_APP_ALIVE, true);
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        MixpanelUtils.getMixpanelApi(MainActivity.this).flush();
        SharedPreferencesUtil.setAppPreference(MainActivity.this, SharedPreferencesUtil.IS_APP_ALIVE, true);
        super.onDestroy();
    }
}
