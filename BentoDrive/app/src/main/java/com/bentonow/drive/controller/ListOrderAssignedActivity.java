package com.bentonow.drive.controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bentonow.drive.R;
import com.bentonow.drive.controller.adapter.OrderListAdapter;
import com.bentonow.drive.dialog.ProgressDialog;
import com.bentonow.drive.listener.NodeEventsListener;
import com.bentonow.drive.listener.RecyclerListListener;
import com.bentonow.drive.listener.WebSocketEventListener;
import com.bentonow.drive.model.OrderItemModel;
import com.bentonow.drive.model.sugar.OrderItemDAO;
import com.bentonow.drive.parse.jackson.BentoOrderJsonParser;
import com.bentonow.drive.socket.WebSocketService;
import com.bentonow.drive.util.BentoDriveUtil;
import com.bentonow.drive.util.DebugUtils;
import com.bentonow.drive.util.SharedPreferencesUtil;
import com.bentonow.drive.util.WidgetsUtils;
import com.bentonow.drive.web.BentoRestClient;
import com.bentonow.drive.widget.material.DialogMaterial;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jose Torres on 11/10/15.
 */
public class ListOrderAssignedActivity extends MainActivity implements View.OnClickListener, RecyclerListListener, NodeEventsListener {

    public static final String TAG = "ListOrderAssignedActivity";
    //public static final int TAG_ID = 2;

    private ImageView imgMenuItemLogOut;
    private TextView txtEmptyView;

    private RecyclerView mRecyclerView;
    private OrderListAdapter mAdapter;

    private ProgressDialog mLoaderDialog;

    private WebSocketService webSocketService = null;
    private ServiceConnection mConnection = new WebSocketServiceConnection();

    private List<OrderItemModel> aListOder = new ArrayList<>();

    private OrderItemModel mCurrentOrder;

    private boolean mBound = false;

    private boolean mIsFirstTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_bento);

        getMenuItemLogOut().setOnClickListener(this);

        getListOrder().setAdapter(getListAdapter());

        getLoaderDialog().show();

        mIsFirstTime = true;

    }

    private void refreshAssignedList() {
        getListAdapter().aListOrder.clear();

        OrderItemDAO.deleteAll();

        OrderItemDAO.save(aListOder);

        for (int a = 0; a < aListOder.size(); a++) {
            mCurrentOrder = aListOder.get(a);
            getListAdapter().aListOrder.add(mCurrentOrder);
            break;
        }

        DebugUtils.logDebug(TAG, "Total Orders: " + aListOder.size());

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getListAdapter().notifyDataSetChanged();
                getTxtEmptyView().setVisibility(getListAdapter().aListOrder.isEmpty() ? View.VISIBLE : View.GONE);
                getListOrder().setVisibility(getListAdapter().aListOrder.isEmpty() ? View.GONE : View.VISIBLE);

                if (mIsFirstTime) {
                    getLoaderDialog().dismiss();
                    mIsFirstTime = false;
                }
            }
        });
    }

    private void setNodeListener() {
        webSocketService.onNodeEventListener(ListOrderAssignedActivity.this);
    }

    private void logInDrive() {
        if (!webSocketService.isConnectedUser()) {
            DebugUtils.logDebug(TAG, "Attempting to connect to node");

            webSocketService.connectWebSocket(SharedPreferencesUtil.getStringPreference(ListOrderAssignedActivity.this, SharedPreferencesUtil.USER_NAME),
                    SharedPreferencesUtil.getStringPreference(ListOrderAssignedActivity.this, SharedPreferencesUtil.PASSWORD), new WebSocketEventListener() {
                        @Override
                        public void onAuthenticationSuccess(String sToken) {
                            setNodeListener();
                            getAssignedOrders();
                        }

                        @Override
                        public void onAuthenticationFailure(String sReason) {
                            WidgetsUtils.createShortToast("There was a problem: " + sReason);
                            BentoDriveUtil.disconnectUser(ListOrderAssignedActivity.this, false);
                        }


                        @Override
                        public void onDisconnect(boolean disconnectingPurposefully) {
                            if (!disconnectingPurposefully) {
                                WidgetsUtils.createShortToast(R.string.error_node_connection);
                                aListOder.clear();
                                OrderItemDAO.deleteAll();
                                setNodeListener();
                            }
                        }
                    });

        } else {
            setNodeListener();

            if (mIsFirstTime)
                getAssignedOrders();
        }
    }

    private void getAssignedOrders() {

        BentoRestClient.getAssignedOrders(null, new TextHttpResponseHandler() {
            @SuppressWarnings("deprecation")
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                DebugUtils.logError(TAG, "Code: " + statusCode);
                DebugUtils.logError(TAG, "Response: " + responseString);

                refreshAssignedList();
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                try {
                    DebugUtils.logDebug(TAG, "Order: " + responseString);
                    aListOder = BentoOrderJsonParser.parseBentoListOrder(responseString);

                } catch (Exception ex) {
                    DebugUtils.logError(TAG, ex);
                }

                refreshAssignedList();
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
            mBound = false;
        }

    }


    @Override
    public void onPush(OrderItemModel mOrder) {
        DebugUtils.logDebug(TAG, "Push: " + mOrder.getOrderId());

        switch (mOrder.getOrderType()) {
            case "ASSIGN":
                if (aListOder.isEmpty()) {
                    aListOder.add(mOrder);
                } else {
                    //  DebugUtils.logDebug(TAG, "CurrentId: " + mCurrentOrder.getId() + " AfterId: " + mOrder.getAfter());
                    if (mOrder.getAfter().isEmpty())
                        aListOder.add(mOrder);
                    else {
                        ArrayList<OrderItemModel> aTempListOder = new ArrayList<>();

                        for (int a = 0; a < aListOder.size(); a++) {
                            if (aListOder.get(a).getId().equals(mOrder.getAfter())) {
                                aTempListOder.add(mOrder);
                            }
                            aTempListOder.add(aListOder.get(a));
                        }
                        aListOder = (ArrayList<OrderItemModel>) aTempListOder.clone();
                    }

                }

                refreshAssignedList();
                break;
            case "UNASSIGN":
                for (int a = 0; a < aListOder.size(); a++)
                    if (aListOder.get(a).getId().equals(mOrder.getId())) {
                        aListOder.remove(a);
                        break;
                    }

                refreshAssignedList();
                break;
            case "REPRIORITIZE":
                if (aListOder.isEmpty()) {
                    aListOder.add(mOrder);
                } else {
                    if (mOrder.getAfter().isEmpty())
                        aListOder.add(mOrder);
                    else {
                        ArrayList<OrderItemModel> aTempListOder = new ArrayList<>();

                        for (int a = 0; a < aListOder.size(); a++) {
                            if (aListOder.get(a).getId().equals(mOrder.getAfter()))
                                aTempListOder.add(mOrder);

                            if (!aListOder.get(a).getId().equals(mOrder.getId()))
                                aTempListOder.add(aListOder.get(a));
                        }
                        aListOder = (ArrayList<OrderItemModel>) aTempListOder.clone();
                    }

                }

                refreshAssignedList();
                break;
            default:
                DebugUtils.logDebug(TAG, "OrderType: Unhandled " + mOrder.getOrderType());
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_menu_item_log_out:
                DialogMaterial mDialog = new DialogMaterial(ListOrderAssignedActivity.this, getString(R.string.dialog_title_log_out), getString(R.string.dialog_msg_log_out));
                mDialog.addAcceptButton("Yes");
                mDialog.setOnAcceptButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        webSocketService.disconnectWebSocket();
                        BentoDriveUtil.disconnectUser(ListOrderAssignedActivity.this, true);
                    }
                });
                mDialog.addCancelButton("No", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        webSocketService.disconnectWebSocket();
                        BentoDriveUtil.disconnectUser(ListOrderAssignedActivity.this, false);
                    }
                });
                mDialog.show();
                break;
            default:
                DebugUtils.logError(TAG, "OnClick(): " + v.getId());
                break;
        }
    }


    @Override
    public void OnItemClickListener(int iPosition) {
        Intent mIntentOrder = new Intent(ListOrderAssignedActivity.this, OrderAssignedActivity.class);
        // mIntentOrder.putExtra(OrderItemModel.TAG, getListAdapter().aListOrder.get(iPosition));
        mIntentOrder.putExtra(OrderAssignedActivity.TAG_ORDER_ID, getListAdapter().aListOrder.get(iPosition).getId());
        startActivity(mIntentOrder);
        // startActivityForResult(mIntentOrder, TAG_ID);
    }

   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAG_ID) {
            if (resultCode == RESULT_OK) {
                OrderItemModel mRetrieveItem = data.getParcelableExtra(OrderItemModel.TAG);
                DebugUtils.logDebug(TAG, "New order Id: " + mRetrieveItem.getId() + " Status: " + mRetrieveItem.getStatus());

                if (!aListOder.isEmpty()) {
                    if (mRetrieveItem.getStatus().contains("COMPLETED"))
                        aListOder.remove(0);
                    else
                        aListOder.set(0, mRetrieveItem);
                }

                refreshAssignedList();
            }
        }
    }*/

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

    @Override
    protected void onResume() {
        super.onResume();
        aListOder = OrderItemDAO.getAllTask();

        if (!aListOder.isEmpty())
            refreshAssignedList();
    }


    private ProgressDialog getLoaderDialog() {
        if (mLoaderDialog == null)
            mLoaderDialog = new ProgressDialog(ListOrderAssignedActivity.this, "Downloading...");
        return mLoaderDialog;
    }

    private ImageView getMenuItemLogOut() {
        if (imgMenuItemLogOut == null)
            imgMenuItemLogOut = (ImageView) findViewById(R.id.img_menu_item_log_out);
        return imgMenuItemLogOut;
    }

    private TextView getTxtEmptyView() {
        if (txtEmptyView == null)
            txtEmptyView = (TextView) findViewById(R.id.txt_empty_list);
        return txtEmptyView;
    }


    private RecyclerView getListOrder() {
        if (mRecyclerView == null) {
            mRecyclerView = (RecyclerView) findViewById(R.id.list_assigned_orders);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(ListOrderAssignedActivity.this));
            //   mRecyclerView.addItemDecoration(new OrderDividerItemDecoration(ListOrderAssignedActivity.this));
        }
        return mRecyclerView;
    }

    private OrderListAdapter getListAdapter() {
        if (mAdapter == null)
            mAdapter = new OrderListAdapter(ListOrderAssignedActivity.this, ListOrderAssignedActivity.this);
        return mAdapter;
    }


}
