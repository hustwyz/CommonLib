package im.wyz.common.http;

import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.telephony.TelephonyManager;
import android.text.TextUtils;


public class NetworkUtil {

	/**
	 * NONE
	 */
	public static final int APN_NONE = 0;
	/**
	 * CMWAP
	 */
	public static final int APN_CMWAP = 1;
	/**
	 * CMNET
	 */
	public static final int APN_CMNET = 2;
	/**
	 * WIFI
	 */
	public static final int APN_WIFI = 3;

	public static final int NETWORK_UNKNOWN = -1;

	public static final int NETWORK_NONE = 0;

	public static final int NETWORK_WIFI = 1;

	public static final int NETWORK_MOBILE = 2;

	public static final int NETWORK_2G = 3;

	public static final int NETWORK_3G = 4;

	public static final String[] networks = { "没有网络", "wifi", "mobile", "2G",
			"3G" };

	private static String hostIp = null;
	private static int hostPort = 0;

	/**
	 * get accesss point type
	 * 
	 * @param context
	 * @return
	 */
	public static int getAccessPointType(Context context) {

		ConnectivityManager mConnectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = mConnectivity.getActiveNetworkInfo();
		if ((info != null) && info.isAvailable()) {
			if (info.getTypeName().toLowerCase().equals("mobile")) {
				hostIp = Proxy.getHost(context);
				hostPort = Proxy.getPort(context);
				if (!TextUtils.isEmpty(hostIp) && (hostPort != 0)) {
					return APN_CMWAP;
				} else {
					return APN_CMNET;
				}
			} else if (info.getTypeName().toLowerCase().equals("wifi")) {
				return APN_WIFI;
			}
		}
		return APN_NONE;
	}

	public static String getHostIp() {
		return hostIp;
	}

	public static int getHostPort() {
		return hostPort;
	}

	/**
	 * get network type
	 * 
	 * @param context
	 * @return
	 */
	public static int getNetworkType(Context context) {

		int networkType = NETWORK_UNKNOWN;

		ConnectivityManager mConnectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = mConnectivity.getActiveNetworkInfo();

		if ((info != null) && info.isAvailable()) {
			if (info.getTypeName().toLowerCase().equals("wifi")) {
				networkType = NETWORK_WIFI;
			} else if (info.getTypeName().toLowerCase().equals("mobile")) {
				networkType = NETWORK_MOBILE;
				TelephonyManager mTelephonyManager = (TelephonyManager) context
						.getSystemService(Service.TELEPHONY_SERVICE);
				int netType = mTelephonyManager.getNetworkType();
				if (netType == TelephonyManager.NETWORK_TYPE_UNKNOWN) {
					networkType = NETWORK_UNKNOWN;
				}
				if (netType == TelephonyManager.NETWORK_TYPE_GPRS
						|| netType == TelephonyManager.NETWORK_TYPE_EDGE) {
					networkType = NETWORK_2G;
				} else {
					networkType = NETWORK_3G;
				}
			}
		} else {
			networkType = NETWORK_NONE;
		}
		return networkType;
	}

	public static String getNetworkName(Context context) {
		int type = getNetworkType(context);
		if (type == NETWORK_UNKNOWN) {
			return "未知网络类型";
		} else {
			return networks[type];
		}
	}

	/**
	 * return true if network is available, false or not
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {
		return NetworkUtil.getAccessPointType(context) != NetworkUtil.APN_NONE;
	}

}
