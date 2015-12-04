package com.bentonow.drive.util;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;

import com.bentonow.drive.controller.ListOrderAssignedActivity;
import com.bentonow.drive.controller.LogInActivity;
import com.bentonow.drive.controller.OrderAssignedActivity;

/**
 * Created by Jose Torres on 11/10/15.
 */
public class BentoDriveUtil {

    public static final boolean bIsKokushoTesting = false;

    public static void openListBentoActivity(FragmentActivity mContext) {
        Intent intent = new Intent(mContext, ListOrderAssignedActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    public static void openBentoOrderActivity(FragmentActivity mContext) {
        Intent intent = new Intent(mContext, OrderAssignedActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    public static void openLogInActivity(Context mContext) {
        Intent intent = new Intent(mContext, LogInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    public static boolean isUserConnected(Context mContext) {
        return SharedPreferencesUtil.getBooleanPreference(mContext, SharedPreferencesUtil.IS_USER_LOG_IN);
    }

    public static void disconnectUser(Context ctx, boolean bSaveSettings) {
        SharedPreferencesUtil.setAppPreference(ctx, SharedPreferencesUtil.IS_USER_LOG_IN, false);
        SharedPreferencesUtil.setAppPreference(ctx, SharedPreferencesUtil.USE_SAVED_SETTINGS, bSaveSettings);
        openLogInActivity(ctx);
    }

    public static boolean isInvalidPhoneNumber(String sMessage) {
        return sMessage.contains("is not a mobile number") || sMessage.contains("is not a valid phone number");
    }
}
