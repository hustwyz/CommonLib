package im.wyz.common.http;

/**
 * http request callback
 * 
 * @author wyz
 * 
 */
public interface HttpCallback {

	/**
	 * http request starting
	 */
	public void onHttpStart();

	/**
	 * http request success
	 * 
	 * @param res
	 */
	public void onHttpSuccess(String res);

	/**
	 * http request failed
	 * 
	 * @param exception
	 */
	public void onHttpFailed(SecretLisaException exception);

}
