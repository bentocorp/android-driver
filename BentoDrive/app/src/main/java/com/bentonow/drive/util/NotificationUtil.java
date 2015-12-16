
package com.bentonow.drive.util;
/**
 * Created by Jose Torres on 07/12/15.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.bentonow.drive.R;
import com.bentonow.drive.controller.ListOrderAssignedActivity;

import java.util.Calendar;

public class NotificationUtil {

    public static final int idNotificationTask = 20141212;

    public static void showBentoDriveNotification(Context mContext, int idNotificationBody, Uri uriLocation) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(idNotificationTask);

        NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(mContext);

        mNotifyBuilder.setSmallIcon(R.mipmap.bento_launch);
        mNotifyBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        mNotifyBuilder.setContentTitle("Bento Drive");
        mNotifyBuilder.setContentText(mContext.getString(idNotificationBody));
        mNotifyBuilder.setSound(uriLocation);
        mNotifyBuilder.setAutoCancel(true).setVibrate(new long[]{700, 700});

        Intent cIntent = new Intent(mContext, ListOrderAssignedActivity.class);
        cIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, cIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mNotifyBuilder.setContentIntent(contentIntent);

        mNotificationManager.notify(idNotificationTask, mNotifyBuilder.build());


    }

    public static void cancelAllNotification(Context context) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
    }

}
