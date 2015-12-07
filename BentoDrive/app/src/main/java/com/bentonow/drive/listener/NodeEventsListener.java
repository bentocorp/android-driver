package com.bentonow.drive.listener;

import com.bentonow.drive.model.OrderItemModel;

import java.util.List;

/**
 * Created by Jose Torres on 11/10/15.
 */
public interface NodeEventsListener {
    void onPush(OrderItemModel mOrderItem);

    void onAssign(List<OrderItemModel> mNewList, boolean bRefresh);

    void onUnassign(List<OrderItemModel> mNewList, boolean bRefresh);

    void onReprioritize(List<OrderItemModel> mNewList, boolean bRefresh);
}
