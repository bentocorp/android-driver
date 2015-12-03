package com.bentonow.drive.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.orm.SugarRecord;

/**
 * Created by joseguadalupetorresfuentes on 15/11/15.
 */
public class OrderItemModel extends SugarRecord implements Parcelable {

    public static final String TAG = "OrderItemModel";

    private String orderType = "";
    private String order_id = "";
    private String name = "";
    private String phone = "";
    private long addressId;
    public Address address = new Address();
    private String item = "";
    private int key;
    private String driverId = "";
    private String status = "";
    private String after = "";

    public OrderItemModel() {
    }

    public OrderItemModel(String orderType, String order_id, String name, String phone, long addressId, Address address, String item, int key, String driverId, String status, String after) {
        this.orderType = orderType;
        this.order_id = order_id;
        this.name = name;
        this.phone = phone;
        this.addressId = addressId;
        this.address = address;
        this.item = item;
        this.key = key;
        this.driverId = driverId;
        this.status = status;
        this.after = after;
    }

    public OrderItemModel(Parcel parcel) {
        orderType = parcel.readString();
        order_id = parcel.readString();
        name = parcel.readString();
        phone = parcel.readString();
        addressId = parcel.readLong();
        address = parcel.readParcelable(Address.class.getClassLoader());
        item = parcel.readString();
        key = parcel.readInt();
        driverId = parcel.readString();
        status = parcel.readString();
        after = parcel.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(orderType);
        dest.writeString(order_id);
        dest.writeString(name);
        dest.writeString(phone);
        dest.writeLong(addressId);
        dest.writeParcelable(address, flags);
        dest.writeString(item);
        dest.writeInt(key);
        dest.writeString(driverId);
        dest.writeString(status);
        dest.writeString(after);
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public static final Creator<OrderItemModel> CREATOR = new Creator<OrderItemModel>() {

        @Override
        public OrderItemModel[] newArray(int size) {
            return new OrderItemModel[size];
        }

        @Override
        public OrderItemModel createFromParcel(Parcel source) {
            return new OrderItemModel(source);
        }
    };


    public String getOrderId() {
        return order_id;
    }

    public void setOrderId(String order_id) {
        this.order_id = order_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    public long getAddressId() {
        return addressId;
    }

    public void setAddressId(long addressId) {
        this.addressId = addressId;
    }
}
