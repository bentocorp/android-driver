package com.bentonow.drive.controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.bentonow.drive.R;
import com.bentonow.drive.dialog.ProgressDialog;
import com.bentonow.drive.listener.WebSocketEventListener;
import com.bentonow.drive.model.OrderItemModel;
import com.bentonow.drive.socket.WebSocketService;
import com.bentonow.drive.util.AndroidUtil;
import com.bentonow.drive.util.BentoDriveUtil;
import com.bentonow.drive.util.DebugUtils;
import com.bentonow.drive.util.LocationUtils;
import com.bentonow.drive.util.SharedPreferencesUtil;
import com.bentonow.drive.util.WidgetsUtils;
import com.bentonow.drive.widget.material.DialogMaterial;

import java.util.List;

/**
 * Created by Jose Torres on 11/10/15.
 */
public class LogInActivity extends MainActivity implements View.OnClickListener, WebSocketEventListener {

    public static final String TAG = "LogInActivity";

    private EditText editUsername;
    private EditText editPassword;
    private Button btnLogIn;

    private ProgressDialog mLoaderDialog;

    private WebSocketService webSocketService = null;
    private ServiceConnection mConnection = new WebSocketServiceConnection();

    private boolean mBound = false;
    private boolean bAlreadyOpen = false;

    private DialogMaterial mDialogMaterial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        if (BentoDriveUtil.bIsKokushoTesting) {
            getEditUsername().setText("jose.torres@gmail.com");
            getEditPassword().setText("bento");
        } else if (SharedPreferencesUtil.getBooleanPreference(LogInActivity.this, SharedPreferencesUtil.USE_SAVED_SETTINGS)) {
            getEditUsername().setText(SharedPreferencesUtil.getStringPreference(LogInActivity.this, SharedPreferencesUtil.USER_NAME));
            getEditPassword().setText(SharedPreferencesUtil.getStringPreference(LogInActivity.this, SharedPreferencesUtil.PASSWORD));
        }

        getBtnLogIn().setOnClickListener(this);
    }

    private boolean isValidField() {
        boolean bIsValid = true;

        if (getEditUsername().getText().toString().isEmpty() || getEditPassword().getText().toString().isEmpty()) {
            bIsValid = false;
            mDialogMaterial = new DialogMaterial(LogInActivity.this, "Error", getString(R.string.dialog_msg_login_empty));
            mDialogMaterial.addAcceptButton("OK");
            mDialogMaterial.show();
        } else if (!LocationUtils.isGpsEnable(LogInActivity.this)) {
            bIsValid = false;
            LocationUtils.showGpsDialog(LogInActivity.this);
        }

        return bIsValid;
    }

    private void logInDrive() {
        if (mBound) {
            if (AndroidUtil.isNetworkAvailable(LogInActivity.this)) {
                getLoaderDialog().show();
                startLogInCountDown();

                webSocketService.connectWebSocket(getEditUsername().getText().toString(), getEditPassword().getText().toString());
            } else
                WidgetsUtils.createShortToast(R.string.error_failed_no_internet);
        }
    }

    private void startLogInCountDown() {
        new CountDownTimer(15000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                if (!bAlreadyOpen && webSocketService != null) {
                    hideDialogs();
                    webSocketService.disconnectWebSocket();
                    WidgetsUtils.createShortToast(R.string.error_node_connection);
                }

            }
        }.start();
    }

    private void hideDialogs() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getLoaderDialog().dismiss();
            }
        });
    }

    @Override
    public void onSuccessfulConnection() {

    }

    @Override
    public void onConnectionError(String sReason) {

    }

    @Override
    public void onConnectionLost(boolean bPurpose) {

    }

    @Override
    public void onAuthenticationSuccess(String sToken) {
        if (!bAlreadyOpen) {
            bAlreadyOpen = true;
            SharedPreferencesUtil.setAppPreference(LogInActivity.this, SharedPreferencesUtil.USER_NAME, getEditUsername().getText().toString());
            SharedPreferencesUtil.setAppPreference(LogInActivity.this, SharedPreferencesUtil.PASSWORD, getEditPassword().getText().toString());
            SharedPreferencesUtil.setAppPreference(LogInActivity.this, SharedPreferencesUtil.USE_SAVED_SETTINGS, true);

            DebugUtils.logDebug(TAG, "Token: " + sToken);
            hideDialogs();

            BentoDriveUtil.openListBentoActivity(LogInActivity.this);
        }

    }

    @Override
    public void onAuthenticationFailure(String sReason) {
        hideDialogs();

        if (sReason.contains("database query came back empty"))
            WidgetsUtils.createShortToast(R.string.error_failed_authenticate);
        else
            WidgetsUtils.createShortToast("There was a problem: " + sReason);

    }

    @Override
    public void onDisconnect(boolean disconnectingPurposefully) {
        hideDialogs();

        if (!disconnectingPurposefully)
            WidgetsUtils.createShortToast(R.string.error_node_connection);
    }

    @Override
    public void onAssign(List<OrderItemModel> mNewList, boolean bRefresh) {

    }

    @Override
    public void onUnassign(List<OrderItemModel> mNewList, boolean bRefresh) {

    }

    @Override
    public void onReprioritize(List<OrderItemModel> mNewList, boolean bRefresh) {

    }

    private class WebSocketServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            DebugUtils.logDebug(TAG, "Successfully bounded to " + name.getClassName());
            WebSocketService.WebSocketServiceBinder webSocketServiceBinder = (WebSocketService.WebSocketServiceBinder) binder;
            webSocketService = webSocketServiceBinder.getService();
            webSocketService.setWebSocketLister(LogInActivity.this);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            DebugUtils.logDebug(TAG, "Disconnected from service " + name);
            mBound = true;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                if (isValidField())
                    logInDrive();
                break;
            default:
                DebugUtils.logError(TAG, "OnClick(): " + v.getId());
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, WebSocketService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    private EditText getEditUsername() {
        if (editUsername == null)
            editUsername = (EditText) findViewById(R.id.edit_username);
        return editUsername;
    }

    private EditText getEditPassword() {
        if (editPassword == null)
            editPassword = (EditText) findViewById(R.id.edit_password);
        return editPassword;
    }


    private Button getBtnLogIn() {
        if (btnLogIn == null)
            btnLogIn = (Button) findViewById(R.id.btn_login);
        return btnLogIn;
    }

    private ProgressDialog getLoaderDialog() {
        if (mLoaderDialog == null)
            mLoaderDialog = new ProgressDialog(LogInActivity.this, "Logging in");
        return mLoaderDialog;
    }

}
