package im.wyz.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Preferences {

	public static final String USER_PREFERENCE = "user_pref";

	public static int getUserPrefInt(Context mContext, String key,
			int defaultValue) {
		SharedPreferences pref = mContext.getSharedPreferences(USER_PREFERENCE,
				0);
		return pref.getInt(key, defaultValue);
	}

	public static boolean getUserPrefBoolean(Context mContext, String key,
			boolean defaultValue) {
		SharedPreferences pref = mContext.getSharedPreferences(USER_PREFERENCE,
				0);
		return pref.getBoolean(key, defaultValue);
	}

	public static String getUserPrefString(Context mContext, String key,String defaultValue) {
		SharedPreferences pref = mContext.getSharedPreferences(USER_PREFERENCE,
				0);
		return pref.getString(key, defaultValue);
	}
	
	public static long getUserPrefLong(Context mContext, String key,long defaultValue) {
		SharedPreferences pref = mContext.getSharedPreferences(USER_PREFERENCE,
				0);
		return pref.getLong(key, defaultValue);
	}

	public static void setUserPrefInt(Context mContext, String key, int value) {
		SharedPreferences pref = mContext.getSharedPreferences(USER_PREFERENCE,
				0);
		Editor edit = pref.edit();
		edit.putInt(key, value);
		edit.commit();
	}

	public static void setUserPrefBoolean(Context mContext, String key,
			boolean value) {
		SharedPreferences pref = mContext.getSharedPreferences(USER_PREFERENCE,
				0);
		Editor edit = pref.edit();
		edit.putBoolean(key, value);
		edit.commit();
	}

	public static void setUserPrefString(Context mContext, String key,
			String value) {
		SharedPreferences pref = mContext.getSharedPreferences(USER_PREFERENCE,
				0);
		Editor edit = pref.edit();
		edit.putString(key, value);
		edit.commit();
	}
	
	public static void setUserPrefLong(Context mContext, String key,
			long value) {
		SharedPreferences pref = mContext.getSharedPreferences(USER_PREFERENCE,
				0);
		Editor edit = pref.edit();
		edit.putLong(key, value);
		edit.commit();
	}

}
