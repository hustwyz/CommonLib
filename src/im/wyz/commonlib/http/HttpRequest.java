package im.wyz.commonlib.http;

import im.wyz.commonlib.R;
import im.wyz.commonlib.utils.FileUtil;
import im.wyz.commonlib.utils.Log;
import im.wyz.commonlib.utils.CommonUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import android.text.TextUtils;

/**
 * 网络请求的类
 * 
 * @author wyz
 * 
 */
public class HttpRequest {

	public static final int OK = 200;

	public static final int SERVER_ERROR = 500;

	public static final int RETRYCOUNT = 1;

	public static final int CONNECT_TIMEOUT = 20000;

	public static final int READ_TIMEOUT = 20000;

	public Context mContext;

	private Bundle heads;

	private boolean isCancelled = false;

	private Log log = CommonUtil.getLog(getClass());

	public HttpRequest(Context context) {
		super();
		this.mContext = context;
		heads = new Bundle();
	}

	/**
	 * 添加请求头部
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
	 * 清除某一个头部
	 * 
	 * @param key
	 */
	public void removeHead(String key) {
		heads.remove(key);
	}

	/**
	 * 清除所有请求头部
	 */
	public void removeAllHead() {
		heads.clear();
	}

	/**
	 * 取消请求
	 */
	public void cancel() {
		isCancelled = true;
	}

	/**
	 * 是否已取消
	 * 
	 * @return
	 */
	public boolean isCancelled() {
		return isCancelled;
	}

	/**
	 * 发起请求
	 * 
	 * @param url
	 *            请求地址
	 * @param method
	 *            请求的方法
	 * @param params
	 *            请求的参数
	 * @param files
	 * @return
	 * @throws Exception
	 */
	public Response httpRequest(String url, String method, Bundle params,
			List<FileItem> files) throws Exception {
		if (method.equals("GET") && params != null) {
			String data = CommonUtil.encodeUrl(params);
			if (!data.equals("")) {
				url = url + '?' + data;
			}
		}
		HttpURLConnection conn;
		conn = openURLConnect(url);
		log.d("URL:" + url);
		log.d("Method:" + method);
		conn.setConnectTimeout(CONNECT_TIMEOUT);
		conn.setReadTimeout(READ_TIMEOUT);
		conn.setDoInput(true);
		conn.setRequestMethod(method);
		// 添加头部
		if (heads != null) {
			for (Iterator<String> i = heads.keySet().iterator(); i.hasNext();) {
				String name = i.next();
				String value = heads.getString(name);
				conn.setRequestProperty(name, value);
				log.d("Head:name=" + name + ";value=" + value);
			}
		}
		if (method.equals("POST")) {
			conn.setDoOutput(true);
			if (files == null) {
				if (params != null)
					conn.getOutputStream().write(
							CommonUtil.encodeUrl(params).getBytes("UTF-8"));
			} else {
				uploadFile(conn, params, files);
			}
		}
		Response res = new Response(conn);
		int statusCode = res.getStatusCode();
		if (statusCode == OK) {
			return res;
		} else if (statusCode >= HttpRequest.SERVER_ERROR) {
			throw new SecretLisaException(mContext.getString(
					R.string.http_error_server_error,
					SecretLisaException.NETWORK_SERVER_ERROR));
		} else {
			throw new SecretLisaException(mContext.getString(
					R.string.http_error_unknown,
					SecretLisaException.NETWORK_UNKNOWN));
		}

	}

	/**
	 * 下载文件
	 * 
	 * @param url
	 *            下载地址
	 * @param params
	 *            请求参数
	 * @param downloadListener
	 *            下载回调
	 * @param filePath
	 *            文件下载的目录
	 * @param fileName
	 *            下载的文件名
	 * @return
	 * @throws Exception
	 */
	public boolean download(String url, Bundle params,
			DownloadListener downloadListener, final String filePath,
			final String fileName) throws Exception {
		isCancelled = false;
		Response response = httpRequest(url, "GET", params, null);
		HttpURLConnection conn = response.getCon();
		int contentLength = conn.getContentLength();
		return write2File(filePath, fileName, conn.getInputStream(),
				contentLength, downloadListener);
	}

	/**
	 * 上传表单
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
	 * 写入到文件中
	 * 
	 * @param handler
	 * @param path
	 * @param fileName
	 * @param is
	 * @param contentLength
	 * @throws IOException
	 */
	private boolean write2File(String path, String fileName, InputStream is,
			int contentLength, DownloadListener downloadListener) {
		if (isCancelled)
			return false;
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
		try {
			file = new File(path + fileName);
			if (!file.exists())
				file.createNewFile();
			output = new FileOutputStream(file);
			byte buffer[] = new byte[4096];
			long lastUpdate = System.currentTimeMillis();
			int total = 0;
			while ((s = is.read(buffer)) != -1) {
				if (isCancelled) {
					output.close();
					FileUtil.deleteFile(file);
					return false;
				}
				output.write(buffer, 0, s);
				total += s;
				if (System.currentTimeMillis() - lastUpdate > 200) {// 200毫秒回调一次
					if (downloadListener != null) {
						downloadListener.onDownloadUpdate(total * 100
								/ contentLength);
					}
					lastUpdate = System.currentTimeMillis();
				}
			}
			output.flush();
			output.close();
			is.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			if (output != null) {
				try {
					output.flush();
					output.close();
					is.close();
				} catch (Exception ee) {
				}
				;
			}
			FileUtil.deleteFile(file);
		}
		return false;
	}

	private HttpURLConnection openURLConnect(String url)
			throws MalformedURLException, IOException, KeyManagementException,
			NoSuchAlgorithmException {
		int netType = NetworkUtil.getNetworkType(mContext);
		if (netType == NetworkUtil.TYPE_NET
				&& !TextUtils.isEmpty(android.net.Proxy.getDefaultHost())) {
			SocketAddress sa = new InetSocketAddress(
					android.net.Proxy.getDefaultHost(),
					android.net.Proxy.getDefaultPort());
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
	 * 打开https的连接
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

	public interface DownloadListener {
		public void onDownloadUpdate(int progress);
	}

}
