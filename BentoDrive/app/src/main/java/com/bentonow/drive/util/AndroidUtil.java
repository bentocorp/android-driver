package com.bentonow.drive.util;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings.Secure;
import android.support.v4.app.FragmentActivity;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.bentonow.drive.Application;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;

public class AndroidUtil {

    private static final Hashtable<String, Typeface> typefaceCache = new Hashtable<String, Typeface>();

    public static Point displaySize = new Point();


    public static void checkDisplaySize() {
        try {
            WindowManager manager = (WindowManager) Application.getInstance().getSystemService(Context.WINDOW_SERVICE);
            if (manager != null) {
                Display display = manager.getDefaultDisplay();
                if (display != null) {
                    if (Build.VERSION.SDK_INT < 13) {
                        displaySize.set(display.getWidth(), display.getHeight());
                    } else {
                        display.getSize(displaySize);
                    }
                    DebugUtils.logError("tmessages", "display size = " + displaySize.x + " " + displaySize.y);
                }
            }
        } catch (Exception e) {
            DebugUtils.logError("tmessages", e.getMessage());
        }
    }

    public static void showKeyboard(final Context context, final EditText view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, 0);
    }

    public static void hideKeyboard(View view) {
        if (view == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (!imm.isActive()) {
            return;
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static boolean isMaterial() {
        if (Build.VERSION.SDK_INT >= 21)
            return true;
        else
            return false;
    }

    public static boolean isJellyBean() {
        if (Build.VERSION.SDK_INT >= 16)
            return true;
        else
            return false;
    }

    public static Typeface getTypeface(String assetPath) {
        synchronized (typefaceCache) {
            if (!typefaceCache.containsKey(assetPath)) {
                try {
                    Typeface t = Typeface.createFromAsset(Application.getInstance().getAssets(), assetPath);
                    typefaceCache.put(assetPath, t);
                } catch (Exception e) {
                    DebugUtils.logDebug("Typefaces", "Could not get typeface '" + assetPath + "' because " + e.getMessage());
                    return null;
                }
            }
            return typefaceCache.get(assetPath);
        }
    }

    public static int dpToPx(float dp) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Application.getInstance().getResources().getDisplayMetrics());
        return (int) px;
    }

    public static int dpToPx(float dp, Resources resources) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
        return (int) px;
    }

    public static int getScreenDensity(FragmentActivity act) {
        DisplayMetrics metrics = new DisplayMetrics();
        act.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int screenDensity = 0;
        switch (metrics.densityDpi) {
            case DisplayMetrics.DENSITY_LOW:
                screenDensity = 1;
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                screenDensity = 2;
                break;
            case DisplayMetrics.DENSITY_HIGH:
                screenDensity = 3;
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                screenDensity = 4;
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                screenDensity = 5;
                break;
            default:
                screenDensity = 0;
                break;
        }
        return screenDensity;
    }


    public static String getAndroidId(Context ctx) {
        String android_id = Secure.getString(ctx.getContentResolver(), Secure.ANDROID_ID);
        return android_id;
    }

    public static boolean isTablet(Context ctx) {
        return (ctx.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static String getVersionName() {
        String versionName = "N/A";
        try {
            PackageInfo pInfo = Application.getInstance().getPackageManager().getPackageInfo(Application.getInstance().getPackageName(), 0);
            versionName = pInfo.versionName;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            DebugUtils.logError(e);
        }
        return versionName;
    }

    public static int getCodeName(Context context) {
        int versionName = 0;
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = pInfo.versionCode;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            DebugUtils.logError(e);
        }
        return versionName;
    }

    public static int dp(float value) {
        return (int) Math.ceil(Application.getInstance().getResources().getDisplayMetrics().density * value);
    }

    public static float dpf2(float value) {
        return Application.getInstance().getResources().getDisplayMetrics().density * value;
    }

    public static float dpf2(int dimenValue) {
        return Application.getInstance().getResources().getDisplayMetrics().density * Application.getInstance().getResources().getDimension(dimenValue);
    }


    public static int getWidthScreen(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    public static int getHeightScreen(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    public static String getLanguage() {
        String result = "en";
        if (Locale.getDefault().getDisplayLanguage().equals("español"))
            result = "es";

        return result;
    }

    public static boolean emailVerification(String email) {
        if (email == null)
            return false;
        else
            return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean urlVerification(String url) {
        boolean isValid = true;
        if (url == null || url.isEmpty())
            isValid = false;
        else
            isValid = Patterns.WEB_URL.matcher(url).matches();
        return isValid;
    }

    public static boolean phoneVerification(String phone) {
        if (phone == null)
            return false;
        else
            return Patterns.PHONE.matcher(phone).matches();
    }


    public static void backToAndroidMenu(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    public static void makeCall(Context context, String phone) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phone));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    public static String getOperatorNanme(Context context) {
        TelephonyManager telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
        return telephonyManager.getSimOperatorName();

    }

    public static String getSimCountryIso(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getSimCountryIso().toUpperCase();
    }

    public static String getClipboardText() {
        ClipboardManager clipboard = (ClipboardManager) Application.getInstance().getSystemService(Activity.CLIPBOARD_SERVICE);
        if (clipboard.getText() == null)
            return "";
        else
            return clipboard.getText().toString();
    }

    public static void setClipboardText(String sClipboard) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) Application.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(sClipboard);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) Application.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", sClipboard);
            clipboard.setPrimaryClip(clip);
        }
    }


    public static boolean sendSms(ArrayList<String> arrayListPhone, String sMessage) {
        boolean bSentSuccess;
        try {
            SmsManager smsManager = SmsManager.getDefault();
            for (int a = 0; a < arrayListPhone.size(); a++) {
                smsManager.sendTextMessage(arrayListPhone.get(a), null, sMessage, null, null);
            }
            bSentSuccess = true;
        } catch (Exception e) {
            DebugUtils.logError("Send SMS", e);
            bSentSuccess = false;
        }
        return bSentSuccess;
    }

    public static void populateSmsApp(Context context, String sPhone, String sMessage) {
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setData(Uri.parse("sms:" + sPhone));
        sendIntent.putExtra("sms_body", sMessage);
        context.startActivity(sendIntent);
    }

    public static void populateSmsApp(Context context, ArrayList<String> arrayListPhone, String sMessage) {
        String toNumbers = "";
        for (String s : arrayListPhone) {
            toNumbers = toNumbers + s + ";";
        }
        toNumbers = toNumbers.substring(0, toNumbers.length() - 1);

        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setData(Uri.parse("smsto:" + toNumbers));
        sendIntent.putExtra("sms_body", sMessage);
        try {
            context.startActivity(sendIntent);
        } catch (Exception e) {
            DebugUtils.logError("PopulateSms", e);
        }
    }

    public static void sendMsms(Context mContext, String sPhone, String sMessage, String sUri) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra("sms_body", sMessage);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(sUri));
        intent.setType("image/jpeg");
        try {
            mContext.startActivity(Intent.createChooser(intent, "Send"));
        } catch (Exception e) {
            DebugUtils.logError("PopulateSms", e);
        }
    }

    public static void sendSms(Context mContext, String sPhone, String sMessage) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra("sms_body", sMessage);
        try {
            mContext.startActivity(Intent.createChooser(intent, "Send"));
        } catch (Exception e) {
            DebugUtils.logError("PopulateSms", e);
        }
    }

    public static String getAndroidId() {
        // return Secure.getString(Application.getInstance().getContentResolver(), Secure.ANDROID_ID);
        TelephonyManager tManager = (TelephonyManager) Application.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
        return tManager.getDeviceId();
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static boolean isGooglePlayServicesAvailable(FragmentActivity mContext) {
        boolean bIsAvailable;
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        if (resultCode == ConnectionResult.SUCCESS) {
            bIsAvailable = true;
        } else {
            bIsAvailable = false;
            if (resultCode == ConnectionResult.SERVICE_MISSING ||
                    resultCode == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED ||
                    resultCode == ConnectionResult.SERVICE_DISABLED)
                GooglePlayServicesUtil.getErrorDialog(resultCode, mContext, 1).show();
        }
        return bIsAvailable;
    }

}
