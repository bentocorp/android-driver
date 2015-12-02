package com.bentonow.drive.listener;

import com.bentonow.drive.model.OrderItemModel;

/**
 * Created by Jose Torres on 11/10/15.
 */
public interface NodeEventsListener {
    void onPush(OrderItemModel mOrderItem);
}
