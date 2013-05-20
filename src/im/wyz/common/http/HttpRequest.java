package im.wyz.common.http;

import im.wyz.common.Config;
import im.wyz.common.R;
import im.wyz.common.utils.Util;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

/**
 * http request
 * 
 * @author wyz
 * 
 */
public class HttpRequest {

	private static final String TAG = "HttpRequest";

	public static final int OK = 200;

	public static final int SERVER_ERROR = 500;

	public static final int RETRYCOUNT = 1;

	public static final int CONNECT_TIMEOUT = 20000;

	public static final int READ_TIMEOUT = 20000;

	public Context context;

	/**
	 * http head
	 */
	private Bundle heads;

	public HttpRequest(Context context) {
		super();
		this.context = context;
		heads = new Bundle();
	}

	/**
	 * add http head
	 * 
	 * @param key
	 * @param value
	 */
	public void addHead(String key, String value) {
		if (value == null)
			return;
		String oldValue = heads.getString(key);
		heads.putString(key, oldValue == null ? value : oldValue + ";" + value);
	}

	/**
	 * http request
	 * 
	 * @param url
	 * @param method
	 * @param params
	 * @param files
	 * @return
	 * @throws Exception
	 */
	public Response httpRequest(String url, String method, Bundle params,
			List<FileItem> files) throws Exception {
		if (method.equals("GET") && params != null) {
			String data = Util.encodeUrl(params);
			if (!data.equals("")) {
				url = url + '?' + data;
			}
		}
		HttpURLConnection conn;
		conn = openURLConnect(url);
		if (Config.DEBUG) {
			Log.i(TAG, "URL=" + url);
			Log.i(TAG, "Method=" + method);
		}

		conn.setConnectTimeout(CONNECT_TIMEOUT);
		conn.setReadTimeout(READ_TIMEOUT);
		conn.setDoInput(true);
		// 添加头部
		if (heads != null) {
			for (Iterator<String> i = heads.keySet().iterator(); i.hasNext();) {
				String name = i.next();
				String value = heads.getString(name);
				conn.setRequestProperty(name, value);
				if (Config.DEBUG) {
					Log.i(TAG, "Head:name=" + name + ";value=" + value);
				}
			}
		}
		if (!method.equals("GET")) {
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			if (files == null) {
				conn.getOutputStream().write(
						Util.encodeUrl(params).getBytes("UTF-8"));
			} else {
				uploadFile(conn, params, files);
			}
		}
		Response res = new Response(conn);
		int statusCode = res.getStatusCode();
		if (statusCode == OK) {
			return res;
		} else if (statusCode >= HttpRequest.SERVER_ERROR) {
			throw new SecretLisaException(context.getString(
					R.string.http_error_server_error,
					SecretLisaException.NETWORK_SERVER_ERROR));
		} else {
			throw new SecretLisaException(context.getString(
					R.string.http_error_unknown,
					SecretLisaException.NETWORK_UNKNOWN));
		}

	}

	/**
	 * upload form
	 * 
	 * @param con
	 * @param parameters
	 * @param files
	 * @throws IOException
	 * @throws SecretLisaException
	 */
	private void uploadFile(HttpURLConnection con, Bundle parameters,
			List<FileItem> files) throws IOException, SecretLisaException {
		con.setRequestProperty("connection", "keep-alive");
		String boundary = "-----------------------------114975832116442893661388290519"; // 分隔符
		con.setRequestProperty("Content-Type", "multipart/form-data; boundary="
				+ boundary);
		boundary = "--" + boundary;

		// 上传普通参数
		StringBuffer params = new StringBuffer();
		if (parameters != null) {
			for (Iterator<String> iter = parameters.keySet().iterator(); iter
					.hasNext();) {
				String name = iter.next();
				String value = parameters.getString(name);
				params.append(boundary + "\r\n");
				params.append("Content-Disposition: form-data; name=\"" + name
						+ "\"\r\n\r\n");
				params.append(value);
				params.append("\r\n");
			}
		}
		byte[] ps = params.toString().getBytes();
		OutputStream os = con.getOutputStream();
		os.write(ps);

		// 上传文件
		for (FileItem file : files) {
			StringBuilder sb = new StringBuilder();
			sb.append(boundary).append("\r\n");
			sb.append("Content-Disposition: form-data; name=\""
					+ file.getParamName() + "\"; filename=\"" + file.getName()
					+ "\"\r\n");
			sb.append("Content-Type: " + file.getContentType() + "\r\n\r\n");
			byte[] fileDiv = sb.toString().getBytes();
			os.write(fileDiv);
			os.write(file.getBytes());
			os.write("\r\n".getBytes());
		}

		// 结束标志
		byte[] endData = ("\r\n" + boundary + "--\r\n").getBytes();
		os.write(endData);

		os.flush();
		os.close();
	}

	/**
	 * open http connection
	 * 
	 * @param url
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 */
	private HttpURLConnection openURLConnect(String url)
			throws MalformedURLException, IOException, KeyManagementException,
			NoSuchAlgorithmException {
		int netType = NetworkUtil.getAccessPointType(context);
		if (netType == NetworkUtil.APN_CMWAP) {
			SocketAddress sa = new InetSocketAddress(NetworkUtil.getHostIp(),
					NetworkUtil.getHostPort());
			Proxy proxy = new Proxy(java.net.Proxy.Type.HTTP, sa);
			if (url.toLowerCase().startsWith("https"))
				return openHttps(url, proxy);
			else
				return (HttpURLConnection) new URL(url).openConnection(proxy);
		} else {
			if (url.toLowerCase().startsWith("https"))
				return openHttps(url, null);
			else
				return (HttpURLConnection) new URL(url).openConnection();
		}
	}

	/**
	 * open https connection
	 * 
	 * @param url
	 * @param proxy
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws Exception
	 */
	private HttpsURLConnection openHttps(String url, Proxy proxy)
			throws NoSuchAlgorithmException, KeyManagementException,
			MalformedURLException, IOException {
		SSLContext sc = SSLContext.getInstance("TLS");
		sc.init(null, new TrustManager[] { new MyTrustManager() },
				new SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());
		if (proxy == null)
			return (HttpsURLConnection) new URL(url).openConnection();
		else
			return (HttpsURLConnection) new URL(url).openConnection(proxy);
	}

	private class MyHostnameVerifier implements HostnameVerifier {

		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}

	private class MyTrustManager implements X509TrustManager {

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	}

}
