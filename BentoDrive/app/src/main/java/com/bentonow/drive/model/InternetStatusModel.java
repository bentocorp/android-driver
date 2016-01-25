package com.bentonow.drive.model;

public class InternetStatusModel {

    private int strengthAmplitude;
    private int hour;
    private int minute;
    private int second;
    private String model = "";
    private String operator = "";
    private boolean isConnected;
    private String typeConnection = "";
    private String networkOperation = "";
    private double lat;
    private double lon;


    public int getStrengthAmplitude() {
        return strengthAmplitude;
    }

    public void setStrengthAmplitude(int strengthAmplitude) {
        this.strengthAmplitude = strengthAmplitude;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public String getTypeConnection() {
        return typeConnection;
    }

    public void setTypeConnection(String typeConnection) {
        this.typeConnection = typeConnection;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getNetworkOperation() {
        return networkOperation;
    }

    public void setNetworkOperation(String networkOperation) {
        this.networkOperation = networkOperation;
    }
}
