package com.bentonow.drive.web;

import android.content.Context;

import com.bentonow.drive.Application;
import com.bentonow.drive.R;
import com.bentonow.drive.util.ConstantUtil;
import com.bentonow.drive.util.DebugUtils;
import com.bentonow.drive.util.SharedPreferencesUtil;

/**
 * Created by Jose Torres on 11/10/15.
 */
public class BentoDriveAPI {

    public final static String URL_AUTHENTICATION = "/api/authenticate?username=%s&password=%s&type=driver";
    public final static String URL_SEND_LOCATION = "/api/uloc?token=%s&lat=%s&lng=%s";
    public final static String URL_GET_ORDERS = "/api/order/getAllAssigned?token=";
    public final static String URL_GET_STATUS_ACCEPT = "/api/order/accept?token=%s&orderId=%s";
    public final static String URL_GET_STATUS_REJECT = "/api/order/reject?token=%s&orderId=%s";
    public final static String URL_GET_STATUS_ARRIVED = "/api/sms/bento-here?token=%s&orderId=%s";
    public final static String URL_GET_STATUS_COMPLETE = "/api/order/complete?token=%s&orderId=%s";

    public static String getNodeUrl(Context mContext) {
        return mContext.getString(R.string.node_url);
    }

    public static String getHoustonUrl(Context mContext) {
        return mContext.getString(R.string.houston_url);
    }

    public static String getAuthenticationUrl(String sUsername, String sPassword) {
        String sUrl = String.format(URL_AUTHENTICATION, sUsername, sPassword);
        DebugUtils.logDebug("URL: " + sUrl);
        return sUrl;
    }

    public static String getSendLocationUrl(double dLatitude, double dLongitude) {
        String sUrl = String.format(URL_SEND_LOCATION, SharedPreferencesUtil.getStringPreference(Application.getInstance(), SharedPreferencesUtil.TOKEN), dLatitude, dLongitude);
        DebugUtils.logDebug("URL: " + sUrl);
        return sUrl;
    }

    public static String getAssignedOrdersUrl() {
        String sUrl = getHoustonUrl(Application.getInstance()) + URL_GET_ORDERS + SharedPreferencesUtil.getStringPreference(Application.getInstance(), SharedPreferencesUtil.TOKEN);
        DebugUtils.logDebug("URL: " + sUrl);
        return sUrl;
    }

    public static String getStatusOrderUrl(ConstantUtil.optStatusOrder optStatus, String sOrderId) {
        String sUrl = "";
        DebugUtils.logDebug("URL: " + sUrl);
        switch (optStatus) {
            case ACCEPT:
                sUrl = getHoustonUrl(Application.getInstance()) + String.format(URL_GET_STATUS_ACCEPT, SharedPreferencesUtil.getStringPreference(Application.getInstance(), SharedPreferencesUtil.TOKEN), sOrderId);
                break;
            case REJECT:
                sUrl = getHoustonUrl(Application.getInstance()) + String.format(URL_GET_STATUS_REJECT, SharedPreferencesUtil.getStringPreference(Application.getInstance(), SharedPreferencesUtil.TOKEN), sOrderId);
                break;
            case ARRIVED:
                sUrl = getHoustonUrl(Application.getInstance()) + String.format(URL_GET_STATUS_ARRIVED, SharedPreferencesUtil.getStringPreference(Application.getInstance(), SharedPreferencesUtil.TOKEN), sOrderId);
                break;
            case COMPLETE:
                sUrl = getHoustonUrl(Application.getInstance()) + String.format(URL_GET_STATUS_COMPLETE, SharedPreferencesUtil.getStringPreference(Application.getInstance(), SharedPreferencesUtil.TOKEN), sOrderId);
                break;
            default:
                sUrl = "";
        }
        return sUrl;
    }


}
