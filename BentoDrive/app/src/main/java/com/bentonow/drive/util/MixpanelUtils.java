package com.bentonow.drive.util;

import android.content.Context;

import com.bentonow.drive.R;
import com.bentonow.drive.model.CallStatusModel;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONObject;

public class MixpanelUtils {

    public static MixpanelAPI mMixpanel;

    public static MixpanelAPI getMixpanelApi(Context mContext) {
        if (mMixpanel == null)
            mMixpanel = MixpanelAPI.getInstance(mContext, mContext.getString(R.string.mixpanel_key));

        return mMixpanel;
    }
/*

    public static void track(String event) {
        getMixpanelApi().track(event);
    }

    public static void track(String event, JSONObject params) {
        getMixpanelApi().track(event, params);
    }
*/

    public static void logInUser(Context mContext) {
        MixpanelAPI.People people = getMixpanelApi(mContext).getPeople();
        people.identify(SharedPreferencesUtil.getStringPreference(mContext, SharedPreferencesUtil.USER_NAME));
    }


    public static void trackConnectNode(Context mContext, CallStatusModel mCallStatus) {
        JSONObject properties = new JSONObject();
        try {
            properties.put("$operator", mCallStatus.getOperator());
            getMixpanelApi(mContext).getPeople().identify(SharedPreferencesUtil.getStringPreference(mContext, SharedPreferencesUtil.USER_NAME));
        } catch (Exception ex) {
            DebugUtils.logError("trackRevenue()", ex);
        }
    }

    public static void clearPreferences(Context mContext) {
        getMixpanelApi(mContext).reset();
        getMixpanelApi(mContext).clearSuperProperties();
    }
}
