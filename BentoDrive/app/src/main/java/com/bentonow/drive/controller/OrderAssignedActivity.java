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
import com.bentonow.drive.model.sugar.OrderItemDAO;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jose Torres on 11/10/15.
 */
public class OrderAssignedActivity extends MainActivity implements View.OnClickListener, NodeEventsListener {

    public static final String TAG = "OrderAssignedActivity";

    public static final String TAG_ORDER_ID = "OrderId";

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
    private TextView txtToolbarSubtitle;

    private ProgressDialog mLoaderDialog;

    private WebSocketService webSocketService = null;
    private ServiceConnection mConnection = new WebSocketServiceConnection();

    private List<OrderItemModel> aListOder = new ArrayList<>();
    ArrayList<OrderItemModel> aTempListOder = new ArrayList<>();

    //private OrderItemModel mOrderModel;

    private boolean mBound = false;
    private boolean mInFront = false;

    private long lOrderId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_bento);

        lOrderId = getIntent().getLongExtra(TAG_ORDER_ID, 0);

        /*mOrderModel = OrderItemDAO.getOrderById(lOrderId);

        if (mOrderModel == null)
            finish();*/

        // mOrderModel = getIntent().getParcelableExtra(OrderItemModel.TAG);

        aListOder = OrderItemDAO.getAllTask();

        if (aListOder.isEmpty())
            finish();

        getContainerMessage().setOnClickListener(this);
        getContainerCall().setOnClickListener(this);
        getContainerMap().setOnClickListener(this);
        getContainerBack().setOnClickListener(this);

        getBtnAcceptOrder().setOnClickListener(this);
        getBtnRejectOrder().setOnClickListener(this);
        getBtnArrivedOrder().setOnClickListener(this);
        getBtnCompleteOrder().setOnClickListener(this);

        getTxtOrderContent().setText(aListOder.get(0).getItem());
        getTxtToolbarSubtitle().setText(aListOder.get(0).getName());

        updateUI();


    }

    private void updateUI() {

        switch (aListOder.get(0).getStatus()) {
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

        OrderItemDAO.update(aListOder.get(0));
        aListOder = OrderItemDAO.getAllTask();
    }


    private void setNodeListener() {
        webSocketService.onNodeEventListener(OrderAssignedActivity.this);
    }

    private void logInDrive() {
        if (!webSocketService.isConnectedUser()) {
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

                        @Override
                        public void onDisconnect(boolean disconnectingPurposefully) {
                            if (disconnectingPurposefully) {
                                aListOder.clear();
                                OrderItemDAO.deleteAll();
                                finish();
                            } else {
                                WidgetsUtils.createShortToast(R.string.error_reconnect);
                            }
                        }
                    });

        } else {
            setNodeListener();
        }
    }

    private void acceptOrder() {

        getLoaderDialog().show();

        BentoRestClient.getStatusOrder(ConstantUtil.optStatusOrder.ACCEPT, aListOder.get(0), new TextHttpResponseHandler() {
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
                                    aListOder.get(0).setStatus("ACCEPTED");
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
                                            aListOder.get(0).setStatus("ACCEPTED");
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

        BentoRestClient.getStatusOrder(ConstantUtil.optStatusOrder.REJECT, aListOder.get(0), new TextHttpResponseHandler() {
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
                                    aListOder.get(0).setStatus("REJECTED");
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
                                            aListOder.get(0).setStatus("REJECTED");
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

        BentoRestClient.getStatusOrder(ConstantUtil.optStatusOrder.ARRIVED, aListOder.get(0), new TextHttpResponseHandler() {
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
                                    aListOder.get(0).setStatus("ARRIVED");
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
                                            aListOder.get(0).setStatus("ARRIVED");
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

        BentoRestClient.getStatusOrder(ConstantUtil.optStatusOrder.COMPLETE, aListOder.get(0), new TextHttpResponseHandler() {
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
                                    completeTask();
                                }
                            });
                            break;
                        case 1:
                            if (mResponse.getMsg() != null && !mResponse.getMsg().isEmpty()) {
                                if (BentoDriveUtil.isInvalidPhoneNumber(mResponse.getMsg())) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            completeTask();
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

    private void completeTask() {
        aTempListOder.clear();

        for (int a = 1; a < aListOder.size(); a++)
            aTempListOder.add(aListOder.get(a));

        refreshData();

        onBackPressed();
    }

    private void refreshData() {
        OrderItemDAO.saveAll(aTempListOder);
        aListOder = OrderItemDAO.getAllTask();

        DebugUtils.logDebug(TAG, "Total Orders: " + aListOder.size());
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
    public void onPush(OrderItemModel mOrder) {
        if (mInFront) {
            DebugUtils.logDebug(TAG, "Push: " + aListOder.get(0).getOrderId() + " Type: " + mOrder.getOrderType() + " After: " + mOrder.getAfter());
            aTempListOder.clear();
            int iOrderId = 0;

            switch (mOrder.getOrderType()) {
                case "ASSIGN":
                    if (aListOder.get(0).getOrderId().equals(mOrder.getAfter())) {
                        aTempListOder.add(mOrder);
                        aTempListOder.addAll(aListOder);

                        OrderItemDAO.saveAll(aTempListOder);
                        WidgetsUtils.createShortToast(R.string.notification_change_task);

                        finish();
                    } else if (mOrder.getAfter().isEmpty()) {
                        aTempListOder.addAll(aListOder);
                        aTempListOder.add(mOrder);

                        refreshData();
                    } else {
                        for (int a = 0; a < aListOder.size(); a++) {
                            if (aListOder.get(a).getOrderId().equals(mOrder.getAfter())) {
                                aTempListOder.add(mOrder);
                            }
                            aTempListOder.add(aListOder.get(a));
                        }

                        refreshData();
                    }
                    break;
                case "UNASSIGN":
                    for (int a = 0; a < aListOder.size(); a++) {
                        if (aListOder.get(a).getOrderId().equals(mOrder.getOrderId()))
                            iOrderId = a;
                        else
                            aTempListOder.add(aListOder.get(a));
                    }

                    refreshData();

                    if (iOrderId == 0) {
                        if (aTempListOder.isEmpty())
                            WidgetsUtils.createShortToast(R.string.notification_un_assigned_task);
                        else
                            WidgetsUtils.createShortToast(R.string.notification_change_task);

                        finish();
                    }

                    break;
                case "REPRIORITIZE":
                    for (int a = 0; a < aListOder.size(); a++) {
                        if (aListOder.get(a).getOrderId().equals(mOrder.getAfter())) {
                            aTempListOder.add(mOrder);
                            iOrderId = a;
                        }

                        if (!aListOder.get(a).getOrderId().equals(mOrder.getOrderId()))
                            aTempListOder.add(aListOder.get(a));
                    }
                    if (mOrder.getAfter().isEmpty())
                        aTempListOder.add(mOrder);

                    refreshData();

                    if (iOrderId == 0) {
                        WidgetsUtils.createShortToast(R.string.notification_change_task);
                        finish();
                    }
                    break;
                default:
                    DebugUtils.logDebug(TAG, "OrderType: Unhandled " + aListOder.get(0).getOrderType());
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.container_message:
                if (aListOder.get(0) != null) {
                    AndroidUtil.populateSmsApp(OrderAssignedActivity.this, aListOder.get(0).getPhone(), String.format(getString(R.string.order_sms_message), aListOder.get(0).getName()));
                }
                break;
            case R.id.container_call:
                if (aListOder.get(0) != null && aListOder.get(0).getPhone() != null) {
                    DialogMaterial mAcceptDialog = new DialogMaterial(OrderAssignedActivity.this, getString(R.string.dialog_title_accept_task), getString(R.string.dialog_msg_accept_task));
                    mAcceptDialog.addCancelButton("Copy To Clipboard", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AndroidUtil.setClipboardText(aListOder.get(0).getPhone());
                        }
                    });
                    mAcceptDialog.addAcceptButton("Call", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AndroidUtil.makeCall(OrderAssignedActivity.this, aListOder.get(0).getPhone());
                        }
                    });
                    mAcceptDialog.show();
                } else
                    WidgetsUtils.createShortToast("There was a problem in the call");
                break;
            case R.id.container_map:
                if (aListOder.get(0) != null) {
                    DialogMaterial mAcceptDialog = new DialogMaterial(OrderAssignedActivity.this, getString(R.string.dialog_title_accept_task), getString(R.string.dialog_msg_accept_task));
                    mAcceptDialog.addCancelButton("Show options", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SocialNetworksUtil.openLocation(OrderAssignedActivity.this, aListOder.get(0).getAddress().getLat(), aListOder.get(0).getAddress().getLng());
                        }
                    });
                    mAcceptDialog.addAcceptButton("Open waze", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SocialNetworksUtil.openWazeLocation(OrderAssignedActivity.this, aListOder.get(0).getAddress().getLat(), aListOder.get(0).getAddress().getLng());
                        }
                    });
                    mAcceptDialog.show();
                } else
                    WidgetsUtils.createShortToast("There was a problem in the map");
                break;
            case R.id.btn_accept_order:
                DialogMaterial mAcceptDialog = new DialogMaterial(OrderAssignedActivity.this, getString(R.string.dialog_title_accept_task), getString(R.string.dialog_msg_accept_task));
                mAcceptDialog.addCancelButton("Cancel");
                mAcceptDialog.addAcceptButton("Accept", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        acceptOrder();
                    }
                });
                mAcceptDialog.show();
                break;
            case R.id.btn_reject_order:
                DialogMaterial mRejectDialog = new DialogMaterial(OrderAssignedActivity.this, getString(R.string.dialog_title_reject_task), getString(R.string.dialog_msg_reject_task));
                mRejectDialog.addCancelButton("Cancel");
                mRejectDialog.addAcceptButton("Reject", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rejectOrder();
                    }
                });
                mRejectDialog.show();
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
        super.onBackPressed();
       /* Intent mIntent = new Intent();
        mIntent.putExtra(OrderItemModel.TAG, mOrderModel);
        setResult(RESULT_OK, mIntent);*/
        // mOrderModel.save();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mInFront = true;
        Intent intent = new Intent(this, WebSocketService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webSocketService != null)
            webSocketService.removeNodeListener();
        mInFront = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            //  unbindService(mConnection);
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


    private TextView getTxtToolbarSubtitle() {
        if (txtToolbarSubtitle == null)
            txtToolbarSubtitle = (TextView) findViewById(R.id.txt_toolbar_subtitle);
        return txtToolbarSubtitle;
    }


}
