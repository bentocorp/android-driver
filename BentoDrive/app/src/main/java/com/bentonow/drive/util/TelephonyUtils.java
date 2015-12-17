package com.bentonow.drive.util;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

import com.bentonow.drive.model.CallStatusModel;

import java.util.Calendar;


public class TelephonyUtils {

	static NetworkInfo network;
	static Calendar c = Calendar.getInstance();
	static LocationManager locationManager;
	protected LongProcess process = null;
	protected ReportCallWebService getCallWs = null;
	String Imei;
	static int strenght = 0;

	public CallStatusModel getInformation(Context context) {
		CallStatusModel pojo = new CallStatusModel();
		network = getNetworkInfo(context);

		if (network == null || !network.isConnectedOrConnecting()) {
			// Sys.log("kokusho", "No connection");
			pojo.setConexion(false);
			pojo.setTipoConexion("N/A");
		} else {

			String conexion = typeConection(network.getType(),
					network.getSubtype());
			pojo.setTipoConexion(conexion);
		}

		pojo.setImei(getImei(context));

		HomeActivity.Imei = pojo.getImei();

		TelephonyManager telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);

		GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager
				.getCellLocation();

		pojo.setOperator(getCarrierName(context));

		String networkOperator = telephonyManager.getNetworkOperator();

		pojo.setMcc(networkOperator.substring(0, 3));

		pojo.setMcn(networkOperator.substring(3));

		int cid = cellLocation.getCid();

		int lac = cellLocation.getLac();

		pojo.setCellLocation(cellLocation.toString());

		pojo.setCell(String.valueOf(cid));

		pojo.setGsmLocation(String.valueOf(lac));

		pojo.setCountryCode(telephonyManager.getNetworkCountryIso());

		pojo.setHour(getHora());

		pojo.setMinute(getMinute());

		pojo.setSecond(getSecond());

		pojo.setModel(android.os.Build.MODEL);

		pojo.setVersionAndroid(android.os.Build.VERSION.RELEASE);

		
		double lat = getLastPosition(context).getLatitude();
		double lon = getLastPosition(context).getLongitude();

		pojo.setLat(lat);
		pojo.setLon(lon);

		pojo.setStrenght(strenght);

		pojo.setMessage(pojo.getTipoConexion() + "," + pojo.getOperator() + ","
				+ pojo.getStrenght() + "," + pojo.getMcc() + ","
				+ pojo.getMcn() + "," + pojo.getCellLocation() + ","
				+ pojo.getCell() + "," + pojo.getGsmLocation() + ","
				+ pojo.getCountryCode() + "," + pojo.getHour() + ":"
				+ pojo.getMinute() + ":" + pojo.getSecond() + ","
				+ pojo.getModel() + "," + pojo.getVersionAndroid() + ","
				+ pojo.getLat() + "," + pojo.getLon());

		return pojo;

	}

	public static NetworkInfo getNetworkInfo(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm.getActiveNetworkInfo();
	}

	public static String getImei(Context context) {
		TelephonyManager mngr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);

		return mngr.getDeviceId();
	}

	public static String getCarrierName(Context context) {
		TelephonyManager manager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String carrierName = manager.getNetworkOperatorName();
		return carrierName;
	}

	/**
	 * Check if there is any connectivity
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isConnected(Context context) {
		NetworkInfo info = getNetworkInfo(context);
		return (info != null && info.isConnected());
	}

	/**
	 * Check if there is any connectivity to a Wifi network
	 * 
	 * @param context
	 * @param type
	 * @return
	 */
	public static boolean isConnectedWifi(Context context) {
		NetworkInfo info = getNetworkInfo(context);
		return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
	}

	/**
	 * Check if there is any connectivity to a mobile network
	 * 
	 * @param context
	 * @param type
	 * @return
	 */
	public static boolean isConnectedMobile(Context context) {
		NetworkInfo info = getNetworkInfo(context);
		return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
	}

	/**
	 * Check if there is fast connectivity
	 * 
	 * @param context
	 * @return
	 */

	/**
	 * Check if the connection is fast
	 * 
	 * @param type
	 * @param subType
	 * @return
	 */
	public static String typeConection(int type, int subType) {
		String network = "";

		if (type == ConnectivityManager.TYPE_WIFI) {
			return "Wi fi";
		} else if (type == ConnectivityManager.TYPE_MOBILE) {
			switch (subType) {
			case TelephonyManager.NETWORK_TYPE_1xRTT:
				return "RTT"; // ~ 50-100 kbps
			case TelephonyManager.NETWORK_TYPE_CDMA:
				return "CDMA"; // ~ 14-64 kbps
			case TelephonyManager.NETWORK_TYPE_EDGE:
				return "Edge"; // ~ 50-100 kbps
			case TelephonyManager.NETWORK_TYPE_EVDO_0:
				return "EVDO 0"; // ~ 400-1000 kbps
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
				return "EVDO A"; // ~ 600-1400 kbps
			case TelephonyManager.NETWORK_TYPE_GPRS:
				return "GPRS"; // ~ 100 kbps
			case TelephonyManager.NETWORK_TYPE_HSDPA:
				return "HSDPA"; // ~ 2-14 Mbps
			case TelephonyManager.NETWORK_TYPE_HSPA:
				return "HSPDA"; // ~ 700-1700 kbps
			case TelephonyManager.NETWORK_TYPE_HSUPA:
				return "HSUPA"; // ~ 1-23 Mbps
			case TelephonyManager.NETWORK_TYPE_UMTS:
				return "UMTS"; // ~ 400-7000 kbps
				/*
				 * Above API level 7, make sure to set android:targetSdkVersion
				 * to appropriate level to use these
				 */
				// case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
				// return "EHRPD"; // ~ 1-2 Mbps
				// case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
				// return "EVDO B"; // ~ 5 Mbps
				// case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
				// return "HSPAP"; // ~ 10-20 Mbps
				// case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
				// return "IDEN"; // ~25 kbps
				// case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
				// return "LTE"; // ~ 10+ Mbps
				// Unknown
			case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			default:
				return "No hay conexion";
			}
		} else {
			return network;
		}
	}

	public static int getHora() {
		return c.get(Calendar.HOUR);
	}

	public static int getMinute() {
		return c.get(Calendar.MINUTE);
	}

	public static int getSecond() {
		return c.get(Calendar.SECOND);
	}

	public static Location getLastPosition(Context context) {
		locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);

		// Creating an empty criteria object
		Criteria criteria = new Criteria();
		String provider;
		Location location = null;
		// Getting the name of the provider that meets the criteria
		provider = locationManager.getBestProvider(criteria, false);

		if (provider != null && !provider.equals("")) {

			// Get the location from the given provider
			location = locationManager.getLastKnownLocation(provider);
		}

		// Location location =
		// locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		// //<5>
		return location;
	}

	public static boolean isGpsEnable(Context context) {
		final LocationManager manager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);

		if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			return false;
		}
		return true;
	}

	public static void comprobarSim(Context context) {

		boolean res = false;

		TelephonyManager telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);

		GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager
				.getCellLocation();

		int cid = cellLocation.getCid();

	}

	public CallStatusPojo getInformationNull(Context context) {
		CallStatusPojo pojo = new CallStatusPojo();
		network = getNetworkInfo(context);

		if (network == null || !network.isConnectedOrConnecting()) {
			// Sys.log("kokusho", "No connection");
			pojo.setConexion(false);

		} else {

			String conexion = typeConection(network.getType(),
					network.getSubtype());
			pojo.setTipoConexion(conexion);
		}

		pojo.setImei(getImei(context));

		HomeActivity.Imei = pojo.getImei();

		pojo.setOperator("N/A");

		pojo.setStrenght(0);

		pojo.setMcc("0");

		pojo.setMcn("0");

		pojo.setCellLocation("0");

		pojo.setCell("0");

		pojo.setGsmLocation("0");

		pojo.setCountryCode("N/A");

		pojo.setHour(getHora());

		pojo.setMinute(getMinute());

		pojo.setSecond(getSecond());

		pojo.setModel(android.os.Build.MODEL);

		pojo.setVersionAndroid(android.os.Build.VERSION.RELEASE);

		double lat;
		double lon;
		try {
			lat = getLastPosition(context).getLatitude();
			lon = getLastPosition(context).getLongitude();
			pojo.setLat(lat);
			pojo.setLon(lon);
		} catch (Exception e) {
			pojo.setLat(0);
			pojo.setLon(0);
		}

		pojo.setMessage(pojo.getTipoConexion() + "," + pojo.getOperator() + ","
				+ pojo.getMcc() + "," + pojo.getMcn() + ","
				+ pojo.getCellLocation() + "," + pojo.getCell() + ","
				+ pojo.getGsmLocation() + "," + pojo.getCountryCode() + ","
				+ pojo.getHour() + ":" + pojo.getMinute() + ":"
				+ pojo.getSecond() + "," + pojo.getModel() + ","
				+ pojo.getVersionAndroid() + "," + pojo.getLat() + ","
				+ pojo.getLon());

		return pojo;

	}

	public static void strenghtSignal(Context context) {

		SignalStrengthListener phoneListener = new SignalStrengthListener(
				new PhoneStateInterface() {

					@Override
					public void phoneStateInterface(CallStatusPojo phoneStats) {
						Sys.log("kokusho telephone", "señal telefono "
								+ phoneStats.getStrenght());
						strenght = (phoneStats.getStrenght());

					}
				});
		TelephonyManager telephony = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		telephony.listen(phoneListener,
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		telephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
	}
	
	public static String getPhoneNumber(Context context){
		TelephonyManager tMgr =(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		  return tMgr.getLine1Number();
	}

}
