package com.bentonow.drive.socket;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;

import com.bentonow.drive.Application;
import com.bentonow.drive.listener.NodeEventsListener;
import com.bentonow.drive.listener.UpdateLocationListener;
import com.bentonow.drive.listener.WebSocketEventListener;
import com.bentonow.drive.model.OrderItemModel;
import com.bentonow.drive.model.SocketResponseModel;
import com.bentonow.drive.parse.jackson.BentoOrderJsonParser;
import com.bentonow.drive.util.BentoDriveUtil;
import com.bentonow.drive.util.DebugUtils;
import com.bentonow.drive.util.GoogleLocationUtil;
import com.bentonow.drive.util.SharedPreferencesUtil;
import com.bentonow.drive.web.BentoDriveAPI;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.bentocorp.api.APIResponse;
import org.bentocorp.api.Authenticate;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class WebSocketService extends Service implements UpdateLocationListener {

    public static final String TAG = "WebSocketService";


    private final WebSocketServiceBinder binder = new WebSocketServiceBinder();
    private ObjectMapper mapper = new ObjectMapper();
    private Socket mSocket = null;
    private boolean connecting = false;
    private boolean disconnectingPurposefully = false;


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

                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAllCerts, new SecureRandom());
                IO.setDefaultSSLContext(sc);
                IO.setDefaultHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });

                IO.Options opts = new IO.Options();

                opts.port = 8081;
                opts.forceNew = true;
                opts.secure = true;
                opts.sslContext = sc;
                opts.hostnameVerifier = new RelaxedHostNameVerifier();

                //opts.timeout = 5000;
                mSocket = IO.socket(BentoDriveAPI.getNodeUrl(WebSocketService.this), opts);
                socketAuthenticate(username, password, mListener);
                mSocket.connect();
            } catch (Exception e) {
                DebugUtils.logError(TAG, "connectWebSocket: " + e.toString());
                if (mListener != null)
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
                                    final String sToken = res.ret.token;
                                    DebugUtils.logDebug(TAG, "Token: " + sToken);
                                    SharedPreferencesUtil.setAppPreference(WebSocketService.this, SharedPreferencesUtil.TOKEN, sToken);
                                    mListener.onAuthenticationSuccess(sToken);
                                    Application.getInstance().handlerPost(new Runnable() {
                                        @Override
                                        public void run() {
                                            GoogleLocationUtil.startLocationUpdates(WebSocketService.this, WebSocketService.this);
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                mListener.onAuthenticationFailure(e.getMessage());
                                DebugUtils.logError(TAG, "socketAuthenticate: " + e.toString());
                                mSocket.disconnect();
                            }
                        }
                    });
                } catch (Exception e) {
                    mListener.onConnectionError(e.getMessage());
                    DebugUtils.logError(TAG, "disconnecting-onemittig");
                    mSocket.disconnect();
                }
            }
        });
        mSocket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object[] args) {
                mListener.onConnectionError(args[0].toString());
                DebugUtils.logError(TAG, "connection-error" + args[0].toString());
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
    }

    public void onNodeEventListener(final NodeEventsListener mListener) {
        if (mSocket != null && mListener != null) {
            DebugUtils.logDebug(TAG, "Push: Subscribed");
            mSocket.on("push", new Emitter.Listener() {
                @Override
                public void call(Object[] args) {
                    try {
                        DebugUtils.logDebug(TAG, "Push: " + args[0].toString());
                        OrderItemModel mOrder = BentoOrderJsonParser.parseBentoOrderItem(args[0].toString());
                        //  Push push = mapper.readValue(args[0].toString(), Push.class);
                        mListener.onPush(mOrder);
                    } catch (Exception e) {
                        DebugUtils.logError(TAG, "Push: " + e.toString());
                    }
                }
            });

            if (BentoDriveUtil.bIsKokushoTesting) {
                DebugUtils.logDebug(TAG, "Pong: Subscribed");
                mSocket.on("pong", new Emitter.Listener() {
                    @Override
                    public void call(Object[] args) {
                        try {
                            DebugUtils.logDebug(TAG, "Pong: " + args[0].toString());
                        } catch (Exception e) {
                            DebugUtils.logError(TAG, "Pong: " + e.toString());
                        }
                    }
                });
            }
        }
    }


    public void disconnectWebSocket() {
        disconnectingPurposefully = true;
        if (mSocket != null) {
            DebugUtils.logDebug(TAG, "disconnecting");
            mSocket.disconnect();
        }
        GoogleLocationUtil.stopLocationUpdates(WebSocketService.this);
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
                        SocketResponseModel mResponse = new ObjectMapper().readValue(args[0].toString(), SocketResponseModel.class);
                        if (mResponse.getCode() != 0) {
                            DebugUtils.logError(TAG, "onLocationUpdated: " + mResponse.getMsg());
                        } else {
                            String sResponse = mResponse.getRet();
                            DebugUtils.logDebug(TAG, "onLocationUpdated: " + sResponse);
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
        disconnectWebSocket();
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

    private TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[]{};
        }

        public void checkClientTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
        }
    }};

    public static class RelaxedHostNameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
}
