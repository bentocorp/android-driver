package com.bentonow.drive.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by joseguadalupetorresfuentes on 15/11/15.
 */
public class OrderItemModel implements Parcelable {

    public static final String TAG = "OrderItemModel";

    private String orderType = "";
    private String id = "";
    private String name = "";
    private String phone = "";
    private Address address = new Address();
    private String item = "";
    private int key;
    private String driverId = "";
    private String status = "";
    private String after = "";

    public OrderItemModel() {
    }

    public OrderItemModel(Parcel parcel) {
        orderType = parcel.readString();
        id = parcel.readString();
        name = parcel.readString();
        phone = parcel.readString();
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
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(phone);
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


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
