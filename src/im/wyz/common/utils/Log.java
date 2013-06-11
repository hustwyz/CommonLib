package im.wyz.common.utils;

import im.wyz.common.Config;

public class Log {

	private static final boolean DEBUG = Config.DEBUG;

	private String TAG = null;

	public Log(Class<?> clazz) {
		TAG = "[" + clazz.getSimpleName() + "]";
	}

	public void d(Object object) {
		if (!DEBUG)
			return;
		android.util.Log.d(TAG, object.toString());
	}

	public void e(Object object) {
		if (!DEBUG)
			return;
		android.util.Log.e(TAG, object.toString());
	}

	public void i(Object object) {
		if (!DEBUG)
			return;
		android.util.Log.i(TAG, object.toString());
	}

	public void v(Object object) {
		if (!DEBUG)
			return;
		android.util.Log.v(TAG, object.toString());
	}
	
	public static void e(String TAG,Object object){
		if(!DEBUG)
			return;
		android.util.Log.e(TAG, object.toString());
	}
	
	public static void d(String TAG,Object object){
		if(!DEBUG)
			return;
		android.util.Log.d(TAG, object.toString());
	}
	
	public static void v(String TAG,Object object){
		if(!DEBUG)
			return;
		android.util.Log.v(TAG, object.toString());
	}
	
	public static void i(String TAG,Object object){
		if(!DEBUG)
			return;
		android.util.Log.i(TAG, object.toString());
	}

}
