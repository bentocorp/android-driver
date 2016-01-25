package com.bentonow.drive.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;

import com.bentonow.drive.Application;
import com.bentonow.drive.R;
import com.bentonow.drive.controller.ListOrderAssignedActivity;
import com.bentonow.drive.controller.LogInActivity;
import com.bentonow.drive.controller.OrderAssignedActivity;
import com.bentonow.drive.model.Address;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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

    public static void showInAppNotification(Context ctx, ConstantUtil.optTaskChanged optTaskChanged) {
        Uri uriSound;
        switch (optTaskChanged) {
            case ASSIGN:
                uriSound = Uri.parse("android.resource://" + Application.getInstance().getPackageName() + "/raw/new_task");
                if (SharedPreferencesUtil.getBooleanPreference(ctx, SharedPreferencesUtil.IS_APP_IN_FRONT)) {
                    WidgetsUtils.createShortToast(R.string.notification_assigned_task);
                    SoundUtil.playNotificationSound(uriSound);
                } else {
                    NotificationUtil.showBentoDriveNotification(ctx, R.string.notification_assigned_task, uriSound);
                }
                break;
            case SWITCHED:
                uriSound = Uri.parse("android.resource://" + Application.getInstance().getPackageName() + "/raw/task_switched");
                if (SharedPreferencesUtil.getBooleanPreference(ctx, SharedPreferencesUtil.IS_APP_IN_FRONT)) {
                    WidgetsUtils.createShortToast(R.string.notification_change_task);
                    SoundUtil.playNotificationSound(uriSound);
                } else {
                    NotificationUtil.showBentoDriveNotification(ctx, R.string.notification_change_task, uriSound);
                }
                break;
            case REMOVED:
                uriSound = Uri.parse("android.resource://" + Application.getInstance().getPackageName() + "/raw/task_removed");
                if (SharedPreferencesUtil.getBooleanPreference(ctx, SharedPreferencesUtil.IS_APP_IN_FRONT)) {
                    WidgetsUtils.createShortToast(R.string.notification_un_assigned_task);
                    SoundUtil.playNotificationSound(uriSound);
                } else {
                    NotificationUtil.showBentoDriveNotification(ctx, R.string.notification_un_assigned_task, uriSound);
                }
                break;
        }
    }

    public static String getFormatAddress(Address mAddress) {
        String sFormat = "";
        try {
            sFormat += mAddress.getResidence() + " ";
            sFormat += mAddress.getStreet() + " ";
            sFormat += mAddress.getCity() + ", ";
            sFormat += mAddress.getRegion();
            sFormat = sFormat.replace("null", "");
        } catch (Exception ex) {
            DebugUtils.logError("FormatAddress", ex);
        }
        return sFormat;
    }

    public static boolean bISValidVersion(String sMInVersion) {
        boolean bIsValid = true;
        int iMinVersion;

        try {
            iMinVersion = Integer.parseInt(sMInVersion);
            if (iMinVersion > AndroidUtil.getCodeName(Application.getInstance()))
                bIsValid = false;
        } catch (Exception ex) {
            DebugUtils.logError("bISValidVersion", ex);
            bIsValid = false;
        }

        return bIsValid;
    }

    public static Calendar getCalendarFromPong(String sPong) {
        Calendar mCalPong = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH);
        try {
            mCalPong.setTime(sdf.parse(sPong));
        } catch (Exception ex) {
            DebugUtils.logError("Util", ex.getLocalizedMessage());
        }
        return mCalPong;
    }
}
