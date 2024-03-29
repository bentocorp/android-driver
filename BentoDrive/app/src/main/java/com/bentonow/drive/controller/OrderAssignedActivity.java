package com.bentonow.drive.controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bentonow.drive.Application;
import com.bentonow.drive.R;
import com.bentonow.drive.dialog.ProgressDialog;
import com.bentonow.drive.listener.WebSocketEventListener;
import com.bentonow.drive.model.OrderItemModel;
import com.bentonow.drive.model.ResponseModel;
import com.bentonow.drive.model.sugar.OrderItemDAO;
import com.bentonow.drive.parse.jackson.BentoOrderJsonParser;
import com.bentonow.drive.parse.jackson.MainParser;
import com.bentonow.drive.socket.WebSocketService;
import com.bentonow.drive.util.AndroidUtil;
import com.bentonow.drive.util.BentoDriveUtil;
import com.bentonow.drive.util.ConstantUtil;
import com.bentonow.drive.util.DebugUtils;
import com.bentonow.drive.util.SocialNetworksUtil;
import com.bentonow.drive.util.SoundUtil;
import com.bentonow.drive.util.WidgetsUtils;
import com.bentonow.drive.web.BentoRestClient;
import com.bentonow.drive.widget.material.ButtonFlat;
import com.bentonow.drive.widget.material.DialogMaterial;
import com.crashlytics.android.Crashlytics;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jose Torres on 11/10/15.
 */
public class OrderAssignedActivity extends MainActivity implements View.OnClickListener, WebSocketEventListener {

    public static final String TAG = "OrderAssignedActivity";

    public static final String TAG_ORDER_ID = "OrderId";
    public static boolean bIsOpen;
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
    private List<OrderItemModel> aTempListOder = new ArrayList<>();
    private boolean mBound = false;
    private boolean mReconnecting = false;
    private String sOrderId = "";
    private DialogMaterial mAcceptDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_bento);

        getContainerMessage().setOnClickListener(this);
        getContainerCall().setOnClickListener(this);
        getContainerMap().setOnClickListener(this);
        getContainerBack().setOnClickListener(this);

        getBtnAcceptOrder().setOnClickListener(this);
        getBtnRejectOrder().setOnClickListener(this);
        getBtnArrivedOrder().setOnClickListener(this);
        getBtnCompleteOrder().setOnClickListener(this);
    }

    private void updateUI() {
        getTxtOrderContent().setText(webSocketService.getListTask().get(0).getItem());
        getTxtToolbarSubtitle().setText(webSocketService.getListTask().get(0).getName());

        switch (webSocketService.getListTask().get(0).getStatus()) {
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

        OrderItemDAO.update(webSocketService.getListTask().get(0));

        webSocketService.saveListTask(webSocketService.getListTask());
    }


    private void bindService() {
        Intent intent = new Intent(this, WebSocketService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void openInvalidPhoneNumberDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAcceptDialog = new DialogMaterial(OrderAssignedActivity.this, getString(R.string.dialog_title_invalid_phone), getString(R.string.dialog_title_invalid_address));
                mAcceptDialog.addAcceptButton("Roger that!");
                mAcceptDialog.show();
            }
        });
    }

    private void acceptOrder() {
        showLoader("Progressing....", true);

        BentoRestClient.getStatusOrder(ConstantUtil.optStatusOrder.ACCEPT, webSocketService.getListTask().get(0), new TextHttpResponseHandler() {
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

                    DebugUtils.logDebug(TAG, "Response Code: " + mResponse.getCode() + "Message: " + mResponse.getMsg());

                    switch (mResponse.getCode()) {
                        case 0:
                            if (mResponse.getMsg() != null && !mResponse.getMsg().isEmpty())
                                WidgetsUtils.createShortToast("Error: " + mResponse.getMsg());

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    webSocketService.getListTask().get(0).setStatus("ACCEPTED");
                                    SoundUtil.playNotificationSound(Uri.parse("android.resource://" + Application.getInstance().getPackageName() + "/raw/lets_drive"));
                                    updateUI();
                                }
                            });
                            break;
                        default:
                            if (mResponse.getMsg() != null && !mResponse.getMsg().isEmpty()) {
                                if (BentoDriveUtil.isInvalidPhoneNumber(mResponse.getMsg())) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            webSocketService.getListTask().get(0).setStatus("ACCEPTED");
                                            SoundUtil.playNotificationSound(Uri.parse("android.resource://" + Application.getInstance().getPackageName() + "/raw/lets_drive"));
                                            updateUI();
                                            openInvalidPhoneNumberDialog();
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

        showLoader("Progressing....", true);

        BentoRestClient.getStatusOrder(ConstantUtil.optStatusOrder.REJECT, webSocketService.getListTask().get(0), new TextHttpResponseHandler() {
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

                    DebugUtils.logDebug(TAG, "Response Code: " + mResponse.getCode() + "Message: " + mResponse.getMsg());

                    switch (mResponse.getCode()) {
                        case 0:
                            if (mResponse.getMsg() != null && !mResponse.getMsg().isEmpty())
                                WidgetsUtils.createShortToast("Error: " + mResponse.getMsg());

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    webSocketService.getListTask().get(0).setStatus("REJECTED");
                                    updateUI();
                                }
                            });
                            break;
                        default:
                            if (mResponse.getMsg() != null && !mResponse.getMsg().isEmpty()) {
                                if (BentoDriveUtil.isInvalidPhoneNumber(mResponse.getMsg())) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            webSocketService.getListTask().get(0).setStatus("REJECTED");
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

        showLoader("Progressing....", true);

        BentoRestClient.getStatusOrder(ConstantUtil.optStatusOrder.ARRIVED, webSocketService.getListTask().get(0), new TextHttpResponseHandler() {
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
                                    webSocketService.getListTask().get(0).setStatus("ARRIVED");
                                    SoundUtil.playNotificationSound(Uri.parse("android.resource://" + Application.getInstance().getPackageName() + "/raw/notified"));
                                    updateUI();
                                }
                            });
                            break;
                        default:
                            if (mResponse.getMsg() != null && !mResponse.getMsg().isEmpty()) {
                                if (BentoDriveUtil.isInvalidPhoneNumber(mResponse.getMsg())) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            webSocketService.getListTask().get(0).setStatus("ARRIVED");
                                            //SoundUtil.playNotificationSound(Uri.parse("android.resource://" + Application.getInstance().getPackageName() + "/raw/invalid_phone"));
                                            updateUI();
                                            openInvalidPhoneNumberDialog();
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

        showLoader("Progressing....", true);

        BentoRestClient.getStatusOrder(ConstantUtil.optStatusOrder.COMPLETE, webSocketService.getListTask().get(0), new TextHttpResponseHandler() {
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
                        default:
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

        for (int a = 1; a < webSocketService.getListTask().size(); a++)
            aTempListOder.add(webSocketService.getListTask().get(a));

        SoundUtil.playNotificationSound(Uri.parse("android.resource://" + Application.getInstance().getPackageName() + "/raw/good_job"));

        refreshData();

        onBackPressed();
    }

    private void refreshData() {
        webSocketService.saveListTask(aTempListOder);

        DebugUtils.logDebug(TAG, "Total Orders: " + webSocketService.getListTask().size());
    }

    private void showLoader(final String sMessage, final boolean bCancelable) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLoaderDialog = new ProgressDialog(OrderAssignedActivity.this, sMessage);
                mLoaderDialog.setCancelable(bCancelable);
                mLoaderDialog.show();
            }
        });
    }

    private void dismissDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mLoaderDialog != null)
                    mLoaderDialog.dismiss();
                if (mAcceptDialog != null)
                    mAcceptDialog.dismiss();
            }
        });
    }


    private void getAssignedOrders() {
        BentoRestClient.getAssignedOrders(null, new TextHttpResponseHandler() {
            @SuppressWarnings("deprecation")
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                DebugUtils.logError(TAG, "Code: " + statusCode);
                DebugUtils.logError(TAG, "Response: " + responseString);

                if (responseString == null || responseString.equals("null"))
                    WidgetsUtils.createShortToast("There was a problem pull to refresh");
                else
                    WidgetsUtils.createShortToast("There was a problem pull to refresh");

                dismissDialog();

            }

            @SuppressWarnings("deprecation")
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                DebugUtils.logDebug(TAG, "Order: " + responseString);

                dismissDialog();

                boolean bSameBento;

                aTempListOder = BentoOrderJsonParser.parseBentoListOrder(responseString);

                if (aTempListOder.isEmpty()) {
                    bSameBento = false;
                } else {
                    bSameBento = sOrderId.equals(aTempListOder.get(0).getOrderId());
                }

                webSocketService.saveListTask(aTempListOder);

                if (!bSameBento) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onBackPressed();
                        }
                    });
                }

            }

        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.container_message:
                if (webSocketService.getListTask().get(0) != null) {
                    AndroidUtil.populateSmsApp(OrderAssignedActivity.this, webSocketService.getListTask().get(0).getPhone(), String.format(getString(R.string.order_sms_message),
                            webSocketService.getListTask().get(0).getName()));
                }
                break;
            case R.id.container_call:
                if (webSocketService.getListTask().get(0) != null && webSocketService.getListTask().get(0).getPhone() != null) {
                    mAcceptDialog = new DialogMaterial(OrderAssignedActivity.this, getString(R.string.dialog_title_phone), webSocketService.getListTask().get(0).getPhone());
                    mAcceptDialog.addCancelButton("Copy To Clipboard", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AndroidUtil.setClipboardText(webSocketService.getListTask().get(0).getPhone());
                        }
                    });
                    mAcceptDialog.addAcceptButton("Call", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AndroidUtil.makeCall(OrderAssignedActivity.this, webSocketService.getListTask().get(0).getPhone());
                        }
                    });
                    mAcceptDialog.show();
                } else
                    WidgetsUtils.createShortToast("There was a problem in the call");
                break;
            case R.id.container_map:
                if (webSocketService.getListTask().get(0) != null) {
                    mAcceptDialog = new DialogMaterial(OrderAssignedActivity.this, getString(R.string.dialog_title_address), BentoDriveUtil.getFormatAddress(webSocketService.getListTask().get(0).getAddress()));
                    mAcceptDialog.addCancelButton("Show Options", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SocialNetworksUtil.openLocation(OrderAssignedActivity.this, webSocketService.getListTask().get(0).getAddress().getLat(), webSocketService.getListTask().get(0).getAddress().getLng());
                        }
                    });
                    mAcceptDialog.addAcceptButton("Open Waze", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // SocialNetworksUtil.openWazeAddress(OrderAssignedActivity.this, webSocketService.getListTask().get(0).getAddress().get, webSocketService.getListTask().get(0).getAddress().getLng());
                            SocialNetworksUtil.openWazeAddress(OrderAssignedActivity.this, BentoDriveUtil.getFormatAddress(webSocketService.getListTask().get(0).getAddress()));
                        }
                    });
                    mAcceptDialog.show();
                } else
                    WidgetsUtils.createShortToast("There was a problem in the map");
                break;
            case R.id.btn_accept_order:
                mAcceptDialog = new DialogMaterial(OrderAssignedActivity.this, getString(R.string.dialog_title_accept_task), getString(R.string.dialog_msg_accept_task));
                mAcceptDialog.addCancelButton("Cancel");
                mAcceptDialog.addAcceptButton("Accept", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!webSocketService.getListTask().isEmpty()) {
                            acceptOrder();
                        } else {
                            WidgetsUtils.createShortToast("Sorry that task is not available anymore");
                            finish();
                        }
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
    public void onReconnecting() {
        if (!mReconnecting) {
            mReconnecting = true;
            showLoader("Connecting...", false);
        }
    }

    @Override
    public void onPong() {

    }

    @Override
    public void onConnectionError(String sReason) {
        DebugUtils.logError(TAG, sReason);
    }

    @Override
    public void onAuthenticationSuccess(String token) {
        if (mReconnecting) {
            dismissDialog();
            WidgetsUtils.createShortToast("Connection Restored");
            showLoader("Downloading...", true);
            getAssignedOrders();
        }

        mReconnecting = false;
    }

    @Override
    public void onAuthenticationFailure(String reason) {
        BentoDriveUtil.disconnectUser(OrderAssignedActivity.this, true);
    }

    @Override
    public void onDisconnect(boolean disconnectingPurposefully) {
        if (!disconnectingPurposefully && !mReconnecting) {
            mReconnecting = true;
            showLoader("Connecting...", false);
        }
    }

    @Override
    public void onAssign(List<OrderItemModel> mNewList, boolean bRefresh) {
        if (mNewList.isEmpty() || !sOrderId.equals(mNewList.get(0).getOrderId())) {
            finish();
        }
    }

    @Override
    public void onUnassign(List<OrderItemModel> mNewList, boolean bRefresh) {
        if (mNewList.isEmpty() || !sOrderId.equals(mNewList.get(0).getOrderId())) {
            finish();
        }
    }

    @Override
    public void onReprioritize(List<OrderItemModel> mNewList, boolean bRefresh) {
        if (mNewList.isEmpty() || !sOrderId.equals(mNewList.get(0).getOrderId())) {
            finish();
        }
    }

    @Override
    public void onModify() {
        dismissDialog();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateUI();
                DialogMaterial mAcceptDialog = new DialogMaterial(OrderAssignedActivity.this, null, getString(R.string.notification_modify_task));
                mAcceptDialog.addAcceptButton("Roger that!");
                if (!isFinishing())
                    mAcceptDialog.show();
            }
        });
    }

    @Override
    public void onTransportEventError(String sError) {

    }

    @Override
    public void onTransportEventClose(String sError) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService();
        bIsOpen = true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (webSocketService != null && !sOrderId.equals("")) {
            webSocketService.setWebSocketLister(OrderAssignedActivity.this);
            if (!webSocketService.getListTask().get(0).getOrderId().equals(sOrderId)) {
                finish();
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DebugUtils.logDebug(TAG, "OnDestroy()");
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        bIsOpen = false;
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

    private class WebSocketServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            DebugUtils.logDebug(TAG, "Successfully bounded to " + name.getClassName());
            WebSocketService.WebSocketServiceBinder webSocketServiceBinder = (WebSocketService.WebSocketServiceBinder) binder;
            webSocketService = webSocketServiceBinder.getService();
            webSocketService.setWebSocketLister(OrderAssignedActivity.this);
            webSocketService.onNodeEventListener();

            mBound = true;

            if (webSocketService.getListTask().isEmpty())
                finish();
            else {
                sOrderId = webSocketService.getListTask().get(0).getOrderId();
                updateUI();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            DebugUtils.logDebug(TAG, "Disconnected from service " + name);
            mBound = true;

            Crashlytics.log(TAG + " Disconnected from service " + name.toString());

            onBackPressed();
        }
    }


}
