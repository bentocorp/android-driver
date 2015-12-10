package com.bentonow.drive.socket;

import android.os.AsyncTask;

import com.bentonow.drive.listener.WebSocketEventListener;

/**
 * Created by Jose Torres on 11/11/15.
 */
public class LogInAsyncTask extends AsyncTask<Void, Void, Void> {

    private WebSocketService webSocketService = null;
    private String sUsername;
    private String sPassword;
    private WebSocketEventListener mListener;

    public LogInAsyncTask(WebSocketService webSocketService, String sUsername, String sPassword, WebSocketEventListener mListener) {
        this.webSocketService = webSocketService;
        this.sUsername = sUsername;
        this.sPassword = sPassword;
        this.mListener = mListener;
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        webSocketService.connectWebSocket(sUsername, sPassword);

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
    }


}