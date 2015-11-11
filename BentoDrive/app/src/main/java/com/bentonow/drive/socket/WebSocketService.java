package com.bentonow.drive.socket;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;

import com.bentonow.drive.listener.UpdateLocationListener;
import com.bentonow.drive.listener.WebSocketEventListener;
import com.bentonow.drive.util.DebugUtils;
import com.bentonow.drive.util.GoogleLocationUtil;
import com.bentonow.drive.web.BentoDriveAPI;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Ack;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.bentocorp.api.APIResponse;
import org.bentocorp.api.Authenticate;
import org.bentocorp.api.ws.Push;

public class WebSocketService extends Service implements UpdateLocationListener {

    public static final String TAG = "WebSocketService";


    private final WebSocketServiceBinder binder = new WebSocketServiceBinder();
    private ObjectMapper mapper = new ObjectMapper();
    private Socket mSocket = null;

    private boolean connecting = false;
    private boolean disconnectingPurposefully = false;
    private String token;


    @Override
    public void onCreate() {
        DebugUtils.logDebug(TAG, "creating new WebSocketService");
    }


    public void connectWebSocket(String username, String password, WebSocketEventListener mListener) {
        if (connecting) {
            DebugUtils.logDebug(TAG, "Connection in progress");
        } else if (mSocket != null && mSocket.connected()) {
            DebugUtils.logDebug(TAG, "Already connected");
        } else {
            try {
                connecting = true;
                //cert(); // XXX - ssl connection on android doesn't work?
                IO.Options opts = new IO.Options();
                opts.forceNew = true;
                //opts.timeout = 5000;
                mSocket = IO.socket(BentoDriveAPI.NODE_URL, opts);
                socketAuthenticate(username, password, mListener);
                mSocket.connect();
            } catch (Exception e) {
                DebugUtils.logError(TAG, "connectWebSocket: " + e.toString());
                mListener.onConnectionError(e.getMessage());
            }
        }
    }


    public void socketAuthenticate(final String sUsername, final String sPassword, final WebSocketEventListener mListener) {
        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object[] args) {
                mListener.onSuccessfulConnection();
                try {
                    String sPath = BentoDriveAPI.getAuthenticationUrl(sUsername, sPassword);
                    DebugUtils.logDebug(TAG, "Connecting: " + sPath);
                    //socket.emit("get", URLEncoder.encode(path, "UTF-8"), new Ack() {
                    mSocket.emit("get", sPath, new Ack() {
                        @Override
                        public void call(Object[] args) {
                            try {
                                APIResponse<Authenticate> res = mapper.readValue(args[0].toString(), new TypeReference<APIResponse<Authenticate>>() {
                                });
                                if (res.code != 0) {
                                    mListener.onAuthenticationFailure(res.msg);
                                    DebugUtils.logError(TAG, "socketAuthenticate: " + res.msg);
                                    mSocket.disconnect();
                                } else {
                                    token = res.ret.token;
                                    GoogleLocationUtil.startLocationUpdates(WebSocketService.this);
                                    mListener.onAuthenticationSuccess(token);
                                }
                            } catch (Exception e) {
                                mListener.onAuthenticationFailure(e.getMessage());
                                DebugUtils.logError(TAG, "socketAuthenticate: " + e.toString());
                                mSocket.disconnect();
                            }
                        }
                    });
                } catch (Exception e) {
                    //} catch (UnsupportedEncodingException e) {
                    mListener.onAuthenticationFailure(e.getMessage());
                    DebugUtils.logError(TAG, "disconnecting-onemittig");
                    mSocket.disconnect();
                }
            }
        });
        mSocket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object[] args) {
                mListener.onConnectionError(args[0].toString());
                DebugUtils.logError(TAG, "connection-error");
                // occurs if the server goes down to automatically reconnect, do not disconnect here
            }
        });
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
            @Override
            public void call(Object[] args) {
                mListener.onConnectionError("Error - WebSocket connection timeout");
                DebugUtils.logError(TAG, "disconnecting-connect-timeout");
                mSocket.disconnect();
            }
        });
        mSocket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object[] args) {
                connecting = false;
                mListener.onDisconnect(disconnectingPurposefully);

                //disconnectingPurposefully = false;
            }
        });
        mSocket.on("push", new Emitter.Listener() {
            @Override
            public void call(Object[] args) {
                try {
                    Push push = mapper.readValue(args[0].toString(), Push.class);
                    mListener.onPush(push);
                } catch (Exception e) {
                    DebugUtils.logError(TAG, "push: " + e.toString());
                }
            }
        });
    }

    public void disconnectWebSocket() {
        disconnectingPurposefully = true;
        DebugUtils.logDebug(TAG, "disconnecting");
        mSocket.disconnect();
    }


    public boolean isConnectedUser() {
        if (mSocket != null && mSocket.connected()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onLocationUpdated(Location mLocation) {
        if (isConnectedUser()) {
            mSocket.emit("get", BentoDriveAPI.getSendLocationUrl(mLocation.getLatitude(), mLocation.getLongitude()), new Ack() {
                @Override
                public void call(Object[] args) {
                    try {
                        APIResponse<Authenticate> res = mapper.readValue(args[0].toString(), new TypeReference<APIResponse<Authenticate>>() {
                        });
                        if (res.code != 0) {
                            DebugUtils.logError(TAG, "onLocationUpdated: " + res.msg);
                        } else {
                            token = res.ret.token;
                            DebugUtils.logDebug(TAG, "onLocationUpdated: " + res.ret);
                            GoogleLocationUtil.startLocationUpdates(WebSocketService.this);
                        }
                    } catch (Exception e) {
                        DebugUtils.logError(TAG, "onLocationUpdated: " + e.toString());
                    }
                }
            });
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        DebugUtils.logDebug(TAG, "destroying WebSocketService");
    }

    // Called when all clients have disconnected
    @Override
    public boolean onUnbind(Intent intent) {
        if (mSocket == null || !mSocket.connected()) {
            stopSelf();
        }
        return true;
    }

    public class WebSocketServiceBinder extends Binder {
        public WebSocketService getService() {
            return WebSocketService.this;
        }
    }


}
