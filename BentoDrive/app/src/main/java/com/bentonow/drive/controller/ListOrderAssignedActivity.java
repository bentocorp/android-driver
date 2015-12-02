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
import com.bentonow.drive.parse.jackson.BentoOrderJsonParser;
import com.bentonow.drive.socket.WebSocketService;
import com.bentonow.drive.util.BentoDriveUtil;
import com.bentonow.drive.util.DebugUtils;
import com.bentonow.drive.util.SharedPreferencesUtil;
import com.bentonow.drive.util.WidgetsUtils;
import com.bentonow.drive.web.BentoRestClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

import java.util.ArrayList;

/**
 * Created by Jose Torres on 11/10/15.
 */
public class ListOrderAssignedActivity extends MainActivity implements View.OnClickListener, RecyclerListListener, NodeEventsListener {

    public static final String TAG = "ListOrderAssignedActivity";

    private ImageView imgMenuItemLogOut;
    private TextView txtEmptyView;

    private RecyclerView mRecyclerView;
    private OrderListAdapter mAdapter;

    private ProgressDialog mLoaderDialog;

    private WebSocketService webSocketService = null;
    private ServiceConnection mConnection = new WebSocketServiceConnection();

    private ArrayList<OrderItemModel> aListOder = new ArrayList<>();

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

        for (int a = 0; a < aListOder.size(); a++) {
            //if (!aListOder.get(a).getStatus().contains("REJECTED"))
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
        getAssignedOrders();
    }

    private void logInDrive() {
        if (!webSocketService.isConnectedUser()) {
            DebugUtils.logDebug(TAG, "Attempting to connect to node");

            webSocketService.connectWebSocket(SharedPreferencesUtil.getStringPreference(ListOrderAssignedActivity.this, SharedPreferencesUtil.USER_NAME),
                    SharedPreferencesUtil.getStringPreference(ListOrderAssignedActivity.this, SharedPreferencesUtil.PASSWORD), new WebSocketEventListener() {
                        @Override
                        public void onAuthenticationSuccess(String sToken) {
                            setNodeListener();
                        }

                        @Override
                        public void onAuthenticationFailure(String sReason) {
                            WidgetsUtils.createShortToast("There was a problem: " + sReason);
                            BentoDriveUtil.disconnectUser(ListOrderAssignedActivity.this);
                        }
                    });

        } else {
            setNodeListener();
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
                webSocketService.disconnectWebSocket();
                BentoDriveUtil.disconnectUser(ListOrderAssignedActivity.this);
                break;
            default:
                DebugUtils.logError(TAG, "OnClick(): " + v.getId());
                break;
        }
    }


    @Override
    public void OnItemClickListener(int iPosition) {
        Intent mIntentOrder = new Intent(ListOrderAssignedActivity.this, OrderAssignedActivity.class);
        mIntentOrder.putExtra(OrderItemModel.TAG, getListAdapter().aListOrder.get(iPosition));
        startActivity(mIntentOrder);
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

    @Override
    protected void onResume() {
        super.onResume();


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
