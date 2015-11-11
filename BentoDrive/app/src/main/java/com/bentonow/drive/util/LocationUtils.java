package com.bentonow.drive.util;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.bentonow.drive.Application;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Jose Torres on 9/9/15.
 */
public class LocationUtils {

    public static String getFullAddress(Address mAddress) {
        if (mAddress == null)
            return "";

        String sAddress = "";

        for (int i = 0; i < mAddress.getMaxAddressLineIndex(); ++i) {
            if (sAddress.length() > 0) sAddress += ", ";
            sAddress += mAddress.getAddressLine(i);
        }

        return sAddress;
    }

    public static String getStreetAddress(Address mAddress) {
        if (mAddress == null) return "";
        return mAddress.getThoroughfare() + ", " + mAddress.getSubThoroughfare();
    }

    public static String getCustomAddress(Address mAddress) {
        if (mAddress == null)
            return "";

        String sAddress = "";

        for (int i = 0; i < mAddress.getMaxAddressLineIndex(); ++i) {
            String sLine;
            if (sAddress.length() > 0)
                sAddress += ", ";

            if (i == 0)
                sLine = mAddress.getSubThoroughfare() + " " + mAddress.getThoroughfare();
            else
                sLine = mAddress.getAddressLine(i);

            if (sLine != null && !sLine.equals("null"))
                sAddress += sLine;
        }

        DebugUtils.logDebug("getCustomAddress()", sAddress);

        return sAddress;
    }

    public static Address getAddressFromLocation(Location mCurrentLocation) {
        List<Address> matches;
        Address mAddress = null;
        Geocoder geoCoder = new Geocoder(Application.getInstance());

        try {
            matches = geoCoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1);
            if (matches != null && !matches.isEmpty())
                mAddress = matches.get(0);
        } catch (Exception e) {
            DebugUtils.logError("getAddressFromLocation()", "scanCurrentLocation() " + e);
        }


        return mAddress;
    }


    public static Address getAddressFromLocation(LatLng mCurrentLocation) {
        List<Address> matches;
        Address mAddress = null;
        Geocoder geoCoder = new Geocoder(Application.getInstance());

        try {
            matches = geoCoder.getFromLocation(mCurrentLocation.latitude, mCurrentLocation.longitude, 1);
            if (matches != null && !matches.isEmpty())
                mAddress = matches.get(0);
        } catch (Exception e) {
            DebugUtils.logError("getAddressFromLocation()", "scanCurrentLocation() " + e);
        }


        return mAddress;
    }
}

