package com.bentonow.drive.web.request;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.bentonow.drive.Application;
import com.bentonow.drive.listener.InterfaceWebRequest;
import com.bentonow.drive.listener.ListenerWebRequest;
import com.bentonow.drive.model.OrderItemModel;
import com.bentonow.drive.util.ConstantUtil;
import com.bentonow.drive.util.DebugUtils;
import com.bentonow.drive.web.BentoDriveAPI;

import java.util.HashMap;

/**
 * Created by Jose Torres on 11/10/15.
 */
public class RequestGetStatusOrders implements InterfaceWebRequest {

    public static final String TAG = "RequestGetAssignedOrders";
    private OrderItemModel mOrderModel;
    private ConstantUtil.optStatusOrder optStatusOrder;

    private ListenerWebRequest mListener;

    public RequestGetStatusOrders(ConstantUtil.optStatusOrder optStatusOrder, OrderItemModel mOrderModel, ListenerWebRequest mListener) {
        this.mListener = mListener;
        this.mOrderModel = mOrderModel;
        this.optStatusOrder = optStatusOrder;
    }

    @Override
    public void dispatchRequest() {

        Request request = new Request(Request.Method.GET, BentoDriveAPI.getStatusOrderUrl(optStatusOrder, mOrderModel.getId()), getErrorListener()) {

            @Override
            public int compareTo(Object another) {
                return 0;
            }

            @Override
            public HashMap<String, String> getParams() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "text/xml; charset=UTF-8");
                return headers;
            }

            @Override
            protected Response parseNetworkResponse(NetworkResponse networkResponse) {
                String jsonString;
                try {
                    jsonString = new String(networkResponse.data, HttpHeaderParser.parseCharset(networkResponse.headers));

                    DebugUtils.logDebug(TAG, "Response: " + jsonString);
                    // DebugUtils.logDebug(TAG, "Headers: " + networkResponse.headers);
                    DebugUtils.logDebug(TAG, "Time: " + networkResponse.networkTimeMs);
                    DebugUtils.logDebug(TAG, "Code: " + networkResponse.statusCode);
                    // DebugUtils.logDebug(TAG, "Modified: " + networkResponse.notModified);

                    if (mListener != null)
                        mListener.onResponse(jsonString);
                } catch (Exception ex) {
                    DebugUtils.logError(TAG, ex);
                    if (mListener != null)
                        mListener.onError(ex.toString());
                }

                return Response.success(networkResponse, HttpHeaderParser.parseCacheHeaders(networkResponse));
            }

            @Override
            protected void deliverResponse(Object response) {
                //DebugUtils.logDebug("DeliverResponse", response.toString());
            }
        };

        request.setTag(TAG);

        Application.getInstance().getVolleyRequest().add(request);
    }

    private Response.ErrorListener getErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (mListener != null)
                    mListener.onError(error.getLocalizedMessage());
                DebugUtils.logDebug(TAG, "Response: " + error.getLocalizedMessage());
                if (error.networkResponse != null) {
                    DebugUtils.logError(TAG, "Headers: " + error.networkResponse.headers);
                    DebugUtils.logDebug(TAG, "Code: " + error.networkResponse.statusCode);
                    DebugUtils.logDebug(TAG, "Modified: " + error.networkResponse.notModified);
                }
                DebugUtils.logDebug(TAG, "Time: " + error.getNetworkTimeMs());
            }
        };
    }
}
