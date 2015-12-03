package com.bentonow.drive.model.sugar;

import com.bentonow.drive.model.Address;
import com.bentonow.drive.model.OrderItemModel;
import com.bentonow.drive.util.DebugUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joseguadalupetorresfuentes on 15/11/15.
 */
public class OrderItemDAO {

    public static final String TAG = "OrderItemDAO";


    public static List<OrderItemModel> getAllTask() {
        List<OrderItemModel> aListTask = new ArrayList<>();

        try {
            aListTask = OrderItemModel.listAll(OrderItemModel.class);
            for (int a = 0; a < aListTask.size(); a++)
                aListTask.get(a).setAddress(Address.findById(Address.class, aListTask.get(a).getAddressId()));
        } catch (Exception ex) {
            DebugUtils.logError(TAG, ex);
        }

        if (aListTask == null)
            aListTask = new ArrayList<>();

        return aListTask;
    }

    public static OrderItemModel getOrderById(long id) {
        OrderItemModel mOrder = OrderItemModel.findById(OrderItemModel.class, id);
        mOrder.setAddress(Address.findById(Address.class, mOrder.getAddressId()));
        return mOrder;
    }

    public static void deleteAll() {
        try {
            OrderItemModel.deleteAll(OrderItemModel.class);
            Address.deleteAll(Address.class);
        } catch (Exception ex) {
            DebugUtils.logError(TAG, ex);
        }
    }

    public static void update(OrderItemModel mOrder) {
        try {
            mOrder.setAddressId(mOrder.getAddress().save());
            mOrder.save();
        } catch (Exception ex) {
            DebugUtils.logError(TAG, ex);
        }
    }

    public static void save(OrderItemModel mOrder) {
        try {
            mOrder.setId(null);
            mOrder.getAddress().setId(null);
            mOrder.setAddressId(mOrder.getAddress().save());
            mOrder.save();
        } catch (Exception ex) {
            DebugUtils.logError(TAG, ex);
        }
    }

    public static void save(List<OrderItemModel> aListOrder) {
        try {
            for (OrderItemModel mOrder : aListOrder)
                save(mOrder);
        } catch (Exception ex) {
            DebugUtils.logError(TAG, ex);
        }
    }

    public static void saveAll(List<OrderItemModel> aListOrder) {
        try {
            deleteAll();
            for (OrderItemModel mOrder : aListOrder)
                save(mOrder);
        } catch (Exception ex) {
            DebugUtils.logError(TAG, ex);
        }
    }

    public static void delete(OrderItemModel mOrder) {
        try {
            mOrder.getAddress().delete();
            mOrder.delete();
        } catch (Exception ex) {
            DebugUtils.logError(TAG, ex);
        }
    }
}
