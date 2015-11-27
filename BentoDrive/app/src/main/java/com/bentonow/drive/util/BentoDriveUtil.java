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

    public static final boolean bIsKokushoTesting = true;

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
        boolean bIsUserConnected = !SharedPreferencesUtil.getStringPreference(mContext, SharedPreferencesUtil.TOKEN).isEmpty() &&
                !SharedPreferencesUtil.getStringPreference(mContext, SharedPreferencesUtil.USER_NAME).isEmpty();
        return bIsUserConnected;
    }

    public static void disconnectUser(Context ctx) {
        SharedPreferencesUtil.clearAllPreferences(ctx);
        openLogInActivity(ctx);
    }

    public static boolean isInvalidPhoneNumber(String sMessage) {
        return sMessage.contains("is not a mobile number") || sMessage.contains("is not a valid phone number");
    }
}
