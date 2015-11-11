package com.bentonow.drive.controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.EditText;

import com.bentonow.drive.R;
import com.bentonow.drive.listener.WebSocketEventListener;
import com.bentonow.drive.socket.WebSocketService;
import com.bentonow.drive.util.BentoDriveUtil;
import com.bentonow.drive.util.DebugUtils;
import com.bentonow.drive.util.SharedPreferencesUtil;
import com.bentonow.drive.widget.material.ButtonRectangle;

/**
 * Created by Jose Torres on 11/10/15.
 */
public class LogInActivity extends MainActivity implements View.OnClickListener {

    public static final String TAG = "LogInActivity";

    private EditText editUsername;
    private EditText editPassword;
    private ButtonRectangle btnLogIn;

    private WebSocketService webSocketService = null;
    private ServiceConnection mConnection = new WebSocketServiceConnection();

    private boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        if (BentoDriveUtil.bIsKokushoTesting) {
            getEditUsername().setText("marc@bentonow.com");
            getEditPassword().setText("wordpass");
        }

        getBtnLogIn().setOnClickListener(this);
    }

    private void logInDrive() {
        if (mBound) {
            boolean bIsConnected = webSocketService.isConnectedUser();
            if (!bIsConnected) {
                DebugUtils.logDebug(TAG, "Attempting to connect to node");
                webSocketService.connectWebSocket(getEditUsername().getText().toString(), getEditPassword().getText().toString(), new WebSocketEventListener() {
                    @Override
                    public void onAuthenticationSuccess(String sToken) {
                        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.TOKEN, sToken);
                        DebugUtils.logDebug(TAG, "Token: " + sToken);
                    }
                });
            } else {
                webSocketService.disconnectWebSocket();

            }
        }
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private class WebSocketServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            DebugUtils.logDebug(TAG, "Successfully bounded to " + name.getClassName());
            WebSocketService.WebSocketServiceBinder webSocketServiceBinder = (WebSocketService.WebSocketServiceBinder) binder;
            webSocketService = webSocketServiceBinder.getService();
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
    protected void onStop() {
        super.onStop();
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


    private ButtonRectangle getBtnLogIn() {
        if (btnLogIn == null)
            btnLogIn = (ButtonRectangle) findViewById(R.id.btn_login);
        return btnLogIn;
    }

}
