package com.bentonow.drive.listener;

import org.bentocorp.api.ws.Push;

/**
 * Created by Jose Torres on 11/10/15.
 */
public abstract class WebSocketEventListener {

    public void onSuccessfulConnection() {
    }

    public void onConnectionError(String reason) {
    }

    public void onAuthenticationSuccess(String token) {
    }

    public void onAuthenticationFailure(String reason) {
    }

    public void onDisconnect(boolean disconnectingPurposefully) {
    }

    public void onPush(Push push) {
    }
}