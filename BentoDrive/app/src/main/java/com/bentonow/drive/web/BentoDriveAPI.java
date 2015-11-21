package com.bentonow.drive.web;

import com.bentonow.drive.Application;
import com.bentonow.drive.R;
import com.bentonow.drive.util.ConstantUtil;
import com.bentonow.drive.util.DebugUtils;
import com.bentonow.drive.util.SharedPreferencesUtil;

/**
 * Created by Jose Torres on 11/10/15.
 */
public class BentoDriveAPI {

    public final static String NODE_URL = Application.getInstance().getString(R.string.node_url);
    public final static String HOUSTON_URL = Application.getInstance().getString(R.string.houston_url);
    public final static String URL_AUTHENTICATION = "/api/authenticate?username=%s&password=%s&type=driver";
    public final static String URL_SEND_LOCATION = "/api/uloc?token=%s&lat=%s&lng=%s";
    public final static String URL_GET_ORDERS = "/api/order/getAllAssigned?token=";
    public final static String URL_GET_STATUS_ACCEPT = "/api/order/accept?token=%s&orderId=%s";
    public final static String URL_GET_STATUS_REJECT = "/api/order/reject?token=%s&orderId=%s";


    public static String getAuthenticationUrl(String sUsername, String sPassword) {
        String sUrl = String.format(URL_AUTHENTICATION, sUsername, sPassword);
        DebugUtils.logDebug("URL: " + sUrl);
        return sUrl;
    }

    public static String getSendLocationUrl(double dLatitude, double dLongitude) {
        String sUrl = String.format(URL_SEND_LOCATION, SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.TOKEN), dLatitude, dLongitude);
        DebugUtils.logDebug("URL: " + sUrl);
        return sUrl;
    }

    public static String getAssignedOrdersUrl() {
        String sUrl = HOUSTON_URL + URL_GET_ORDERS + SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.TOKEN);
        DebugUtils.logDebug("URL: " + sUrl);
        return sUrl;
    }

    public static String getStatusOrderUrl(ConstantUtil.optStatusOrder optStatus, String sOrderId) {
        switch (optStatus) {
            case ACCEPT:
                return HOUSTON_URL + String.format(URL_GET_STATUS_ACCEPT, SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.TOKEN), sOrderId);
            case REJECT:
                return HOUSTON_URL + String.format(URL_GET_STATUS_REJECT, SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.TOKEN), sOrderId);
            default:
                return "";
        }
    }

}
