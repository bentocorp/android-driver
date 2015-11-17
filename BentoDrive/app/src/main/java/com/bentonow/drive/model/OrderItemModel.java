package com.bentonow.drive.model;

import java.util.ArrayList;

/**
 * Created by joseguadalupetorresfuentes on 15/11/15.
 */
public class OrderItemModel {

    private int orderType;
    private String id = "";
    private String name = "";
    private String phone = "";
    private Address address = new Address();
    private ArrayList<MenuItemModel> items = new ArrayList<>();
    private String task = "";
    private int key;
    private String orderString = "";
    private int driverId;
    private String status = "";

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

    public ArrayList<MenuItemModel> getItems() {
        return items;
    }

    public void setItems(ArrayList<MenuItemModel> items) {
        this.items = items;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getOrderString() {
        return orderString;
    }

    public void setOrderString(String orderString) {
        this.orderString = orderString;
    }

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getOrderType() {
        return orderType;
    }

    public void setOrderType(int orderType) {
        this.orderType = orderType;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }
}
