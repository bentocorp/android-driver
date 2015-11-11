package com.bentonow.drive;

/**
 * Created by Jose Torres on 07/05/15.
 */

import android.content.res.Configuration;
import android.os.Handler;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bentonow.drive.listener.InterfaceWebRequest;
import com.bentonow.drive.util.DebugUtils;


public class Application extends android.app.Application {

    private static Application singleton;
    public Handler mHandler = new Handler();
    private Thread mThread;
    private RequestQueue mRequestQueue;

    public static Application getInstance() {
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        singleton = this;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        DebugUtils.logDebug("Application", "onConfigurationChanged()");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        DebugUtils.logDebug("Application", "onLowMemory()");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        DebugUtils.logDebug("Application", "onTerminate()");
    }


    public void doInBackground(Runnable runnable) {
        new Thread(runnable).start();
    }


    public void handlerPost(Runnable runnable) {
        mHandler.post(runnable);
    }

    public void handlerDelayPost(Runnable runnable, long delay) {
        mHandler.postDelayed(runnable, delay);
    }

    public void runThread(Runnable runnable) {
        mThread = new Thread(runnable);
        mThread.setDaemon(true);
        mThread.start();
    }

    public void cancelRunOnUIThread(Runnable runnable) {
        mHandler.removeCallbacks(runnable);
    }

    public void stopThreads() {
        if (mThread != null)
            mThread.interrupt();
    }

    public RequestQueue getVolleyRequest() {
        if (mRequestQueue == null)
            mRequestQueue = Volley.newRequestQueue(Application.getInstance());
        return mRequestQueue;
    }

    public void webRequest(final InterfaceWebRequest interfaceWebRequest) {
        doInBackground(new Runnable() {
            @Override
            public void run() {
                interfaceWebRequest.dispatchRequest();
            }
        });
    }
}
