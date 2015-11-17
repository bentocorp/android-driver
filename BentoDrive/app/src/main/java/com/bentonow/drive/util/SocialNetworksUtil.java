package com.bentonow.drive.util;

import java.security.MessageDigest;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;

import com.bentonow.drive.Application;


public class SocialNetworksUtil {

    public static void generateKeyHash() {
        try {
            PackageInfo info = Application.getInstance().getPackageManager().getPackageInfo("com.bentonow.drive", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                DebugUtils.logDebug(Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (Exception e) {
            DebugUtils.logError(e);
        }
    }

    public static void openFacebookPage(Activity act, String pageId) {
        final String urlFb = "fb://page/" + pageId;
        Intent pageIntent = new Intent(Intent.ACTION_VIEW);
        pageIntent.setData(Uri.parse(urlFb));

        // If Facebook application is installed, use that else launch a browser
        final PackageManager packageManager = act.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(
                pageIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() == 0) {
            final String urlBrowser = "https://www.facebook.com/pages/"
                    + pageId;
            pageIntent.setData(Uri.parse(urlBrowser));
        }

        act.startActivity(pageIntent);
    }

    public static void openFacebookProfile(Activity act, String userId) {
        final String urlFb = "fb://profile/" + userId;
        Intent pageIntent = new Intent(Intent.ACTION_VIEW);
        pageIntent.setData(Uri.parse(urlFb));

        // If Facebook application is installed, use that else launch a browser
        final PackageManager packageManager = act.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(
                pageIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() == 0) {
            final String urlBrowser = "https://www.facebook.com/"
                    + userId;
            pageIntent.setData(Uri.parse(urlBrowser));
        }

        act.startActivity(pageIntent);
    }

    public static void openTwitterUser(Activity act, String userId) {
        final String urlTwitter = "twitter://user?user_id=" + userId;

        Intent pageIntent = new Intent(Intent.ACTION_VIEW);
        pageIntent.setData(Uri.parse(urlTwitter));

        // If Twitter application is installed, use that else launch a browser
        final PackageManager packageManager = act.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(
                pageIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() == 0) {
            final String urlBrowser = "https://twitter.com/" + userId;
            pageIntent.setData(Uri.parse(urlBrowser));
        }

        act.startActivity(pageIntent);
    }

    public static void openGoogleUser(Activity act, String userId) {
        final String urlTwitter = "https://plus.google.com/" + userId;

        Intent pageIntent = new Intent(Intent.ACTION_VIEW);
        pageIntent.setData(Uri.parse(urlTwitter));

        // If Twitter application is installed, use that else launch a browser
        final PackageManager packageManager = act.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(
                pageIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() == 0) {
            final String urlBrowser = "https://plus.google.com/" + userId;
            pageIntent.setData(Uri.parse(urlBrowser));
        }

        act.startActivity(pageIntent);
    }

    public static void openYoutubeVideo(Activity act, String videoId) {
        final String urlVideo = "vnd.youtube://" + videoId;

        Intent pageIntent = new Intent(Intent.ACTION_VIEW);
        pageIntent.setData(Uri.parse(urlVideo));

        // If Twitter application is installed, use that else launch a browser
        final PackageManager packageManager = act.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(
                pageIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() == 0) {
            final String urlBrowser = "https://www.youtube.com/watch?v=" + videoId;
            pageIntent.setData(Uri.parse(urlBrowser));
        }

        act.startActivity(pageIntent);
    }

    public static void openWazeLocation(Activity act, double latitude, double longitude) {
        final String urlLocation = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f", latitude, longitude, latitude, longitude);
        final String urlWaze = "waze://?ll=" + latitude + "," + longitude + "&navigate=yes";

        Intent pageIntent = new Intent(Intent.ACTION_VIEW);
        pageIntent.setData(Uri.parse(urlLocation));

        final PackageManager packageManager = act.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(pageIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() != 0)
            pageIntent.setData(Uri.parse(urlWaze));

        act.startActivity(pageIntent);
    }


}