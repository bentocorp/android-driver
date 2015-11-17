package com.bentonow.drive.model;

/**
 * Created by joseguadalupetorresfuentes on 16/11/15.
 */
public class DishItemModel {

    private int id;
    private String name = "";
    private String type = "";
    private String label = "";

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
