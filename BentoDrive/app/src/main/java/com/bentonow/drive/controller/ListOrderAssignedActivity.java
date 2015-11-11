package com.bentonow.drive.controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;

import com.bentonow.drive.Application;
import com.bentonow.drive.R;
import com.bentonow.drive.listener.ListenerWebRequest;
import com.bentonow.drive.socket.WebSocketService;
import com.bentonow.drive.util.BentoDriveUtil;
import com.bentonow.drive.util.DebugUtils;
import com.bentonow.drive.web.request.RequestGetAssignedOrders;

/**
 * Created by Jose Torres on 11/10/15.
 */
public class ListOrderAssignedActivity extends MainActivity implements View.OnClickListener {

    public static final String TAG = "ListOrderAssignedActivity";

    private ImageView imgMenuItemLogOut;

    private WebSocketService webSocketService = null;
    private ServiceConnection mConnection = new WebSocketServiceConnection();

    private boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_bento);

        getMenuItemLogOut().setOnClickListener(this);

        Application.getInstance().webRequest(new RequestGetAssignedOrders(new ListenerWebRequest() {
            @Override
            public void onError(String sError) {
                super.onError(sError);
            }

            @Override
            public void onResponse(Object oResponse) {
                super.onResponse(oResponse);
            }

            @Override
            public void onComplete() {
                super.onComplete();
            }
        }));

    }

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
            case R.id.img_menu_item_log_out:
                webSocketService.disconnectWebSocket();
                BentoDriveUtil.disconnectUser(ListOrderAssignedActivity.this);
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
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    private ImageView getMenuItemLogOut() {
        if (imgMenuItemLogOut == null)
            imgMenuItemLogOut = (ImageView) findViewById(R.id.img_menu_item_log_out);
        return imgMenuItemLogOut;
    }

}
