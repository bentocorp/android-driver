package com.bentonow.drive.listener;

import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import com.bentonow.drive.util.DebugUtils;

/**
 * Created by Kokusho on 17/12/15.
 */
public class SignalStrengthListener extends PhoneStateListener {

    public static String TAG = "SignalStrengthListener";

    PhoneStateInterface listener;

    public SignalStrengthListener(PhoneStateInterface mListener) {
        listener = mListener;
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        DebugUtils.logDebug(TAG, "State changed: " + stateName(state));
    }

    private String stateName(int state) {
        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                return "Idle";
            case TelephonyManager.CALL_STATE_OFFHOOK:
                return "Off hook";
            case TelephonyManager.CALL_STATE_RINGING:
                return "Ringing";
        }
        return Integer.toString(state);
    }


    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        // get the signal strength (a value between 0 and 31)
        int strengthAmplitude = signalStrength.getGsmSignalStrength();
        // do something with it (in this case we update a text view)
        DebugUtils.logDebug(TAG, "State changed: ", strengthAmplitude + "");
        listener.phoneStateInterface(strengthAmplitude);
    }

}

