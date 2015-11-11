package com.bentonow.drive.model;

/**
 * Created by Jose Torres on 11/11/15.
 */
public class SocketResponseModel {
    public static final String TAG = "SocketResponseModel";

    private String msg;
    private int code;
    private String ret;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getRet() {
        return ret;
    }

    public void setRet(String ret) {
        this.ret = ret;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
