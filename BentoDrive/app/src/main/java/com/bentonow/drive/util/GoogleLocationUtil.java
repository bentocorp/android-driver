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

    public static void connectGoogleApi(final Context mContext, final UpdateLocationListener mListener) {
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        DebugUtils.logDebug(TAG, "buildGoogleApiClient() onConnected:");
                        if (mListener != null) {
                            startLocationUpdates(mContext, mListener);
                            mListener.onLocationUpdated(getCurrentLocation());
                        } else
                            WidgetsUtils.createLongToast("buildGoogleApiClient() onConnected: Listener Lost");
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        DebugUtils.logDebug(TAG, "buildGoogleApiClient() onConnectionSuspended: " + i);
                        WidgetsUtils.createLongToast("buildGoogleApiClient() onConnectionSuspended: " + i);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        DebugUtils.logDebug(TAG, "buildGoogleApiClient() " + connectionResult.toString());
                        WidgetsUtils.createLongToast("onConnectionFailed() " + connectionResult.toString());
                    }
                })
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    public static void startLocationUpdates(final Context mContext, final UpdateLocationListener mListener) {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, getLocationRequest(), new LocationListener() {
                @Override
                public void onLocationChanged(Location mCurrentLocation) {
                    if (mListener != null && mCurrentLocation != null)
                        mListener.onLocationUpdated(mCurrentLocation);

                }
            });
        } else
            connectGoogleApi(mContext, mListener);
    }

    public static void stopLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, new LocationListener() {
                @Override
                public void onLocationChanged(Location mLocation) {
                    DebugUtils.logDebug(TAG, "stopLocationUpdates() onLocationChanged: " + mLocation.toString());
                }
            });
            mGoogleApiClient.disconnect();
        }
    }


    public static LocationRequest getLocationRequest() {
        if (mLocationRequest == null) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(2000);
            mLocationRequest.setFastestInterval(1000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
        return mLocationRequest;
    }

    public static Location getCurrentLocation() {
        Location mCurrentLocation = null;
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        return mCurrentLocation;
    }

}
