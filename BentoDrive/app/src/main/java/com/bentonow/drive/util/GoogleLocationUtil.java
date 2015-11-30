package com.bentonow.drive.util;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.bentonow.drive.listener.UpdateLocationListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Jose Torres on 10/1/15.
 */
public class GoogleLocationUtil {

    public static final String TAG = "GoogleLocationUtil";

    private static LocationRequest mLocationRequest;
    private static GoogleApiClient mGoogleApiClient;


    public static synchronized GoogleApiClient getGoogleApiClient(final Context mContext, final UpdateLocationListener mListener) {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                            DebugUtils.logDebug(TAG, "buildGoogleApiClient() onConnected:");
                            if (mListener != null)
                                startLocationUpdates(mContext, mListener);
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            DebugUtils.logDebug(TAG, "buildGoogleApiClient() onConnectionSuspended: " + i);
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {
                            DebugUtils.logDebug(TAG, "buildGoogleApiClient() " + connectionResult.toString());
                        }
                    })
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }
        return mGoogleApiClient;
    }

    public static void startLocationUpdates(final Context mContext, final UpdateLocationListener mListener) {
        if (getGoogleApiClient(mContext, mListener).isConnected())
            LocationServices.FusedLocationApi.requestLocationUpdates(getGoogleApiClient(mContext, null), getLocationRequest(), new LocationListener() {
                @Override
                public void onLocationChanged(Location mCurrentLocation) {
                    if (mListener != null)
                        mListener.onLocationUpdated(mCurrentLocation);

                }
            });
    }

    public static void stopLocationUpdates(final Context mContext) {
        if (getGoogleApiClient(mContext, null).isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(getGoogleApiClient(mContext, null), new LocationListener() {
                @Override
                public void onLocationChanged(Location mLocation) {
                    DebugUtils.logDebug(TAG, "stopLocationUpdates() onLocationChanged: " + mLocation.toString());
                }
            });
    }


    public static LocationRequest getLocationRequest() {
        if (mLocationRequest == null) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(100000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
        return mLocationRequest;
    }

    public static Location getCurrentLocation(final Context mContext) {
        Location mCurrentLocation = null;
        if (getGoogleApiClient(mContext, null).isConnected())
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(getGoogleApiClient(mContext, null));

        return mCurrentLocation;
    }

}
