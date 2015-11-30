package com.bentonow.drive.web.request;

import com.bentonow.drive.listener.InterfaceWebRequest;
import com.bentonow.drive.listener.ListenerWebRequest;

/**
 * Created by Jose Torres on 11/10/15.
 */
public class RequestGetAssignedOrders implements InterfaceWebRequest {

    public static final String TAG = "RequestGetAssignedOrders";
    private ListenerWebRequest mListener;

    public RequestGetAssignedOrders(ListenerWebRequest mListener) {
        this.mListener = mListener;
    }

    @Override
    public void dispatchRequest() {

       /* Request request = new Request(Request.Method.GET, BentoDriveAPI.getAssignedOrdersUrl(), getErrorListener()) {

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

                    // DebugUtils.logDebug(TAG, "Response: " + jsonString);
                    // DebugUtils.logDebug(TAG, "Headers: " + networkResponse.headers);
                    DebugUtils.logDebug(TAG, "Time: " + networkResponse.networkTimeMs);
                    DebugUtils.logDebug(TAG, "Code: " + networkResponse.statusCode);
                    // DebugUtils.logDebug(TAG, "Modified: " + networkResponse.notModified);

                    ArrayList<OrderItemModel> aListOrder;

                    aListOrder = BentoOrderJsonParser.parseBentoListOrder(jsonString);

                    if (mListener != null)
                        mListener.onResponse(aListOrder);
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

        Application.getInstance().getVolleyRequest().add(request);*/
    }

    /*private Response.ErrorListener getErrorListener() {
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
    }*/
}
