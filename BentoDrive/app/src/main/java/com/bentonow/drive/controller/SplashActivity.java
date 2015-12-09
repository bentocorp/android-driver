package com.bentonow.drive.controller;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bentonow.drive.R;
import com.bentonow.drive.listener.DialogSelectListener;
import com.bentonow.drive.model.VersionModel;
import com.bentonow.drive.model.sugar.OrderItemDAO;
import com.bentonow.drive.parse.jackson.MinVersionJsonParser;
import com.bentonow.drive.util.AndroidUtil;
import com.bentonow.drive.util.BentoDriveUtil;
import com.bentonow.drive.util.DebugUtils;
import com.bentonow.drive.util.SharedPreferencesUtil;
import com.bentonow.drive.util.SocialNetworksUtil;
import com.bentonow.drive.web.BentoRestClient;
import com.bentonow.drive.widget.material.DialogMaterial;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

/**
 * Created by Jose Torres on 11/10/15.
 */
public class SplashActivity extends MainActivity {

    public static final String TAG = "SplashActivity";

    private ImageView imgMenuItemLogOut;
    private TextView txtAppVersion;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getTxtAppVersion().setText(AndroidUtil.getVersionName());

        OrderItemDAO.deleteAll();
        SharedPreferencesUtil.setAppPreference(SplashActivity.this, SharedPreferencesUtil.IS_USER_LOG_IN, false);


    }

    private void nextScreen() {
        if (AndroidUtil.isGooglePlayServicesAvailable(SplashActivity.this)) {
            getMinVersion();
        }
    }

    private void getMinVersion() {
        BentoRestClient.getMinVersion(new TextHttpResponseHandler() {
            @SuppressWarnings("deprecation")
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                DebugUtils.logError(TAG, "Code: " + statusCode);
                DebugUtils.logError(TAG, "Response: " + responseString);

            }

            @SuppressWarnings("deprecation")
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                try {
                    VersionModel mVersion = MinVersionJsonParser.parseMinVersion(responseString);
                    // mVersion.setMin_version("2");
                    //mVersion.setMin_version_url("http://s3-us-west-1.amazonaws.com/bentonow-assets/android_driver_app/AndroidDrive.html");
                    if (mVersion.getMin_version() != null && !mVersion.getMin_version().isEmpty() && !mVersion.getMin_version().equals("null") && !mVersion.getMin_version().equals(String.valueOf(AndroidUtil.getCodeName(SplashActivity.this)))) {
                        forceDownloadLink(mVersion.getMin_version_url());
                    } else {
                        BentoDriveUtil.openLogInActivity(SplashActivity.this);
                    }

                } catch (Exception ex) {
                    DebugUtils.logError(TAG, ex);
                }
            }

        });
    }

    private void forceDownloadLink(final String sUrl) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DialogMaterial mDialog = new DialogMaterial(SplashActivity.this, "New Version", "There is a new version available");
                mDialog.addAcceptButton("Download", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SocialNetworksUtil.openWebUrl(SplashActivity.this, sUrl);
                    }
                });
                mDialog.addCancelButton("Cancel");
                mDialog.addCancelDialog(new DialogSelectListener() {
                    @Override
                    public void dialogConfirmation(Object obj) {
                        finish();
                    }
                });
                mDialog.show();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        new CountDownTimer(1 * 600, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                /*forceDownloadLink();
                SharedPreferencesUtil.setAppPreference(SplashActivity.this, SharedPreferencesUtil.IS_USER_LOG_IN, false);
                BentoDriveUtil.openLogInActivity(SplashActivity.this);*/
                nextScreen();
            }
        }.start();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    private ImageView getMenuItemLogOut() {
        if (imgMenuItemLogOut == null)
            imgMenuItemLogOut = (ImageView) findViewById(R.id.img_menu_item_log_out);
        return imgMenuItemLogOut;
    }


    private TextView getTxtAppVersion() {
        if (txtAppVersion == null)
            txtAppVersion = (TextView) findViewById(R.id.txt_app_version);
        return txtAppVersion;
    }


}
