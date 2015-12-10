package com.bentonow.drive.socket;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;

import com.bentonow.drive.Application;
import com.bentonow.drive.listener.UpdateLocationListener;
import com.bentonow.drive.listener.WebSocketEventListener;
import com.bentonow.drive.model.OrderItemModel;
import com.bentonow.drive.model.SocketResponseModel;
import com.bentonow.drive.model.sugar.OrderItemDAO;
import com.bentonow.drive.parse.jackson.BentoOrderJsonParser;
import com.bentonow.drive.util.BentoDriveUtil;
import com.bentonow.drive.util.ConstantUtil;
import com.bentonow.drive.util.DebugUtils;
import com.bentonow.drive.util.GoogleLocationUtil;
import com.bentonow.drive.util.SharedPreferencesUtil;
import com.bentonow.drive.util.WidgetsUtils;
import com.bentonow.drive.web.BentoDriveAPI;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.bentocorp.api.APIResponse;
import org.bentocorp.api.Authenticate;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

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

    private List<OrderItemModel> aListTask;

    private String sUsername = "";
    private String sPassword = "";

    private WebSocketEventListener mSocketListener;

    @Override
    public void onCreate() {
        DebugUtils.logDebug(TAG, "creating new WebSocketService");
        SharedPreferencesUtil.setAppPreference(WebSocketService.this, SharedPreferencesUtil.IS_USER_LOG_IN, false);
        aListTask = new ArrayList<>();
    }

    public void connectWebSocket(String username, String password) {
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

                opts.port = 8443;
                opts.forceNew = true;
                opts.secure = true;
                opts.sslContext = sc;
                opts.hostnameVerifier = new RelaxedHostNameVerifier();
                opts.reconnectionDelay = 500;
                opts.timeout = 5000;

                //opts.timeout = 5000;
                mSocket = IO.socket(BentoDriveAPI.getNodeUrl(WebSocketService.this), opts);
                socketAuthenticate(username, password);
                mSocket.connect();
            } catch (Exception e) {
                DebugUtils.logError(TAG, "connectWebSocket: " + e.toString());
                if (mSocketListener != null)
                    mSocketListener.onConnectionError(e.getMessage());
            }
        }
    }


    public void socketAuthenticate(final String sUser, final String sPass) {
        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object[] args) {
                if (mSocketListener != null)
                    mSocketListener.onSuccessfulConnection();
                try {
                    String sPath = BentoDriveAPI.getAuthenticationUrl(sUser, sPass);
                    DebugUtils.logDebug(TAG, "Connecting: " + sPath);
                    mSocket.emit("get", sPath, new Ack() {
                        @Override
                        public void call(Object[] args) {
                            try {
                                APIResponse<Authenticate> res = mapper.readValue(args[0].toString(), new TypeReference<APIResponse<Authenticate>>() {
                                });
                                if (res.code != 0) {
                                    if (mSocketListener != null)
                                        mSocketListener.onAuthenticationFailure(res.msg);
                                    DebugUtils.logError(TAG, "socketAuthenticate: " + res.msg);
                                    mSocket.disconnect();
                                } else {
                                    final String sToken = res.ret.token;
                                    DebugUtils.logDebug(TAG, "Token: " + sToken);
                                    SharedPreferencesUtil.setAppPreference(WebSocketService.this, SharedPreferencesUtil.TOKEN, sToken);
                                    SharedPreferencesUtil.setAppPreference(WebSocketService.this, SharedPreferencesUtil.IS_USER_LOG_IN, true);

                                    sUsername = sUser;
                                    sPassword = sPass;

                                    if (mSocketListener != null)
                                        mSocketListener.onAuthenticationSuccess(sToken);

                                    Application.getInstance().handlerPost(new Runnable() {
                                        @Override
                                        public void run() {
                                            GoogleLocationUtil.startLocationUpdates(WebSocketService.this, WebSocketService.this);
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                if (mSocketListener != null)
                                    mSocketListener.onAuthenticationFailure(e.getMessage());
                                DebugUtils.logError(TAG, "socketAuthenticate: " + e.toString());
                                mSocket.disconnect();
                            }
                        }
                    });
                } catch (Exception e) {
                    if (mSocketListener != null)
                        mSocketListener.onConnectionError(e.getMessage());
                    DebugUtils.logError(TAG, "disconnecting-onemittig");
                    mSocket.disconnect();
                }
            }
        });
        mSocket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object[] args) {
                DebugUtils.logError(TAG, "connection-error: " + args[0].toString());
                if (mSocketListener != null)
                    mSocketListener.onConnectionLost(disconnectingPurposefully);

                if (disconnectingPurposefully)
                    mSocket.disconnect();
                else
                    mSocketListener.onConnectionLost(false);
            }
        });
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
            @Override
            public void call(Object[] args) {
                if (mSocketListener != null)
                    mSocketListener.onConnectionError("Error - WebSocket connection timeout");

                mSocket.disconnect();

                DebugUtils.logError(TAG, "disconnecting-connect-timeout");
            }
        });
        mSocket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object[] args) {
                connecting = false;
                if (mSocketListener != null)
                    mSocketListener.onDisconnect(disconnectingPurposefully);

                disconnectingPurposefully = false;
            }
        });
    }

    public void onNodeEventListener() {
        if (mSocket != null) {
            removeNodeListener();
            DebugUtils.logDebug(TAG, "Push: Subscribed");
            mSocket.on("push", new Emitter.Listener() {
                @Override
                public void call(Object[] args) {
                    try {
                        OrderItemModel mOrder = BentoOrderJsonParser.parseBentoOrderItem(args[0].toString());
                        ArrayList<OrderItemModel> aTempListOder = new ArrayList<>();
                        //DebugUtils.logDebug(TAG, "Push: " + aListTask.get(0).getOrderId() + " Type: " + mOrder.getOrderType() + " After: " + mOrder.getAfter());

                        boolean bRefresh = true;
                        int iOrderId = -1;

                        switch (mOrder.getOrderType()) {
                            case "ASSIGN":
                                if (aListTask.isEmpty()) {
                                    aListTask.add(mOrder);
                                    BentoDriveUtil.showInAppNotification(WebSocketService.this, ConstantUtil.optTaskChanged.ASSIGN);
                                } else if (mOrder.getAfter().isEmpty()) {
                                    if (aListTask.isEmpty()) {
                                        BentoDriveUtil.showInAppNotification(WebSocketService.this, ConstantUtil.optTaskChanged.ASSIGN);
                                    } else {
                                        bRefresh = false;
                                    }
                                    aListTask.add(mOrder);
                                } else {
                                    if (aListTask.get(0).getOrderId().equals(mOrder.getAfter())) {
                                        aTempListOder.add(mOrder);
                                        BentoDriveUtil.showInAppNotification(WebSocketService.this, ConstantUtil.optTaskChanged.SWITCHED);
                                        aTempListOder.addAll(aListTask);
                                    } else {
                                        for (int a = 0; a < aListTask.size(); a++) {
                                            if (aListTask.get(a).getOrderId().equals(mOrder.getAfter())) {
                                                aTempListOder.add(mOrder);
                                            }
                                            aTempListOder.add(aListTask.get(a));
                                        }
                                        bRefresh = false;
                                    }
                                    aListTask = (ArrayList<OrderItemModel>) aTempListOder.clone();
                                }

                                saveListTask(aListTask);

                                if (mSocketListener != null)
                                    mSocketListener.onAssign(aListTask, bRefresh);

                                break;
                            case "UNASSIGN":
                                for (int a = 0; a < aListTask.size(); a++)
                                    if (aListTask.get(a).getOrderId().equals(mOrder.getOrderId())) {
                                        iOrderId = a;
                                        aListTask.remove(a);
                                        break;
                                    }

                                if (iOrderId == 0) {
                                    if (aListTask.isEmpty())
                                        BentoDriveUtil.showInAppNotification(WebSocketService.this, ConstantUtil.optTaskChanged.REMOVED);
                                    else
                                        BentoDriveUtil.showInAppNotification(WebSocketService.this, ConstantUtil.optTaskChanged.SWITCHED);
                                }

                                saveListTask(aListTask);

                                if (mSocketListener != null)
                                    mSocketListener.onUnassign(aListTask, bRefresh);
                                break;
                            case "REPRIORITIZE":
                                if (aListTask.isEmpty()) {
                                    aListTask.add(mOrder);
                                    BentoDriveUtil.showInAppNotification(WebSocketService.this, ConstantUtil.optTaskChanged.ASSIGN);
                                } else {
                                    String sOrderId = aListTask.get(0).getOrderId();

                                    for (int a = 0; a < aListTask.size(); a++) {
                                        if (aListTask.get(a).getOrderId().equals(mOrder.getAfter())) {
                                            aTempListOder.add(mOrder);
                                        }

                                        if (!aListTask.get(a).getOrderId().equals(mOrder.getOrderId()))
                                            aTempListOder.add(aListTask.get(a));
                                    }

                                    if (mOrder.getAfter().isEmpty())
                                        aTempListOder.add(mOrder);

                                    aListTask = (ArrayList<OrderItemModel>) aTempListOder.clone();

                                    if (!aListTask.get(0).getOrderId().equals(sOrderId))
                                        BentoDriveUtil.showInAppNotification(WebSocketService.this, ConstantUtil.optTaskChanged.SWITCHED);

                                }

                                saveListTask(aListTask);

                                if (mSocketListener != null)
                                    mSocketListener.onReprioritize(aListTask, bRefresh);
                                break;
                            default:
                                DebugUtils.logDebug(TAG, "OrderType: Unhandled " + mOrder.getOrderType());
                                break;
                        }

                    } catch (Exception e) {
                        DebugUtils.logError(TAG, "Push: " + e.toString());
                    }
                }
            });

            if (BentoDriveUtil.bIsKokushoTesting) {
               /* DebugUtils.logDebug(TAG, "Pong: Subscribed");
                mSocket.on("pong", new Emitter.Listener() {
                    @Override
                    public void call(Object[] args) {
                        try {
                            DebugUtils.logDebug(TAG, "Pong: " + args[0].toString());
                        } catch (Exception e) {
                            DebugUtils.logError(TAG, "Pong: " + e.toString());
                        }
                    }
                });*/
            }
        }
    }

    public void removeNodeListener() {
        if (mSocket != null) {
            mSocket.off("push");
            mSocket.off("pong");
        }
    }


    public void disconnectWebSocket() {
        disconnectingPurposefully = true;
        connecting = false;
        SharedPreferencesUtil.setAppPreference(WebSocketService.this, SharedPreferencesUtil.IS_USER_LOG_IN, false);
        sUsername = "";
        sPassword = "";

        OrderItemDAO.deleteAll();

        if (mSocket != null) {
            DebugUtils.logDebug(TAG, "disconnecting");
            removeNodeListener();
            mSocket.off();
            mSocket.disconnect();
        }

        GoogleLocationUtil.stopLocationUpdates();
    }

    public boolean isConnectedUser() {
        if (mSocket != null && mSocket.connected() && SharedPreferencesUtil.getBooleanPreference(WebSocketService.this, SharedPreferencesUtil.IS_USER_LOG_IN)) {
            return true;
        } else {
            return false;
        }
    }

    public List<OrderItemModel> getListTask() {
        if (aListTask == null)
            aListTask = new ArrayList<>();

        return aListTask;
    }

    public void saveListTask(List<OrderItemModel> aListNewTask) {
        OrderItemDAO.saveAll(aListNewTask);
        aListTask = OrderItemDAO.getAllTask();
    }

    public void setWebSocketLister(WebSocketEventListener mListener) {
        mSocketListener = mListener;
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
                            WidgetsUtils.createShortToast("onLocationUpdated: " + mResponse.getMsg());
                        } else {
                            String sResponse = mResponse.getRet();
                            DebugUtils.logDebug(TAG, "onLocationUpdated: " + sResponse);
                        }
                    } catch (Exception e) {
                        DebugUtils.logError(TAG, "onLocationUpdated: " + e.toString());
                        WidgetsUtils.createShortToast("onLocationUpdated: " + e.toString());
                    }
                }
            });
        } else {
            DebugUtils.logError(TAG, "Cant send the current location");
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
