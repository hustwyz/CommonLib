package im.wyz.common.http;

/**
 * the download callback
 * 
 * @author wyz
 * 
 */
public interface DownloadCallback {

	/**
	 * download start
	 */
	public void onDownloadStart();

	/**
	 * download success
	 */
	public void onDownloadSuccess();

	/**
	 * download failed
	 * 
	 * @param errorString
	 */
	public void onDownloadFailed(String errorString);

	/**
	 * update progress
	 * 
	 * @param progress
	 */
	public void onDownloadUpdate(int progress);

}
