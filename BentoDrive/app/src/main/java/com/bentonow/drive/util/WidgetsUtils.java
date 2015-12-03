/**
 * @author Kokusho Torres
 * 02/09/2014
 */
package com.bentonow.drive.util;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.bentonow.drive.Application;


public class WidgetsUtils {

    public static Handler mHandler;

    /**
     * Method that creates a short toast based in a String
     *
     * @param message The String that is going to be show
     */
    public static void createShortToast(final String message) {
        getHandler().post(new Runnable() {
            public void run() {
                Toast.makeText(Application.getInstance(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Method that creates a long Toast Message based in a string
     *
     * @param message The String that is going to be show
     */
    public static void createLongToast(final String message) {
        getHandler().post(new Runnable() {
            public void run() {
                Toast.makeText(Application.getInstance(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Method that creates a short Toast based in a string id
     *
     * @param id The Int from the String id
     */
    public static void createShortToast(final int id) {
        getHandler().post(new Runnable() {
            public void run() {
                Toast.makeText(Application.getInstance(), Application.getInstance().getString(id), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Method that creates a long Toast Message based in a string id
     *
     * @param id
     */
    public static void createLongToast(final int id) {
        getHandler().post(new Runnable() {
            public void run() {
                Toast.makeText(Application.getInstance(), Application.getInstance().getString(id), Toast.LENGTH_LONG).show();
            }
        });
    }

    public static Handler getHandler() {
        if (mHandler == null)
            mHandler = new Handler(Looper.getMainLooper());

        return mHandler;
    }

}
