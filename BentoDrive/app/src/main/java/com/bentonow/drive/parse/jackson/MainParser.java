/**
 * @author Kokusho Torres
 */

package com.bentonow.drive.parse.jackson;

import com.bentonow.drive.util.DebugUtils;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MainParser {

    static long init, now;

    public static String nameField;
    public static JsonFactory jsonFactory;
    public static JsonParser jp;

    public static final String TAG_ID = "id";
    public static final String TAG_NAME = "name";
    public static final String TAG_PHONE = "phone";
    public static final String TAG_ADDRESS = "address";
    public static final String TAG_STREET = "street";
    public static final String TAG_RESIDENCE = "residence";
    public static final String TAG_CITY = "city";
    public static final String TAG_REGION = "region";
    public static final String TAG_ZIPCODE = "zipCode";
    public static final String TAG_COUNTRY = "country";
    public static final String TAG_LAT = "lat";
    public static final String TAG_LNG = "lng";
    public static final String TAG_ITEM = "item";
    public static final String TAG_ITEMS = "items";
    public static final String TAG_TYPE = "type";
    public static final String TAG_LABEL = "label";
    public static final String TAG_KEY = "key";
    public static final String TAG_ORDERSTRING = "orderString";
    public static final String TAG_DRIVERID = "driverId";
    public static final String TAG_STATUS = "status";

//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";


    public static void tagNotFound() {
        try {
            jp.skipChildren();
        } catch (Exception e) {
            DebugUtils.logError(e);
        }
    }

    public static void startParsed() {
        init = System.currentTimeMillis();
    }

    public static void stopParsed() {
        now = System.currentTimeMillis();
        DebugUtils.logDebug("Parse en :: " + (now - init) + " ms");
    }

    public static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

}
