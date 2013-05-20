package im.wyz.common.http;

public class SecretLisaException extends Exception {

	private static final long serialVersionUID = -5278868721442817956L;

	// 没有网络连接
	public static final int NETWORK_NO = 1001;

	// 无法解析域名
	public static final int NETWORK_UNKNOWNHOST = 1002;

	// 网速不给力
	public static final int NETWORK_BAD = 1003;

	// 服务器异常
	public static final int NETWORK_SERVER_ERROR = 1004;

	// 网络故障
	public static final int NETWORK_ERROR = 1005;

	// 未知错误
	public static final int NETWORK_UNKNOWN = 1006;

	// 未知的连接
	public static final int NETWORK_BAD_URL = 1007;

	// 文件找不到
	public static final int NETWORK_BAD_FILE = 1008;

	// 异常码
	private int errorCode = -1;

	public SecretLisaException() {
		super();
	}

	public SecretLisaException(String detailMessage, Exception e) {
		super(detailMessage, e);
	}

	public SecretLisaException(String detailMessage) {
		super(detailMessage);
	}

	public SecretLisaException(String detailMessage, int errorCode) {
		super(detailMessage);
		this.errorCode = errorCode;
	}

	public SecretLisaException(String detailMessage, Exception e, int errorCode) {
		super(detailMessage, e);
		this.errorCode = errorCode;
	}

	public SecretLisaException(Exception e) {
		super(e);
	}

	public int getStatusCode() {
		return errorCode;
	}

}
