package com.bentonow.drive.controller;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
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
import com.bentonow.drive.util.WidgetsUtils;
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
    private Button btnRetry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getTxtAppVersion().setText(AndroidUtil.getVersionName());

        OrderItemDAO.deleteAll();
        SharedPreferencesUtil.setAppPreference(SplashActivity.this, SharedPreferencesUtil.IS_USER_LOG_IN, false);

        getBtnRetry().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMinVersion();
            }
        });

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

                switch (statusCode) {
                    case 0:
                        WidgetsUtils.createShortToast(R.string.error_failed_no_internet);
                        break;
                    default:
                        WidgetsUtils.createShortToast(responseString);
                        break;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getBtnRetry().setVisibility(View.VISIBLE);
                    }
                });

            }

            @SuppressWarnings("deprecation")
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                try {
                    DebugUtils.logDebug(TAG, responseString);
                    VersionModel mVersion = MinVersionJsonParser.parseMinVersion(responseString);
                    //mVersion.setMin_version("3");
                    //mVersion.setMin_version_url("http://s3-us-west-1.amazonaws.com/bentonow-assets/android_driver_app/AndroidDrive.html");

                    switch (mVersion.getiCode()) {
                        case 0:
                            boolean bErrorMin = false;
                            if (mVersion.getMin_version() == null || mVersion.getMin_version().isEmpty() || mVersion.getMin_version().equals("null")) {
                                mVersion.setMin_version("NULL");
                                bErrorMin = true;
                            } else if (mVersion.getMin_version_url() == null || mVersion.getMin_version_url().isEmpty() || mVersion.getMin_version_url().equals("null")) {
                                mVersion.setMin_version_url("NULL");
                                bErrorMin = true;
                            }

                            if (bErrorMin) {
                                DialogMaterial mDialog = new DialogMaterial(SplashActivity.this, "Error", "There seems to be a problem. Please inform dispatcher. +" +
                                        "\nAndroid Min: " + mVersion.getMin_version() + " \nAndroid Min URL: " + mVersion.getMin_version_url());
                                mDialog.addAcceptButton("Accept");
                                mDialog.show();
                            } else {
                                if (!BentoDriveUtil.bISValidVersion(mVersion.getMin_version())) {
                                    forceDownloadLink(mVersion.getMin_version_url(), mVersion.getMin_version());
                                } else {
                                    BentoDriveUtil.openLogInActivity(SplashActivity.this);
                                }
                            }

                            break;
                        case 1:
                            DialogMaterial mDialog = new DialogMaterial(SplashActivity.this, "Error", mVersion.getsMessage());
                            mDialog.addAcceptButton("Accept");
                            mDialog.show();
                            break;
                    }


                } catch (Exception ex) {
                    DebugUtils.logError(TAG, ex);
                }
            }

        });
    }

    private void forceDownloadLink(final String sUrl, final String sVersionCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DialogMaterial mDialog = new DialogMaterial(SplashActivity.this, "Update Available", "Please update to the new version now. " +
                        "\nCurrent: " + AndroidUtil.getCodeName(SplashActivity.this) + " \nNew: " + sVersionCode);
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

    private Button getBtnRetry() {
        if (btnRetry == null)
            btnRetry = (Button) findViewById(R.id.btn_retry);
        return btnRetry;
    }


}
