package com.bentonow.drive.web;

import android.os.Looper;

import com.bentonow.drive.model.OrderItemModel;
import com.bentonow.drive.util.ConstantUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class BentoRestClient {

    static final String TAG = "BentoRestClient";

    public static AsyncHttpClient syncHttpClient = new SyncHttpClient();
    public static AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

    private static class CustomSSLSocketFactory extends SSLSocketFactory {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        public CustomSSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
            super(truststore);

            TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[]{tm}, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }

    private static AsyncHttpClient getClient() {
        // Return the synchronous HTTP client when the thread is not prepared
        if (Looper.myLooper() == null)
            return syncHttpClient;
        return asyncHttpClient;
    }


    public static void init() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException, KeyManagementException {
        // We initialize a default Keystore
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        // We load the KeyStore
        trustStore.load(null, null);
        // We initialize a new SSLSocketFacrory
        CustomSSLSocketFactory socketFactory = new CustomSSLSocketFactory(trustStore);
        // We set that all host names are allowed in the socket factory
        socketFactory.setHostnameVerifier(CustomSSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        // We set the SSL Factory
        getClient().setSSLSocketFactory(socketFactory);
    }


    public static void getAssignedOrders(RequestParams params, AsyncHttpResponseHandler responseHandler) {
        getClient().get(BentoDriveAPI.getAssignedOrdersUrl(), params, responseHandler);
    }

    public static void getStatusOrder(ConstantUtil.optStatusOrder optStatusOrder, OrderItemModel mOrderModel, AsyncHttpResponseHandler responseHandler) {
        getClient().get(BentoDriveAPI.getStatusOrderUrl(optStatusOrder, mOrderModel.getOrderId()), null, responseHandler);
    }

    public static void getMinVersion(AsyncHttpResponseHandler responseHandler) {
        RequestParams mParams = new RequestParams();
        mParams.add("device_id", "android");
        getClient().post(BentoDriveAPI.getMinVersionUrl(), mParams, responseHandler);
    }

/*    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Log.i(TAG, "[POST] " + getAbsoluteUrl(url));
        Log.i(TAG, "[params] " + (params != null ? params.toString() : "null"));
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void getCustom(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Log.i(TAG, "[GET] " + url);
        Log.i(TAG, "[params] " + (params != null ? params.toString() : "null"));
        client.get(url, params, responseHandler);
    }*/

}
