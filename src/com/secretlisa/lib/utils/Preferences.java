package com.secretlisa.lib.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Preferences {

	public static final String USER_PREFERENCE = "secretlisa_pref";

	public static int getUserPrefInt(Context context, String key,
			int defaultValue) {
		SharedPreferences pref = context.getSharedPreferences(USER_PREFERENCE,
				0);
		return pref.getInt(key, defaultValue);
	}

	public static boolean getUserPrefBoolean(Context context, String key,
			boolean defaultValue) {
		SharedPreferences pref = context.getSharedPreferences(USER_PREFERENCE,
				0);
		return pref.getBoolean(key, defaultValue);
	}

	public static String getUserPrefString(Context context, String key,String defaultValue) {
		SharedPreferences pref = context.getSharedPreferences(USER_PREFERENCE,
				0);
		return pref.getString(key, defaultValue);
	}
	
	public static long getUserPrefLong(Context context, String key,long defaultValue) {
		SharedPreferences pref = context.getSharedPreferences(USER_PREFERENCE,
				0);
		return pref.getLong(key, defaultValue);
	}

	public static void setUserPrefInt(Context context, String key, int value) {
		SharedPreferences pref = context.getSharedPreferences(USER_PREFERENCE,
				0);
		Editor edit = pref.edit();
		edit.putInt(key, value);
		edit.commit();
	}

	public static void setUserPrefBoolean(Context context, String key,
			boolean value) {
		SharedPreferences pref = context.getSharedPreferences(USER_PREFERENCE,
				0);
		Editor edit = pref.edit();
		edit.putBoolean(key, value);
		edit.commit();
	}

	public static void setUserPrefString(Context context, String key,
			String value) {
		SharedPreferences pref = context.getSharedPreferences(USER_PREFERENCE,
				0);
		Editor edit = pref.edit();
		edit.putString(key, value);
		edit.commit();
	}
	
	public static void setUserPrefLong(Context context, String key,
			long value) {
		SharedPreferences pref = context.getSharedPreferences(USER_PREFERENCE,
				0);
		Editor edit = pref.edit();
		edit.putLong(key, value);
		edit.commit();
	}
	
	public static void clearUserPref(Context context){
		SharedPreferences pref = context.getSharedPreferences(USER_PREFERENCE,
				0);
		Editor edit = pref.edit();
		edit.clear();
		edit.commit();
	}
	
	public static void removeKey(Context context,String key){
		SharedPreferences pref = context.getSharedPreferences(USER_PREFERENCE,
				0);
		Editor edit = pref.edit();
		edit.remove(key);
		edit.commit();
	}

}
