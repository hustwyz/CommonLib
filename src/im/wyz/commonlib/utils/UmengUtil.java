package im.wyz.commonlib.utils;

import im.wyz.commonlib.CommonConfig;

import java.util.HashMap;

import android.content.Context;

import com.umeng.analytics.MobclickAgent;

public class UmengUtil {

	/**
	 * 友盟统计，点击事件
	 * 
	 * @param mContext
	 * @param event
	 */
	public static void onUmengEvent(Context context, String event) {
		if (CommonConfig.DEBUG)
			MobclickAgent.onEvent(context, event);
	}

	/**
	 * 友盟统计，点击事件
	 * 
	 * @param mContext
	 * @param event
	 * @param map
	 */
	public static void onUmengEvent(Context context, String event,
			HashMap<String, String> map) {
		if (CommonConfig.DEBUG)
			MobclickAgent.onEvent(context, event, map);
	}
	
	public static void onUmengEventBegin(Context context, String event) {
		if (CommonConfig.DEBUG)
			MobclickAgent.onEventBegin(context, event);
	}
	
	public static void onUmengEventEnd(Context context, String event) {
		if (CommonConfig.DEBUG)
			MobclickAgent.onEventEnd(context, event);
	}
	
}
