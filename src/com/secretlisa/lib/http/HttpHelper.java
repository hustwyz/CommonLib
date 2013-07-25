package com.secretlisa.lib.http;

import java.io.FileNotFoundException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.http.conn.ConnectTimeoutException;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.secretlisa.lib.R;
import com.secretlisa.lib.http.HttpRequest.DownloadListener;
import com.secretlisa.lib.utils.CommonUtil;
import com.secretlisa.lib.utils.Log;

/**
 * 网络请求的工具类
 * 
 * @author wyz
 * 
 */
public class HttpHelper {

	protected static final int MSG_START = 1;
	protected static final int MSG_SUCCESS = 2;
	protected static final int MSG_UPDATE = 3;
	protected static final int MSG_FAIL = 4;
	
	protected Log log = CommonUtil.getLog(getClass());
	protected Context mContext;
	protected HttpRequest mHttp;
	
	public HttpHelper(Context context){
		this.mContext = context;
		mHttp = new HttpRequest(mContext);
	}
	
	/**
	 * 添加请求头部
	 * 
	 * @param key
	 * @param value
	 */
	public void addHead(String key, String value) {
		mHttp.addHead(key, value);
	}
	
	/**
	 * 清除某一个头部
	 * 
	 * @param key
	 */
	public void removeHead(String key) {
		mHttp.removeHead(key);
	}

	/**
	 * 清除所有请求头部
	 */
	public void removeAllHead() {
		mHttp.removeAllHead();
	} 
	
	public void cancel() {
		mHttp.cancel();
	}
	
	public boolean isCancelled(){
		return mHttp.isCancelled();
	}
	
	/**
	 * POST请求
	 * 
	 * @param url
	 * @param params
	 * @param mHttpCallback
	 */
	public void post(final String url, final Bundle params,
			final HttpCallback httpCallback) {
		request(url, params, httpCallback, "POST", null);
	}

	/**
	 * GET请求
	 * 
	 * @param url
	 * @param params
	 * @param mHttpCallback
	 */
	public void get(final String url, final Bundle params,
			final HttpCallback httpCallback) {
		request(url, params, httpCallback, "GET", null);
	}

	/**
	 * 上传文件
	 * 
	 * @param url
	 * @param params
	 * @param mHttpCallback
	 * @param mFileItem
	 */
	public void upload(final String url, final Bundle params,
			final HttpCallback httpCallback, final List<FileItem> fileItems) {
		request(url, params, httpCallback, "POST", fileItems);
	}

	private void request(final String url, final Bundle params,
			final HttpCallback httpCallback, final String method,
			final List<FileItem> fileItems) {
		final Handler httpHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MSG_START:
					if (httpCallback != null) {
						httpCallback.onHttpStart();
					}
					break;
				case MSG_SUCCESS:
					if (httpCallback != null) {
						httpCallback.onHttpSuccess((String) (msg.obj));
					}
					break;
				case MSG_FAIL:
					if (httpCallback != null) {
						httpCallback
								.onHttpFailed((SecretLisaException) msg.obj);
					}
					break;
				}
			}
		};
		new Thread() {
			@Override
			public void run() {
				Response res = null;
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
					res = mHttp.httpRequest(url, method, params, fileItems);
					Message msgSuccess = httpHandler.obtainMessage(MSG_SUCCESS,
							res.getString());
					httpHandler.sendMessage(msgSuccess);
				} catch (UnknownHostException e) {
					log.d(e.getClass().getName() + ":" + e.getMessage());
					Message msgFailed = httpHandler.obtainMessage(
							MSG_FAIL,
							new SecretLisaException(mContext
									.getString(R.string.http_error_unknowhost),
									SecretLisaException.NETWORK_UNKNOWNHOST));
					httpHandler.sendMessage(msgFailed);
				} catch (SocketTimeoutException e) {
					log.d(e.getClass().getName() + ":" + e.getMessage());
					Message msgFailed = httpHandler
							.obtainMessage(
									MSG_FAIL,
									new SecretLisaException(
											mContext.getString(R.string.http_error_bad_network),
											SecretLisaException.NETWORK_BAD));
					httpHandler.sendMessage(msgFailed);
				} catch (ConnectTimeoutException e) {
					log.d(e.getClass().getName() + ":" + e.getMessage());
					Message msgFailed = httpHandler
							.obtainMessage(
									MSG_FAIL,
									new SecretLisaException(
											mContext.getString(R.string.http_error_bad_network),
											SecretLisaException.NETWORK_BAD));
					httpHandler.sendMessage(msgFailed);
				} catch (SocketException e) {
					log.d(e.getClass().getName() + ":" + e.getMessage());
					Message msgFailed = httpHandler
							.obtainMessage(
									MSG_FAIL,
									new SecretLisaException(
											mContext.getString(R.string.http_error_error_netowrk),
											SecretLisaException.NETWORK_ERROR));
					httpHandler.sendMessage(msgFailed);
				} catch (FileNotFoundException e) {
					log.d(e.getClass().getName() + ":" + e.getMessage());
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
					log.d(e.getClass().getName() + ":" + e.getMessage());
					Message msgFailed = httpHandler.obtainMessage(
							MSG_FAIL,
							new SecretLisaException(mContext
									.getString(R.string.http_error_unknown),
									SecretLisaException.NETWORK_UNKNOWN));
					httpHandler.sendMessage(msgFailed);
				} finally {
					try {
						if (res != null)
							res.disconnect();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}.start();
	}
	
	/**
	 * 下载文件
	 * 
	 * @param url
	 *            下载链接
	 * @param params
	 *            请求参数
	 * @param mDownloadCallback
	 *            回调
	 * @param filePath
	 *            下载路径
	 * @param fileName
	 *            保存的文件名称
	 */
	public void download(final String url, final Bundle params,
			final DownloadCallback downloadCallback, final String filePath,
			final String fileName) {
		final Handler httpHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MSG_START:
					if (downloadCallback != null) {
						downloadCallback.onDownloadStart();
					}
					break;
				case MSG_SUCCESS:
					if (downloadCallback != null) {
						downloadCallback.onDownloadSuccess();
					}
					break;
				case MSG_FAIL:
					if (downloadCallback != null) {
						downloadCallback.onDownloadFailed((Exception) msg.obj);
					}
					break;
				case MSG_UPDATE:
					if (downloadCallback != null) {
						downloadCallback.onDownloadUpdate((Integer) msg.obj);
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
					boolean success = mHttp.download(url, params, new DownloadListener(){

						@Override
						public void onDownloadUpdate(int progress) {
							Message msg = new Message();
							msg.obj = progress;
							msg.what = MSG_UPDATE;
							httpHandler.sendMessage(msg);
						}
						
					}, filePath, fileName);
					if (success) {
						Message msgSuccess = httpHandler.obtainMessage(
								MSG_SUCCESS, null);
						httpHandler.sendMessage(msgSuccess);
					} else {
						Message msgSuccess = httpHandler
								.obtainMessage(
										MSG_FAIL,
										new SecretLisaException(
												mContext.getString(R.string.http_error_unknown),
												SecretLisaException.NETWORK_UNKNOWN));
						httpHandler.sendMessage(msgSuccess);
					}
				} catch (UnknownHostException e) {
					log.d(e.getClass().getName() + ":" + e.getMessage());
					Message msgFailed = httpHandler.obtainMessage(
							MSG_FAIL,
							new SecretLisaException(mContext
									.getString(R.string.http_error_unknowhost),
									SecretLisaException.NETWORK_UNKNOWNHOST));
					httpHandler.sendMessage(msgFailed);
				} catch (SocketTimeoutException e) {
					log.d(e.getClass().getName() + ":" + e.getMessage());
					if (isCancelled())
						return;
					Message msgFailed = httpHandler
							.obtainMessage(
									MSG_FAIL,
									new SecretLisaException(
											mContext.getString(R.string.http_error_bad_network),
											SecretLisaException.NETWORK_BAD));
					httpHandler.sendMessage(msgFailed);
				} catch (ConnectTimeoutException e) {
					log.d(e.getClass().getName() + ":" + e.getMessage());
					if (isCancelled())
						return;
					Message msgFailed = httpHandler
							.obtainMessage(
									MSG_FAIL,
									new SecretLisaException(
											mContext.getString(R.string.http_error_bad_network),
											SecretLisaException.NETWORK_BAD));
					httpHandler.sendMessage(msgFailed);
				} catch (SocketException e) {
					log.d(e.getClass().getName() + ":" + e.getMessage());
					if (isCancelled())
						return;
					Message msgFailed = httpHandler
							.obtainMessage(
									MSG_FAIL,
									new SecretLisaException(
											mContext.getString(R.string.http_error_error_netowrk),
											SecretLisaException.NETWORK_ERROR));
					httpHandler.sendMessage(msgFailed);
				} catch (FileNotFoundException e) {
					log.d(e.getClass().getName() + ":" + e.getMessage());
					if (isCancelled())
						return;
					Message msgFailed = httpHandler
							.obtainMessage(
									MSG_FAIL,
									new SecretLisaException(
											mContext.getString(R.string.http_error_error_bad_url),
											SecretLisaException.NETWORK_BAD_URL));
					httpHandler.sendMessage(msgFailed);
				} catch (SecretLisaException e) {
					if (isCancelled())
						return;
					Message msgFailed = httpHandler.obtainMessage(MSG_FAIL, e);
					httpHandler.sendMessage(msgFailed);
				} catch (Exception e) {
					log.d(e.getClass().getName() + ":" + e.getMessage());
					if (isCancelled())
						return;
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

}
