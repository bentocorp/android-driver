package com.bentonow.drive.controller;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bentonow.drive.R;
import com.bentonow.drive.listener.DialogSelectListener;
import com.bentonow.drive.model.sugar.OrderItemDAO;
import com.bentonow.drive.util.AndroidUtil;
import com.bentonow.drive.util.BentoDriveUtil;
import com.bentonow.drive.util.SharedPreferencesUtil;
import com.bentonow.drive.util.SocialNetworksUtil;
import com.bentonow.drive.widget.material.DialogMaterial;

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

        new CountDownTimer(2 * 600, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                /*forceDownloadLink();*/
                SharedPreferencesUtil.setAppPreference(SplashActivity.this, SharedPreferencesUtil.IS_USER_LOG_IN, false);
                BentoDriveUtil.openLogInActivity(SplashActivity.this);
            }
        }.start();
    }

    private void forceDownloadLink() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DialogMaterial mDialog = new DialogMaterial(SplashActivity.this, "New Version", "There is a new version available");
                mDialog.addAcceptButton("Download", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SocialNetworksUtil.openWebUrl(SplashActivity.this, "http://dl.dropboxusercontent.com/u/20121288/Apps-O-rama/BentoDrive/app-stage-debug.apk");
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
