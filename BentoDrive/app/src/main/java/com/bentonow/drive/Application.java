package com.bentonow.drive;

/**
 * Created by Jose Torres on 07/05/15.
 */

import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;

import com.bentonow.drive.util.DebugUtils;


public class Application extends android.app.Application {

    private static Application singleton;
    public Handler mHandler;
    private Thread mThread;

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

    public Handler getHandler() {
        if (mHandler == null)
            mHandler = new Handler(Looper.getMainLooper());

        return mHandler;
    }


    public void handlerPost(Runnable runnable) {
        getHandler().post(runnable);
    }

    public void handlerDelayPost(Runnable runnable, long delay) {
        getHandler().postDelayed(runnable, delay);
    }

    public void runThread(Runnable runnable) {
        mThread = new Thread(runnable);
        mThread.setDaemon(true);
        mThread.start();
    }

    public void cancelRunOnUIThread(Runnable runnable) {
        getHandler().removeCallbacks(runnable);
    }

    public void stopThreads() {
        if (mThread != null)
            mThread.interrupt();
    }

}
