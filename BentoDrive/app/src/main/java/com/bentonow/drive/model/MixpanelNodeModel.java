package com.bentonow.drive.model;

/**
 * Created by Kokusho on 15/02/16.
 */
public class MixpanelNodeModel {
    private boolean bIsRetrying;
    private boolean bIsReconnecting;
    private boolean bIsWebServiceNull;
    private boolean bIsUserLogged;
    private boolean bIsListenerEnable;
    private boolean bIsTransportError;
    private boolean bIsTransportClosed;
    private long seconds;
    private String sTransportError = "";
    private String sTransportClosed = "";

    public boolean isbIsRetrying() {
        return bIsRetrying;
    }

    public void setbIsRetrying(boolean bIsRetrying) {
        this.bIsRetrying = bIsRetrying;
    }

    public boolean isbIsReconnecting() {
        return bIsReconnecting;
    }

    public void setbIsReconnecting(boolean bIsReconnecting) {
        this.bIsReconnecting = bIsReconnecting;
    }

    public boolean isbIsWebServiceNull() {
        return bIsWebServiceNull;
    }

    public void setbIsWebServiceNull(boolean bIsWebServiceNull) {
        this.bIsWebServiceNull = bIsWebServiceNull;
    }

    public boolean isbIsUserLogged() {
        return bIsUserLogged;
    }

    public void setbIsUserLogged(boolean bIsUserLogged) {
        this.bIsUserLogged = bIsUserLogged;
    }

    public boolean isbIsListenerEnable() {
        return bIsListenerEnable;
    }

    public void setbIsListenerEnable(boolean bIsListenerEnable) {
        this.bIsListenerEnable = bIsListenerEnable;
    }

    public boolean isbIsTransportError() {
        return bIsTransportError;
    }

    public void setbIsTransportError(boolean bIsTransportError) {
        this.bIsTransportError = bIsTransportError;
    }

    public boolean isbIsTransportClosed() {
        return bIsTransportClosed;
    }

    public void setbIsTransportClosed(boolean bIsTransportClosed) {
        this.bIsTransportClosed = bIsTransportClosed;
    }

    public String getsTransportError() {
        return sTransportError;
    }

    public void setsTransportError(String sTransportError) {
        this.sTransportError = sTransportError;
    }

    public String getsTransportClosed() {
        return sTransportClosed;
    }

    public void setsTransportClosed(String sTransportClosed) {
        this.sTransportClosed = sTransportClosed;
    }

    public long getSeconds() {
        return seconds;
    }

    public void setSeconds(long seconds) {
        this.seconds = seconds;
    }
}
