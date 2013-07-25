package com.secretlisa.lib.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.secretlisa.lib.R;

public class CommonUtil {

	public static final String[] WEEKDAYS = { "周日", "周一", "周二", "周三", "周四",
			"周五", "周六" };

	public static Log getLog(Class<?> clazz) {
		return new Log(clazz);
	}

	/**
	 * 屏幕的宽度
	 * 
	 * @param mContext
	 * @return
	 */
	public static int getScreenWidth(Context context) {
		return context.getResources().getDisplayMetrics().widthPixels;
	}

	/**
	 * 屏幕的高度
	 * 
	 * @param mContext
	 * @return
	 */
	public static int getScreenHeight(Context context) {
		return context.getResources().getDisplayMetrics().heightPixels;
	}

	/**
	 * 获取版本名
	 * 
	 * @param context
	 * @return
	 */
	public static String getVersionName(Context context) {
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			return pi.versionName;
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * 获取版本号
	 * 
	 * @param context
	 * @return
	 */
	public static int getVersionCode(Context context) {
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			return pi.versionCode;
		} catch (NameNotFoundException e) {
			return -1;
		}
	}

	/**
	 * 获取md5加密的值
	 * 
	 * @param string
	 * @return
	 */
	public static String md5(String string) {
		return md5(string, null);
	}

	/**
	 * 获取md5加密后的字符串
	 * 
	 * @param string
	 * @param method
	 *            加密方法:md5或是sha-1等
	 * @return
	 */
	public static String md5(String string, String method) {
		if (string == null || string.trim().length() < 1) {
			return null;
		}
		String m = "md5";
		if (!TextUtils.isEmpty(method)) {
			m = method;
		}
		try {
			byte[] source = string.getBytes("UTF-8");
			MessageDigest md5 = MessageDigest.getInstance(m);
			StringBuffer result = new StringBuffer();
			for (byte b : md5.digest(source)) {
				result.append(Integer.toHexString((b & 0xf0) >>> 4));
				result.append(Integer.toHexString(b & 0x0f));
			}
			return result.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void showToast(Context context, int resId) {
		showToast(context, context.getString(resId));
	}

	public static void showToast(Context context, String text) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 启动一个activity
	 * 
	 * @param activity
	 * @param intent
	 * @param enterAnim
	 * @param exitAnim
	 */
	public static void goToActivity(Activity activity, Intent intent,
			int enterAnim, int exitAnim) {
		activity.startActivity(intent);
		if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.DONUT) {
			new PendingTransitionHelper(activity, enterAnim, exitAnim);
		}
	}

	/**
	 * 结束一个activity
	 * 
	 * @param activity
	 * @param enterAnim
	 * @param exitAnim
	 */
	public static void finishActivity(Activity activity, int enterAnim,
			int exitAnim) {
		activity.finish();
		if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.DONUT) {
			new PendingTransitionHelper(activity, enterAnim, exitAnim);
		}
	}

	/**
	 * 默认的启动一个activity的方式
	 * 
	 * @param activity
	 * @param intent
	 */
	public static void goToActivity(Activity activity, Intent intent) {
		activity.startActivity(intent);
		if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.DONUT) {
			new PendingTransitionHelper(activity, R.anim.in_from_right,
					R.anim.out_to_center);
		}
	}

	/**
	 * 默认的结束一个activity的方式
	 * 
	 * @param activity
	 */
	public static void finishActivity(Activity activity) {
		activity.finish();
		if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.DONUT) {
			new PendingTransitionHelper(activity, R.anim.in_from_center,
					R.anim.out_to_right);
		}
	}

	/**
	 * dip转化为px
	 * 
	 * @param context
	 * @param dipValue
	 *            要转化的dip
	 * @return int 单位px
	 */
	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	/**
	 * px转化为dip
	 * 
	 * @param context
	 * @param pxValue
	 *            要转化的px
	 * @return int 单位dip
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 将输入流度成字符串
	 * 
	 * @param is
	 * @return
	 */
	public static String changeIsToStr(InputStream is) {
		StringBuilder builder = new StringBuilder();
		try {
			String str;
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "utf-8"));
			while ((str = reader.readLine()) != null) {
				builder.append(str);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder.toString();
	}

	/**
	 * 将Bundle中的数据对编码成http-query的形式的字符串
	 * 
	 * @param parameters
	 * @return
	 */
	public static String encodeUrl(Bundle parameters) {
		if (parameters == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String key : parameters.keySet()) {
			if (first) {
				first = false;
			} else {
				sb.append("&");
			}
			sb.append(key + "=" + URLEncoder.encode(parameters.getString(key)));
		}
		return sb.toString();
	}

	/**
	 * 将http-query的形式的字符串解码存入Bundle中
	 * 
	 * @param s
	 * @return
	 */
	public static Bundle decodeUrl(String s) {
		Bundle params = new Bundle();
		if (s != null) {
			String array[] = s.split("&");
			for (String parameter : array) {
				String v[] = parameter.split("=");
				params.putString(URLDecoder.decode(v[0]),
						URLDecoder.decode(v[1]));
			}
		}
		return params;
	}

	/**
	 * 获取当前时间(13位)
	 * 
	 * @return long
	 */
	public static long getNowTime() {
		return System.currentTimeMillis();
	}

	/**
	 * 格式化时间
	 * 
	 * @param format
	 *            yyyy-MM-dd HH:mm
	 * @param time
	 * @return
	 */
	public static String formatTime(String format, long time) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(new Date(time));
	}

	/**
	 * 获取星期
	 * 
	 * @param time
	 * @return
	 */
	public static String getWeekdayString(long time) {
		return WEEKDAYS[getWeekdayIndex(time)];
	}

	/**
	 * 获取星期
	 * 
	 * @param timeOffset
	 * @return
	 */
	public static int getWeekdayIndex(long timeOffset) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timeOffset);
		int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
		if (w < 0)
			w = 0;
		return w;
	}

	/**
	 * 获取捕获的异常的字符串
	 * 
	 * @param Exception
	 *            e
	 * @return
	 */
	public static String getErrorString(Exception e) {
		StringBuilder builder = new StringBuilder();
		StackTraceElement[] elements = e.getStackTrace();
		for (StackTraceElement item : elements) {
			builder.append(item.toString()).append("\n");
		}
		return builder.toString();
	}

	/**
	 * 过滤SQL特殊字符防止注入
	 * 
	 * @param sql
	 * @return
	 */
	public static String transactSQLInjection(String sql) {
		return sql.replaceAll(".*([';]+|(--)+).*", " ");
	}

}
