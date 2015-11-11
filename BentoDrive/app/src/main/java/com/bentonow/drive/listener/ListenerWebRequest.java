package com.bentonow.drive.listener;

/**
 * Created by Jose Torres on 11/10/15.
 */
public abstract class ListenerWebRequest {
    public void onError(String sError) {
        onComplete();
    }

    public void onResponse(Object oResponse) {
        onComplete();
    }

    public void onComplete() {
    }
}
