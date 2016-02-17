package com.bentonow.drive.util;

import android.content.Context;

import com.bentonow.drive.R;
import com.bentonow.drive.model.CallStatusModel;
import com.bentonow.drive.model.MixpanelNodeModel;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONObject;

public class MixpanelUtils {
    public static final String TAG = "MixpanelUtils";

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


    public static void trackNodeIntermittent(Context mContext, MixpanelNodeModel mNode) {
        try {
            JSONObject mJsonNode = new JSONObject();
            mJsonNode.put("Listener Enable", mNode.isbIsListenerEnable());
            mJsonNode.put("Reconnecting", mNode.isbIsReconnecting());
            mJsonNode.put("Retrying", mNode.isbIsRetrying());
            mJsonNode.put("Service Null", mNode.isbIsWebServiceNull());
            mJsonNode.put("Transport Closed", mNode.isbIsTransportClosed());
            mJsonNode.put("Transport Error", mNode.isbIsTransportError());
            mJsonNode.put("Transport Closed Info", mNode.getsTransportClosed());
            mJsonNode.put("Transport Error Info", mNode.getsTransportError());
            mJsonNode.put("Seconds", mNode.getSeconds());
            try {
                CallStatusModel mCallStatus = TelephonyUtils.getInformation(mContext);
                mJsonNode.put("Cell", mCallStatus.getCell());
                mJsonNode.put("Cell Location", mCallStatus.getCellLocation());
                mJsonNode.put("Gsm Location", mCallStatus.getGsmLocation());
                mJsonNode.put("Mcc", mCallStatus.getMcc());
                mJsonNode.put("Mcn", mCallStatus.getMcn());
                mJsonNode.put("Model", mCallStatus.getModel());
                mJsonNode.put("Operator", mCallStatus.getOperator());
                mJsonNode.put("Signal Strength", mCallStatus.getStrenght());
                mJsonNode.put("Connection Type", mCallStatus.getTipoConexion());
            } catch (Exception ex) {
                DebugUtils.logError(TAG, ex);
            }
            getMixpanelApi(mContext).getPeople().identify(SharedPreferencesUtil.getStringPreference(mContext, SharedPreferencesUtil.USER_NAME));
            getMixpanelApi(mContext).track("Node Intermittent", mJsonNode);
        } catch (Exception ex) {
            DebugUtils.logError("trackRevenue()", ex);
        }
    }

    public static void clearPreferences(Context mContext) {
        getMixpanelApi(mContext).reset();
        getMixpanelApi(mContext).clearSuperProperties();
    }
}
