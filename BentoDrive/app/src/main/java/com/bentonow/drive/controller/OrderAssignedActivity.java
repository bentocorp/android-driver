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

import com.bentonow.drive.R;
import com.bentonow.drive.dialog.ProgressDialog;
import com.bentonow.drive.listener.NodeEventsListener;
import com.bentonow.drive.listener.WebSocketEventListener;
import com.bentonow.drive.model.OrderItemModel;
import com.bentonow.drive.model.ResponseModel;
import com.bentonow.drive.parse.jackson.MainParser;
import com.bentonow.drive.socket.WebSocketService;
import com.bentonow.drive.util.AndroidUtil;
import com.bentonow.drive.util.BentoDriveUtil;
import com.bentonow.drive.util.ConstantUtil;
import com.bentonow.drive.util.DebugUtils;
import com.bentonow.drive.util.SharedPreferencesUtil;
import com.bentonow.drive.util.SocialNetworksUtil;
import com.bentonow.drive.util.WidgetsUtils;
import com.bentonow.drive.web.BentoRestClient;
import com.bentonow.drive.widget.material.ButtonFlat;
import com.bentonow.drive.widget.material.DialogMaterial;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

/**
 * Created by Jose Torres on 11/10/15.
 */
public class OrderAssignedActivity extends MainActivity implements View.OnClickListener, NodeEventsListener {

    public static final String TAG = "OrderAssignedActivity";

    private ImageView imgMenuItemLogOut;
    private FrameLayout mContainerMessage;
    private FrameLayout mContainerCall;
    private FrameLayout mContainerMap;
    private FrameLayout mContainerBack;
    private ButtonFlat btnAcceptOrder;
    private ButtonFlat btnRejectOrder;
    private ButtonFlat btnCompleteOrder;
    private ButtonFlat btnArrivedOrder;

    private TextView txtOrderContent;
    private TextView txtStatus;

    private ProgressDialog mLoaderDialog;

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
        getContainerBack().setOnClickListener(this);

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
                getTxtStatus().setVisibility(View.GONE);
                break;
            case "ACCEPTED":
                getBtnAcceptOrder().setVisibility(View.GONE);
                getBtnRejectOrder().setVisibility(View.GONE);
                getBtnArrivedOrder().setVisibility(View.VISIBLE);
                getBtnCompleteOrder().setVisibility(View.GONE);
                getTxtStatus().setVisibility(View.GONE);
                break;
            case "ARRIVED":
                getBtnAcceptOrder().setVisibility(View.GONE);
                getBtnRejectOrder().setVisibility(View.GONE);
                getBtnArrivedOrder().setVisibility(View.GONE);
                getBtnCompleteOrder().setVisibility(View.VISIBLE);
                getTxtStatus().setVisibility(View.GONE);
                break;
            case "REJECTED":
                getBtnAcceptOrder().setVisibility(View.GONE);
                getBtnRejectOrder().setVisibility(View.GONE);
                getBtnArrivedOrder().setVisibility(View.GONE);
                getBtnCompleteOrder().setVisibility(View.GONE);
                getTxtStatus().setVisibility(View.VISIBLE);
                break;
        }
    }


    private void setNodeListener() {
        webSocketService.onNodeEventListener(OrderAssignedActivity.this);
    }

    private void logInDrive() {
        if (!webSocketService.isConnectedUser()) {
            getLoaderDialog().show();

            DebugUtils.logDebug(TAG, "Attempting to connect to node");

            webSocketService.connectWebSocket(SharedPreferencesUtil.getStringPreference(OrderAssignedActivity.this, SharedPreferencesUtil.USER_NAME),
                    SharedPreferencesUtil.getStringPreference(OrderAssignedActivity.this, SharedPreferencesUtil.PASSWORD), new WebSocketEventListener() {
                        @Override
                        public void onAuthenticationSuccess(String sToken) {
                            setNodeListener();
                        }

                        @Override
                        public void onAuthenticationFailure(String sReason) {
                            WidgetsUtils.createShortToast("There was a problem: " + sReason);
                            BentoDriveUtil.disconnectUser(OrderAssignedActivity.this, false);
                        }
                    });

        } else {
            setNodeListener();
        }
    }

    private void acceptOrder() {

        getLoaderDialog().show();

        BentoRestClient.getStatusOrder(ConstantUtil.optStatusOrder.ACCEPT, mOrderModel, new TextHttpResponseHandler() {
            @SuppressWarnings("deprecation")
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                DebugUtils.logError(TAG, "Code: " + statusCode);
                DebugUtils.logError(TAG, "Response: " + responseString);

                dismissDialog();
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                try {
                    ResponseModel mResponse = MainParser.getObjectMapper().readValue(responseString, ResponseModel.class);

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
                } catch (Exception ex) {
                    DebugUtils.logError(TAG, ex);
                }

                dismissDialog();
            }

        });

    }

    private void rejectOrder() {

        getLoaderDialog().show();

        BentoRestClient.getStatusOrder(ConstantUtil.optStatusOrder.REJECT, mOrderModel, new TextHttpResponseHandler() {
            @SuppressWarnings("deprecation")
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                DebugUtils.logError(TAG, "Code: " + statusCode);
                DebugUtils.logError(TAG, "Response: " + responseString);

                dismissDialog();
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                try {
                    ResponseModel mResponse = MainParser.getObjectMapper().readValue(responseString, ResponseModel.class);

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

                } catch (Exception ex) {
                    DebugUtils.logError(TAG, ex);
                }

                dismissDialog();
            }

        });

    }


    private void arrivedOrder() {

        getLoaderDialog().show();

        BentoRestClient.getStatusOrder(ConstantUtil.optStatusOrder.ARRIVED, mOrderModel, new TextHttpResponseHandler() {
            @SuppressWarnings("deprecation")
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                DebugUtils.logError(TAG, "Code: " + statusCode);
                DebugUtils.logError(TAG, "Response: " + responseString);

                dismissDialog();
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                try {
                    ResponseModel mResponse = MainParser.getObjectMapper().readValue(responseString, ResponseModel.class);
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
                } catch (Exception ex) {
                    DebugUtils.logError(TAG, ex);
                }

                dismissDialog();
            }

        });
    }

    private void completeOrder() {

        getLoaderDialog().show();

        BentoRestClient.getStatusOrder(ConstantUtil.optStatusOrder.COMPLETE, mOrderModel, new TextHttpResponseHandler() {
            @SuppressWarnings("deprecation")
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                DebugUtils.logError(TAG, "Code: " + statusCode);
                DebugUtils.logError(TAG, "Response: " + responseString);

                dismissDialog();
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                try {
                    ResponseModel mResponse = MainParser.getObjectMapper().readValue(responseString, ResponseModel.class);
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
                } catch (Exception ex) {
                    DebugUtils.logError(TAG, ex);
                }

                dismissDialog();
            }

        });
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
            logInDrive();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            DebugUtils.logDebug(TAG, "Disconnected from service " + name);
            mBound = true;
        }
    }


    @Override
    public void onPush(OrderItemModel mOrderModel) {

        switch (mOrderModel.getOrderType()) {
            case "ASSIGN":

                break;
            case "UNASSIGN":

                break;
            case "REPRIORITIZE":

                break;
            default:
                DebugUtils.logDebug(TAG, "OrderType: Unhandled " + mOrderModel.getOrderType());
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.container_message:
                if (mOrderModel != null)
                    AndroidUtil.populateSmsApp(OrderAssignedActivity.this, mOrderModel.getPhone(), String.format(getString(R.string.order_sms_message), mOrderModel.getName()));
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
                DialogMaterial mDialog = new DialogMaterial(OrderAssignedActivity.this, getString(R.string.dialog_title_accept_task), getString(R.string.dialog_msg_accept_task));
                mDialog.addCancelButton("Cancel");
                mDialog.addAcceptButton("Accept", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        acceptOrder();
                    }
                });
                mDialog.show();
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
            case R.id.container_back:
                onBackPressed();
                break;
            default:
                DebugUtils.logError(TAG, "OnClick(): " + v.getId());
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent mIntent = new Intent();
        mIntent.putExtra(OrderItemModel.TAG, mOrderModel);
        setResult(RESULT_OK, mIntent);
        super.onBackPressed();
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

    private ProgressDialog getLoaderDialog() {
        if (mLoaderDialog == null)
            mLoaderDialog = new ProgressDialog(OrderAssignedActivity.this, "Progressing....");
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

    private FrameLayout getContainerBack() {
        if (mContainerBack == null)
            mContainerBack = (FrameLayout) findViewById(R.id.container_back);
        return mContainerBack;
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

    private TextView getTxtStatus() {
        if (txtStatus == null)
            txtStatus = (TextView) findViewById(R.id.txt_status);
        return txtStatus;
    }


}
