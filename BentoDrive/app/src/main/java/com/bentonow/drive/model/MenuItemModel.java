package com.bentonow.drive.model;

import java.util.ArrayList;

/**
 * Created by joseguadalupetorresfuentes on 16/11/15.
 */
public class MenuItemModel {

    private ArrayList<DishItemModel> items = new ArrayList<>();

    public ArrayList<DishItemModel> getItems() {
        return items;
    }

    public void setItems(ArrayList<DishItemModel> items) {
        this.items = items;
    }
}
