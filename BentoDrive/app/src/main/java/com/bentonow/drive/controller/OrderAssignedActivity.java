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
import com.bentonow.drive.model.ResponseModel;
import com.bentonow.drive.socket.WebSocketService;
import com.bentonow.drive.util.AndroidUtil;
import com.bentonow.drive.util.BentoDriveUtil;
import com.bentonow.drive.util.ConstantUtil;
import com.bentonow.drive.util.DebugUtils;
import com.bentonow.drive.util.SocialNetworksUtil;
import com.bentonow.drive.util.WidgetsUtils;
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
    private ButtonFlat btnCompleteOrder;
    private ButtonFlat btnArrivedOrder;

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

        getContainerMessage().setOnClickListener(this);
        getContainerCall().setOnClickListener(this);
        getContainerMap().setOnClickListener(this);

        getBtnAcceptOrder().setOnClickListener(this);
        getBtnRejectOrder().setOnClickListener(this);
        getBtnArrivedOrder().setOnClickListener(this);
        getBtnCompleteOrder().setOnClickListener(this);

        getTxtOrderContent().setText(mOrderModel.getItem());

        updateUI();

    }

    private void updateUI() {

        switch (mOrderModel.getStatus()) {
            case "PENDING":
                getBtnAcceptOrder().setVisibility(View.VISIBLE);
                getBtnRejectOrder().setVisibility(View.VISIBLE);
                getBtnArrivedOrder().setVisibility(View.GONE);
                getBtnCompleteOrder().setVisibility(View.GONE);
                break;
            case "ACCEPTED":
                getBtnAcceptOrder().setVisibility(View.GONE);
                getBtnRejectOrder().setVisibility(View.GONE);
                getBtnArrivedOrder().setVisibility(View.VISIBLE);
                getBtnCompleteOrder().setVisibility(View.GONE);
                break;
            case "ARRIVED":
                getBtnAcceptOrder().setVisibility(View.GONE);
                getBtnRejectOrder().setVisibility(View.GONE);
                getBtnArrivedOrder().setVisibility(View.GONE);
                getBtnCompleteOrder().setVisibility(View.VISIBLE);
                break;
            case "REJECTED":
                getBtnAcceptOrder().setVisibility(View.VISIBLE);
                getBtnRejectOrder().setVisibility(View.GONE);
                getBtnArrivedOrder().setVisibility(View.GONE);
                getBtnCompleteOrder().setVisibility(View.GONE);
                break;
        }
    }

    private void acceptOrder() {
        getLoaderDialog().show();
        Application.getInstance().webRequest(new RequestGetStatusOrders(ConstantUtil.optStatusOrder.ACCEPT, mOrderModel, new ListenerWebRequest() {
            @Override
            public void onError(String sError) {
                onComplete();
            }

            @Override
            public void onResponse(Object oResponse) {
                ResponseModel mResponse = (ResponseModel) oResponse;

                switch (mResponse.getCode()) {
                    case 0:
                        if (mResponse.getMsg() != null && !mResponse.getMsg().isEmpty())
                            WidgetsUtils.createShortToast("Error: " + mResponse.getMsg());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mOrderModel.setStatus("ACCEPTED");
                                updateUI();
                            }
                        });
                        break;
                    case 1:
                        if (mResponse.getMsg() != null && !mResponse.getMsg().isEmpty()) {
                            if (BentoDriveUtil.isInvalidPhoneNumber(mResponse.getMsg())) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mOrderModel.setStatus("ACCEPTED");
                                        updateUI();
                                    }
                                });
                            } else
                                WidgetsUtils.createShortToast("Error: " + mResponse.getMsg());
                        }
                        break;
                }

                onComplete();
            }

            @Override
            public void onComplete() {
                dismissDialog();
                super.onComplete();
            }
        }));

    }

    private void rejectOrder() {
        getLoaderDialog().show();
        Application.getInstance().webRequest(new RequestGetStatusOrders(ConstantUtil.optStatusOrder.REJECT, mOrderModel, new ListenerWebRequest() {
            @Override
            public void onError(String sError) {
                onComplete();
            }

            @Override
            public void onResponse(Object oResponse) {
                ResponseModel mResponse = (ResponseModel) oResponse;
                switch (mResponse.getCode()) {
                    case 0:
                        if (mResponse.getMsg() != null && !mResponse.getMsg().isEmpty())
                            WidgetsUtils.createShortToast("Error: " + mResponse.getMsg());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mOrderModel.setStatus("REJECTED");
                                updateUI();
                            }
                        });
                        break;
                    case 1:
                        if (mResponse.getMsg() != null && !mResponse.getMsg().isEmpty()) {
                            if (BentoDriveUtil.isInvalidPhoneNumber(mResponse.getMsg())) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mOrderModel.setStatus("REJECTED");
                                        updateUI();
                                    }
                                });
                            } else
                                WidgetsUtils.createShortToast("Error: " + mResponse.getMsg());
                        }
                        break;
                }
                onComplete();
            }

            @Override
            public void onComplete() {
                dismissDialog();
                super.onComplete();
            }
        }));
    }


    private void arrivedOrder() {
        getLoaderDialog().show();
        Application.getInstance().webRequest(new RequestGetStatusOrders(ConstantUtil.optStatusOrder.ARRIVED, mOrderModel, new ListenerWebRequest() {
            @Override
            public void onError(String sError) {
                onComplete();
            }

            @Override
            public void onResponse(Object oResponse) {
                ResponseModel mResponse = (ResponseModel) oResponse;
                switch (mResponse.getCode()) {
                    case 0:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mOrderModel.setStatus("ARRIVED");
                                updateUI();
                            }
                        });
                        break;
                    case 1:
                        if (mResponse.getMsg() != null && !mResponse.getMsg().isEmpty()) {
                            if (BentoDriveUtil.isInvalidPhoneNumber(mResponse.getMsg())) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mOrderModel.setStatus("ARRIVED");
                                        updateUI();
                                    }
                                });
                            } else
                                WidgetsUtils.createShortToast("Error: " + mResponse.getMsg());
                        }
                        break;
                }

                onComplete();
            }

            @Override
            public void onComplete() {
                dismissDialog();
                super.onComplete();
            }
        }));
    }

    private void completeOrder() {
        getLoaderDialog().show();
        Application.getInstance().webRequest(new RequestGetStatusOrders(ConstantUtil.optStatusOrder.COMPLETE, mOrderModel, new ListenerWebRequest() {
            @Override
            public void onError(String sError) {
                onComplete();
            }

            @Override
            public void onResponse(Object oResponse) {
                ResponseModel mResponse = (ResponseModel) oResponse;
                switch (mResponse.getCode()) {
                    case 0:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mOrderModel.setStatus("COMPLETED");
                                onBackPressed();
                            }
                        });
                        break;
                    case 1:
                        if (mResponse.getMsg() != null && !mResponse.getMsg().isEmpty()) {
                            if (BentoDriveUtil.isInvalidPhoneNumber(mResponse.getMsg())) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mOrderModel.setStatus("COMPLETED");
                                        onBackPressed();
                                    }
                                });
                            } else
                                WidgetsUtils.createShortToast("Error: " + mResponse.getMsg());
                        }
                        break;
                }

                onComplete();
            }

            @Override
            public void onComplete() {
                dismissDialog();
                super.onComplete();
            }
        }));
    }

    private void dismissDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getLoaderDialog().dismiss();
            }
        });
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
                acceptOrder();
                break;
            case R.id.btn_reject_order:
                rejectOrder();
                break;
            case R.id.btn_arrived_order:
                arrivedOrder();
                break;
            case R.id.btn_complete_order:
                completeOrder();
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

    private ButtonFlat getBtnArrivedOrder() {
        if (btnArrivedOrder == null)
            btnArrivedOrder = (ButtonFlat) findViewById(R.id.btn_arrived_order);
        return btnArrivedOrder;
    }


    private ButtonFlat getBtnCompleteOrder() {
        if (btnCompleteOrder == null)
            btnCompleteOrder = (ButtonFlat) findViewById(R.id.btn_complete_order);
        return btnCompleteOrder;
    }

    private TextView getTxtOrderContent() {
        if (txtOrderContent == null)
            txtOrderContent = (TextView) findViewById(R.id.txt_order_content);
        return txtOrderContent;
    }


}
