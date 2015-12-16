package com.bentonow.drive.model;

/**
 * Created by Jose Torres on 11/24/15.
 */
public class VersionModel {
    private String min_version = "";
    private String min_version_url = "";
    private int iCode = 0;
    private String sMessage = "";

    public String getMin_version() {
        return min_version;
    }

    public void setMin_version(String min_version) {
        this.min_version = min_version;
    }

    public String getMin_version_url() {
        return min_version_url;
    }

    public void setMin_version_url(String min_version_url) {
        this.min_version_url = min_version_url;
    }

    public int getiCode() {
        return iCode;
    }

    public void setiCode(int iCode) {
        this.iCode = iCode;
    }

    public String getsMessage() {
        return sMessage;
    }

    public void setsMessage(String sMessage) {
        this.sMessage = sMessage;
    }
}
