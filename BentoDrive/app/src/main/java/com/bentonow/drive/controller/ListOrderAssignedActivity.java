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

import com.bentonow.drive.Application;
import com.bentonow.drive.R;
import com.bentonow.drive.controller.adapter.OrderListAdapter;
import com.bentonow.drive.listener.ListenerWebRequest;
import com.bentonow.drive.listener.NodeEventsListener;
import com.bentonow.drive.listener.RecyclerListListener;
import com.bentonow.drive.model.OrderItemModel;
import com.bentonow.drive.socket.WebSocketService;
import com.bentonow.drive.util.BentoDriveUtil;
import com.bentonow.drive.util.DebugUtils;
import com.bentonow.drive.web.request.RequestGetAssignedOrders;
import com.bentonow.drive.widget.OrderDividerItemDecoration;

import org.bentocorp.api.ws.Push;

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

    private WebSocketService webSocketService = null;
    private ServiceConnection mConnection = new WebSocketServiceConnection();

    private ArrayList<OrderItemModel> aListOder;

    private boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_bento);

        getMenuItemLogOut().setOnClickListener(this);

        getListOrder().setAdapter(getListAdapter());


    }

    private void getAssignedOrders() {
        Application.getInstance().webRequest(new RequestGetAssignedOrders(new ListenerWebRequest() {
            @Override
            public void onError(String sError) {
                super.onError(sError);
            }

            @Override
            public void onResponse(Object oResponse) {
                aListOder = (ArrayList<OrderItemModel>) oResponse;
                getListAdapter().aListOrder.clear();

                if (!aListOder.isEmpty())
                    getListAdapter().aListOrder.add(aListOder.get(0));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getListAdapter().notifyDataSetChanged();
                        getTxtEmptyView().setVisibility(getListAdapter().aListOrder.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });

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
            webSocketService.onNodeEventListener(ListOrderAssignedActivity.this);
            mBound = true;
            getAssignedOrders();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            DebugUtils.logDebug(TAG, "Disconnected from service " + name);
            mBound = true;
        }

    }


    @Override
    public void onPush(Push mPush) {
        DebugUtils.logDebug(TAG, "Push: " + mPush.body.toString());
        getAssignedOrders();
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
            mRecyclerView.addItemDecoration(new OrderDividerItemDecoration(ListOrderAssignedActivity.this));
        }
        return mRecyclerView;
    }

    private OrderListAdapter getListAdapter() {
        if (mAdapter == null)
            mAdapter = new OrderListAdapter(ListOrderAssignedActivity.this, ListOrderAssignedActivity.this);
        return mAdapter;
    }


}
