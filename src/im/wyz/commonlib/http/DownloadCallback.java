package im.wyz.commonlib.http;

/**
 * 下载文件的回调
 * 
 * @author wyz
 * 
 */
public interface DownloadCallback {

	/**
	 * 开始下载
	 */
	public void onDownloadStart();

	/**
	 * 下载成功
	 */
	public void onDownloadSuccess();

	/**
	 * 下载失败
	 * 
	 * @param errorString
	 */
	public void onDownloadFailed(Exception errorString);

	/**
	 * 下载进度更新
	 * 
	 * @param progress
	 */
	public void onDownloadUpdate(int progress);

}
