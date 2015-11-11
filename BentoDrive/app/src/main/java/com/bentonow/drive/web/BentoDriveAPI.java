package com.bentonow.drive.web;

import com.bentonow.drive.Application;
import com.bentonow.drive.R;
import com.bentonow.drive.util.SharedPreferencesUtil;

/**
 * Created by Jose Torres on 11/10/15.
 */
public class BentoDriveAPI {

    public final static String NODE_URL = Application.getInstance().getString(R.string.node_url);
    public final static String URL_AUTHENTICATION = "/api/authenticate?username=%s&password=%s&type=driver";
    public final static String URL_SEND_LOCATION = "/api/uloc?token=%s&lat=%s&lng=%s";

    public static String getAuthenticationUrl(String sUsername, String sPassword) {
        return String.format(URL_AUTHENTICATION, sUsername, sPassword);
    }

    public static String getSendLocationUrl(double dLatitude, double dLongitude) {
        return String.format(URL_SEND_LOCATION, SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.TOKEN), dLatitude, dLongitude);
    }

}
