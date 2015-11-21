package com.bentonow.drive.controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bentonow.drive.Application;
import com.bentonow.drive.R;
import com.bentonow.drive.dialog.LoaderDialog;
import com.bentonow.drive.listener.ListenerWebRequest;
import com.bentonow.drive.model.OrderItemModel;
import com.bentonow.drive.socket.WebSocketService;
import com.bentonow.drive.util.AndroidUtil;
import com.bentonow.drive.util.BentoDriveUtil;
import com.bentonow.drive.util.ConstantUtil;
import com.bentonow.drive.util.DebugUtils;
import com.bentonow.drive.util.SocialNetworksUtil;
import com.bentonow.drive.web.request.RequestGetStatusOrders;
import com.bentonow.drive.widget.material.ButtonFlat;

/**
 * Created by Jose Torres on 11/10/15.
 */
public class OrderAssignedActivity extends MainActivity implements View.OnClickListener {

    public static final String TAG = "ListOrderAssignedActivity";

    private ImageView imgMenuItemLogOut;
    private FrameLayout mContainerMessage;
    private FrameLayout mContainerCall;
    private FrameLayout mContainerMap;
    private ButtonFlat btnAcceptOrder;
    private ButtonFlat btnRejectOrder;
    private TextView txtOrderContent;

    private LoaderDialog mLoaderDialog;

    private WebSocketService webSocketService = null;
    private ServiceConnection mConnection = new WebSocketServiceConnection();

    private boolean mBound = false;

    private OrderItemModel mOrderModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_bento);

        mOrderModel = getIntent().getParcelableExtra(OrderItemModel.TAG);

        getMenuItemLogOut().setOnClickListener(this);
        getContainerMessage().setOnClickListener(this);
        getContainerCall().setOnClickListener(this);
        getContainerMap().setOnClickListener(this);

        getBtnAcceptOrder().setOnClickListener(this);
        getBtnRejectOrder().setOnClickListener(this);

        getTxtOrderContent().setText(mOrderModel.getItem());

    }

    private void acceptOrder() {
        Application.getInstance().webRequest(new RequestGetStatusOrders(ConstantUtil.optStatusOrder.ACCEPT, mOrderModel, new ListenerWebRequest() {
            @Override
            public void onError(String sError) {
                onComplete();
            }

            @Override
            public void onResponse(Object oResponse) {
                onComplete();
            }

            @Override
            public void onComplete() {
                super.onComplete();
            }
        }));

    }

    private void cancelOrder() {
        Application.getInstance().webRequest(new RequestGetStatusOrders(ConstantUtil.optStatusOrder.REJECT , mOrderModel, new ListenerWebRequest() {
            @Override
            public void onError(String sError) {
                onComplete();
            }

            @Override
            public void onResponse(Object oResponse) {
                onComplete();
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
                BentoDriveUtil.disconnectUser(OrderAssignedActivity.this);
                break;
            case R.id.container_message:
                if (mOrderModel != null)
                    AndroidUtil.sendSms(OrderAssignedActivity.this, mOrderModel.getPhone(), "Message");
                break;
            case R.id.container_call:
                if (mOrderModel != null)
                    AndroidUtil.makeCall(OrderAssignedActivity.this, mOrderModel.getPhone());
                break;
            case R.id.container_map:
                if (mOrderModel != null)
                    SocialNetworksUtil.openWazeLocation(OrderAssignedActivity.this, mOrderModel.getAddress().getLat(), mOrderModel.getAddress().getLng());
                break;
            case R.id.btn_accept_order:

                break;
            case R.id.btn_reject_order:

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

    private LoaderDialog getLoaderDialog() {
        if (mLoaderDialog == null)
            mLoaderDialog = new LoaderDialog(OrderAssignedActivity.this);
        return mLoaderDialog;
    }

    private ImageView getMenuItemLogOut() {
        if (imgMenuItemLogOut == null)
            imgMenuItemLogOut = (ImageView) findViewById(R.id.img_menu_item_log_out);
        return imgMenuItemLogOut;
    }

    private FrameLayout getContainerMessage() {
        if (mContainerMessage == null)
            mContainerMessage = (FrameLayout) findViewById(R.id.container_message);
        return mContainerMessage;
    }

    private FrameLayout getContainerCall() {
        if (mContainerCall == null)
            mContainerCall = (FrameLayout) findViewById(R.id.container_call);
        return mContainerCall;
    }

    private FrameLayout getContainerMap() {
        if (mContainerMap == null)
            mContainerMap = (FrameLayout) findViewById(R.id.container_map);
        return mContainerMap;
    }

    private ButtonFlat getBtnAcceptOrder() {
        if (btnAcceptOrder == null)
            btnAcceptOrder = (ButtonFlat) findViewById(R.id.btn_accept_order);
        return btnAcceptOrder;
    }

    private ButtonFlat getBtnRejectOrder() {
        if (btnRejectOrder == null)
            btnRejectOrder = (ButtonFlat) findViewById(R.id.btn_reject_order);
        return btnRejectOrder;
    }

    private TextView getTxtOrderContent() {
        if (txtOrderContent == null)
            txtOrderContent = (TextView) findViewById(R.id.txt_order_content);
        return txtOrderContent;
    }


}
