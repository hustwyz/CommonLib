package im.wyz.common.http;

/**
 * HTTP请求的回调
 * 
 * @author wyz
 * 
 */
public interface HttpCallback {

	/**
	 * 开始发起HTTP请求
	 */
	public void onHttpStart();

	/**
	 * 请求成功
	 * 
	 * @param res
	 */
	public void onHttpSuccess(String res);

	/**
	 * 请求失败
	 * 
	 * @param exception
	 */
	public void onHttpFailed(SecretLisaException exception);

}
