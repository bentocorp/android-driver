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
import com.bentonow.drive.model.OrderItemModel;
import com.bentonow.drive.parse.jackson.BentoOrderJsonParser;
import com.bentonow.drive.socket.WebSocketService;
import com.bentonow.drive.util.BentoDriveUtil;
import com.bentonow.drive.util.DebugUtils;
import com.bentonow.drive.util.NotificationUtil;
import com.bentonow.drive.util.SharedPreferencesUtil;
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

    private void refreshAssignedList(boolean bRefresh) {
        if (bRefresh) {
            getListAdapter().aListOrder.clear();

            if (!aListOder.isEmpty())
                getListAdapter().aListOrder.add(aListOder.get(0));

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getTxtEmptyView().setVisibility(getListAdapter().aListOrder.isEmpty() ? View.VISIBLE : View.GONE);
                    getListOrder().setVisibility(getListAdapter().aListOrder.isEmpty() ? View.GONE : View.VISIBLE);
                    getListAdapter().notifyDataSetChanged();
                }
            });

        }

        DebugUtils.logDebug(TAG, "Total Orders: " + aListOder.size());


    }

    private void hideLoader() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mIsFirstTime) {
                    getLoaderDialog().dismiss();
                    mIsFirstTime = false;
                }
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

                webSocketService.saveListTask(aListOder);

                hideLoader();

                refreshAssignedList(true);
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                try {
                    DebugUtils.logDebug(TAG, "Order: " + responseString);
                    aListOder = BentoOrderJsonParser.parseBentoListOrder(responseString);

                    webSocketService.saveListTask(aListOder);

                } catch (Exception ex) {
                    DebugUtils.logError(TAG, ex);
                }

                hideLoader();

                refreshAssignedList(true);
            }

        });

    }

    private class WebSocketServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            DebugUtils.logDebug(TAG, "Successfully bounded to " + name.getClassName());
            WebSocketService.WebSocketServiceBinder webSocketServiceBinder = (WebSocketService.WebSocketServiceBinder) binder;
            webSocketService = webSocketServiceBinder.getService();
            webSocketService.onNodeEventListener(ListOrderAssignedActivity.this);
            aListOder = webSocketService.getListTask();
            mBound = true;

            if (mIsFirstTime)
                getAssignedOrders();
            else
                refreshAssignedList(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            DebugUtils.logDebug(TAG, "Disconnected from service " + name);
            mBound = false;
        }

    }


    @Override
    public void onPush(OrderItemModel mOrder) {
    }

    @Override
    public void onAssign(List<OrderItemModel> mNewList, boolean bRefresh) {
        aListOder = mNewList;

        refreshAssignedList(bRefresh);
    }

    @Override
    public void onUnassign(List<OrderItemModel> mNewList, boolean bRefresh) {
        aListOder = mNewList;

        refreshAssignedList(bRefresh);
    }

    @Override
    public void onReprioritize(List<OrderItemModel> mNewList, boolean bRefresh) {
        aListOder = mNewList;

        refreshAssignedList(bRefresh);
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
        if (iPosition == 0) {
            Intent mIntentOrder = new Intent(ListOrderAssignedActivity.this, OrderAssignedActivity.class);
            mIntentOrder.putExtra(OrderAssignedActivity.TAG_ORDER_ID, getListAdapter().aListOrder.get(0).getId());
            startActivity(mIntentOrder);
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
        DebugUtils.logDebug(TAG, "OnDestroy()");
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!BentoDriveUtil.isUserConnected(ListOrderAssignedActivity.this)) {
            BentoDriveUtil.disconnectUser(ListOrderAssignedActivity.this, SharedPreferencesUtil.getBooleanPreference((ListOrderAssignedActivity.this), SharedPreferencesUtil.USE_SAVED_SETTINGS));
            finish();
        } else {
            if (webSocketService != null) {
                aListOder = webSocketService.getListTask();
                webSocketService.onNodeEventListener(ListOrderAssignedActivity.this);
            }

            NotificationUtil.cancelAllNotification(ListOrderAssignedActivity.this);

            refreshAssignedList(true);
        }

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
