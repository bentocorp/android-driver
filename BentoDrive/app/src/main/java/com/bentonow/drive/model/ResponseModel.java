package com.bentonow.drive.model;

/**
 * Created by Jose Torres on 11/24/15.
 */
public class ResponseModel {
    private int code;
    private String msg = "";
    private String ret = "";

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getRet() {
        return ret;
    }

    public void setRet(String ret) {
        this.ret = ret;
    }
}
