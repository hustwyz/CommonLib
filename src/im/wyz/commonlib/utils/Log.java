package im.wyz.commonlib.utils;

import im.wyz.commonlib.CommonConfig;

public class Log {

	private static final boolean DEBUG = CommonConfig.DEBUG;

	private static final String TAG_ = "SecretLisa";

	private String TAG = null;

	public Log(Class<?> clazz) {
		TAG = "[" + clazz.getSimpleName() + "]";
	}

	public void d(Object object) {
		if (!DEBUG)
			return;
		android.util.Log.d(TAG_, TAG + object.toString());
	}

	public void e(Object object) {
		if (!DEBUG)
			return;
		android.util.Log.e(TAG_, TAG + object.toString());
	}

	public void e(Object object, Exception e) {
		if (!DEBUG)
			return;
		android.util.Log.e(TAG_, TAG + object.toString(), e);
	}

	public void w(Object object) {
		if (!DEBUG)
			return;
		android.util.Log.e(TAG_, TAG + object.toString());
	}

	public void w(Object object, Exception e) {
		if (!DEBUG)
			return;
		android.util.Log.e(TAG_, TAG + object.toString(), e);
	}

	public void i(Object object) {
		if (!DEBUG)
			return;
		android.util.Log.i(TAG_, TAG + object.toString());
	}

	public void v(Object object) {
		if (!DEBUG)
			return;
		android.util.Log.v(TAG_, TAG + object.toString());
	}

	public static void e(String TAG, Object object) {
		if (!DEBUG)
			return;
		android.util.Log.e(TAG, object.toString());
	}

	public static void e(String TAG, Object object, Exception e) {
		if (!DEBUG)
			return;
		android.util.Log.e(TAG_, TAG + object.toString(), e);
	}

	public static void d(String TAG, Object object) {
		if (!DEBUG)
			return;
		android.util.Log.d(TAG, object.toString());
	}

	public static void v(String TAG, Object object) {
		if (!DEBUG)
			return;
		android.util.Log.v(TAG, object.toString());
	}

	public static void i(String TAG, Object object) {
		if (!DEBUG)
			return;
		android.util.Log.i(TAG, object.toString());
	}

	public static void w(String TAG, Object object) {
		if (!DEBUG)
			return;
		android.util.Log.e(TAG, object.toString());
	}

	public static void w(String TAG, Object object, Exception e) {
		if (!DEBUG)
			return;
		android.util.Log.e(TAG, object.toString(), e);
	}

}
