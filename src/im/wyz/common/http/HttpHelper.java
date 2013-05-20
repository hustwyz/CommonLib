package im.wyz.common.http;

import im.wyz.common.Config;
import im.wyz.common.R;
import im.wyz.common.utils.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.http.conn.ConnectTimeoutException;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * the utils to send a http request
 * 
 * @author wyz
 * 
 */
public class HttpHelper {
	
	private static final String TAG = "HttpHelper";
	
	public static final int MSG_START = 1;
	public static final int MSG_SUCCESS = 2;
	public static final int MSG_UPDATE = 3;
	public static final int MSG_FAIL = 4;

	Context mContext;

	HttpRequest mHttp;

	public HttpHelper(Context context) {
		this.mContext = context.getApplicationContext();
		mHttp = new HttpRequest(mContext);
	}

	/**
	 * add http head
	 * 
	 * @param key
	 * @param value
	 */
	public void addHead(String key, String value) {
		mHttp.addHead(key, value);
	}

	/**
	 * http POST request
	 * 
	 * @param url
	 * @param params
	 * @param mHttpCallback
	 */
	public void post(final String url, final Bundle params,
			final HttpCallback mHttpCallback) {
		request(url, params, mHttpCallback, "PSOT", null);
	}

	/**
	 * http GET request
	 * 
	 * @param url
	 * @param params
	 * @param mHttpCallback
	 */
	public void get(final String url, final Bundle params,
			final HttpCallback mHttpCallback) {
		request(url, params, mHttpCallback, "GET", null);
	}

	/**
	 * upload file
	 * 
	 * @param url
	 * @param params
	 * @param mHttpCallback
	 * @param mFileItem
	 */
	public void upload(final String url, final Bundle params,
			final HttpCallback mHttpCallback, final List<FileItem> mFileItems) {
		request(url, params, mHttpCallback, "POST", mFileItems);
	}

	/**
	 * download file
	 * 
	 * @param url
	 * @param params
	 * @param mDownloadCallback
	 * @param filePath
	 * @param fileName
	 */
	public void download(final String url, final Bundle params,
			final DownloadCallback mDownloadCallback, final String filePath,
			final String fileName) {
		final Handler httpHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MSG_START:
					if (mDownloadCallback != null) {
						mDownloadCallback.onDownloadStart();
					}
					break;
				case MSG_SUCCESS:
					if (mDownloadCallback != null) {
						mDownloadCallback.onDownloadSuccess();
					}
					break;
				case MSG_FAIL:
					if (mDownloadCallback != null) {
						mDownloadCallback.onDownloadFailed((String) msg.obj);
					}
					break;
				case MSG_UPDATE:
					if (mDownloadCallback != null) {
						mDownloadCallback.onDownloadUpdate((Integer) msg.obj);
					}
					break;
				}
			}
		};
		new Thread() {
			@Override
			public void run() {
				try {
					if (!NetworkUtil.isNetworkAvailable(mContext)) {
						Message msg = httpHandler
								.obtainMessage(
										MSG_FAIL,
										new SecretLisaException(
												mContext.getString(R.string.http_error_nonetwork),
												SecretLisaException.NETWORK_NO));
						httpHandler.sendMessage(msg);
						return;
					}
					Message msgStart = httpHandler.obtainMessage(MSG_START);
					httpHandler.sendMessage(msgStart);
					Response res = mHttp.httpRequest(url, "GET", params, null);
					HttpURLConnection conn = res.getCon();
					int contentLength = conn.getContentLength();
					write2File(httpHandler, filePath, fileName,
							res.getInputStream(), contentLength);
					conn.disconnect();
					Message msgSuccess = httpHandler.obtainMessage(MSG_SUCCESS,
							null);
					httpHandler.sendMessage(msgSuccess);
				} catch (UnknownHostException e) {
					if(Config.DEBUG){
						Log.e(TAG, Util.getErrorString(e));
					}
					Message msgFailed = httpHandler.obtainMessage(
							MSG_FAIL,
							new SecretLisaException(mContext
									.getString(R.string.http_error_unknowhost),
									SecretLisaException.NETWORK_UNKNOWNHOST));
					httpHandler.sendMessage(msgFailed);
				} catch (SocketTimeoutException e) {
					if(Config.DEBUG){
						Log.e(TAG, Util.getErrorString(e));
					}
					Message msgFailed = httpHandler
							.obtainMessage(
									MSG_FAIL,
									new SecretLisaException(
											mContext.getString(R.string.http_error_bad_network),
											SecretLisaException.NETWORK_BAD));
					httpHandler.sendMessage(msgFailed);
				} catch (ConnectTimeoutException e) {
					if(Config.DEBUG){
						Log.e(TAG, Util.getErrorString(e));
					}
					Message msgFailed = httpHandler
							.obtainMessage(
									MSG_FAIL,
									new SecretLisaException(
											mContext.getString(R.string.http_error_bad_network),
											SecretLisaException.NETWORK_BAD));
					httpHandler.sendMessage(msgFailed);
				} catch (SocketException e) {
					if(Config.DEBUG){
						Log.e(TAG, Util.getErrorString(e));
					}
					Message msgFailed = httpHandler
							.obtainMessage(
									MSG_FAIL,
									new SecretLisaException(
											mContext.getString(R.string.http_error_error_netowrk),
											SecretLisaException.NETWORK_ERROR));
					httpHandler.sendMessage(msgFailed);
				} catch (FileNotFoundException e) {
					if(Config.DEBUG){
						Log.e(TAG, Util.getErrorString(e));
					}
					Message msgFailed = httpHandler
							.obtainMessage(
									MSG_FAIL,
									new SecretLisaException(
											mContext.getString(R.string.http_error_error_bad_url),
											SecretLisaException.NETWORK_BAD_URL));
					httpHandler.sendMessage(msgFailed);
				} catch (SecretLisaException e) {
					Message msgFailed = httpHandler.obtainMessage(MSG_FAIL, e);
					httpHandler.sendMessage(msgFailed);
				} catch (Exception e) {
					if(Config.DEBUG){
						Log.e(TAG, Util.getErrorString(e));
					}
					Message msgFailed = httpHandler.obtainMessage(
							MSG_FAIL,
							new SecretLisaException(mContext
									.getString(R.string.http_error_unknown),
									SecretLisaException.NETWORK_UNKNOWN));
					httpHandler.sendMessage(msgFailed);
				}
			}

		}.start();
	}

	private void request(final String url, final Bundle params,
			final HttpCallback mHttpCallback, final String method,
			final List<FileItem> mFileItems) {
		final Handler httpHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MSG_START:
					if (mHttpCallback != null) {
						mHttpCallback.onHttpStart();
					}
					break;
				case MSG_SUCCESS:
					if (mHttpCallback != null) {
						mHttpCallback.onHttpSuccess((String) (msg.obj));
					}
					break;
				case MSG_FAIL:
					if (mHttpCallback != null) {
						mHttpCallback
								.onHttpFailed((SecretLisaException) msg.obj);
					}
					break;
				}
			}
		};
		new Thread() {
			@Override
			public void run() {
				try {
					if (!NetworkUtil.isNetworkAvailable(mContext)) {
						Message msg = httpHandler
								.obtainMessage(
										MSG_FAIL,
										new SecretLisaException(
												mContext.getString(R.string.http_error_nonetwork),
												SecretLisaException.NETWORK_NO));
						httpHandler.sendMessage(msg);
						return;
					}
					Message msgStart = httpHandler.obtainMessage(MSG_START);
					httpHandler.sendMessage(msgStart);
					Response res = mHttp.httpRequest(url, method, params,
							mFileItems);
					Message msgSuccess = httpHandler.obtainMessage(MSG_SUCCESS,
							res.getString());
					httpHandler.sendMessage(msgSuccess);
				} catch (UnknownHostException e) {
					if(Config.DEBUG){
						Log.e(TAG, Util.getErrorString(e));
					}
					Message msgFailed = httpHandler.obtainMessage(
							MSG_FAIL,
							new SecretLisaException(mContext
									.getString(R.string.http_error_unknowhost),
									SecretLisaException.NETWORK_UNKNOWNHOST));
					httpHandler.sendMessage(msgFailed);
				} catch (SocketTimeoutException e) {
					if(Config.DEBUG){
						Log.e(TAG, Util.getErrorString(e));
					}
					Message msgFailed = httpHandler
							.obtainMessage(
									MSG_FAIL,
									new SecretLisaException(
											mContext.getString(R.string.http_error_bad_network),
											SecretLisaException.NETWORK_BAD));
					httpHandler.sendMessage(msgFailed);
				} catch (ConnectTimeoutException e) {
					if(Config.DEBUG){
						Log.e(TAG, Util.getErrorString(e));
					}
					Message msgFailed = httpHandler
							.obtainMessage(
									MSG_FAIL,
									new SecretLisaException(
											mContext.getString(R.string.http_error_bad_network),
											SecretLisaException.NETWORK_BAD));
					httpHandler.sendMessage(msgFailed);
				} catch (SocketException e) {
					if(Config.DEBUG){
						Log.e(TAG, Util.getErrorString(e));
					}
					Message msgFailed = httpHandler
							.obtainMessage(
									MSG_FAIL,
									new SecretLisaException(
											mContext.getString(R.string.http_error_error_netowrk),
											SecretLisaException.NETWORK_ERROR));
					httpHandler.sendMessage(msgFailed);
				} catch (FileNotFoundException e) {
					if(Config.DEBUG){
						Log.e(TAG, Util.getErrorString(e));
					}
					Message msgFailed = httpHandler
							.obtainMessage(
									MSG_FAIL,
									new SecretLisaException(
											mContext.getString(R.string.http_error_error_bad_url),
											SecretLisaException.NETWORK_BAD_URL));
					httpHandler.sendMessage(msgFailed);
				} catch (SecretLisaException e) {
					Message msgFailed = httpHandler.obtainMessage(MSG_FAIL, e);
					httpHandler.sendMessage(msgFailed);
				} catch (Exception e) {
					if(Config.DEBUG){
						Log.e(TAG, Util.getErrorString(e));
					}
					Message msgFailed = httpHandler.obtainMessage(
							MSG_FAIL,
							new SecretLisaException(mContext
									.getString(R.string.http_error_unknown),
									SecretLisaException.NETWORK_UNKNOWN));
					httpHandler.sendMessage(msgFailed);
				}
			}

		}.start();
	}

	/**
	 * write data into file
	 * 
	 * @param handler
	 * @param path
	 * @param fileName
	 * @param is
	 * @param contentLength
	 * @throws IOException
	 */
	private void write2File(final Handler handler, String path,
			String fileName, InputStream is, int contentLength)
			throws IOException {
		int s = 0;
		File file = null;
		OutputStream output = null;
		if (!path.endsWith(File.separator)) {
			path = path + File.separator;
		}
		File folder = new File(path);
		if (!folder.exists() || folder.isFile()) {
			folder.mkdirs();
		}
		file = new File(path + fileName);
		if (!file.exists())
			file.createNewFile();
		output = new FileOutputStream(file);
		byte buffer[] = new byte[4096];
		long lastUpdate = System.currentTimeMillis();
		int total = 0;
		while ((s = is.read(buffer)) != -1) {
			output.write(buffer, 0, s);
			total += s;
			if (System.currentTimeMillis() - lastUpdate > 200) {
				Message msg = new Message();
				msg.obj = total * 100 / contentLength;
				msg.what = MSG_UPDATE;
				handler.sendMessage(msg);
				lastUpdate = System.currentTimeMillis();
			}
		}
		output.flush();
		output.close();
	}
}
