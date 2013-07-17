package im.wyz.commonlib.utils;

import android.os.Handler;
import android.os.Message;

/**
 * abstract async task
 * 
 * @author wyz
 * 
 * @param <Result>
 * @param <Progress>
 */
public abstract class BaseTask<Result, Progress> {

	private static final int MSG_POST = 2;

	private static final int MSG_UPDATE = 3;

	private static final int MSG_CANCELED = 4;

	public static enum Status {
		FINISHED, RUNNING, PENDDING, CANCELED
	}

	private Status mStatus = Status.PENDDING;

	public final Status getStatus() {
		return mStatus;
	}

	public final boolean isRunning() {
		return mStatus == Status.RUNNING;
	}

	/**
	 * 异步方法，需要子类实现
	 * 
	 * @return
	 */
	protected abstract Result doInBackground();

	private final Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_UPDATE:
				onUpdate((Progress) msg.obj);
				break;
			case MSG_POST:
				onPostTask((Result) msg.obj);
				break;
			case MSG_CANCELED:
				onCanceled();
				break;
			}
			super.handleMessage(msg);
		}

	};

	public final void publishProgress(Progress progress) {
		Message msg = new Message();
		msg.what = MSG_UPDATE;
		msg.obj = progress;
		handler.sendMessage(msg);
	}

	/**
	 * 任务是否被取消了
	 * 
	 * @return
	 */
	public final boolean isCanceled() {
		return mStatus == Status.CANCELED;
	}

	/**
	 * 取消任务
	 */
	public final void cancelTask() {
		if (!isRunning())
			return;
		mStatus = Status.CANCELED;
		Message msg = new Message();
		msg.what = MSG_CANCELED;
		handler.sendMessage(msg);
	}

	private void onAfterTask(Result result) {
		Message msg = new Message();
		msg.what = MSG_POST;
		msg.obj = result;
		handler.sendMessage(msg);
	}

	/**
	 * 任务取消后的回调，需要子类实现
	 */
	protected void onCanceled() {
	}

	/**
	 * 任务异步更新进度的回调
	 * 
	 * @param progress
	 */
	protected void onUpdate(Progress progress) {
	}

	/**
	 * 任务完成后的回调
	 * 
	 * @param result
	 */
	protected void onPostTask(Result result) {
	}

	/**
	 * 任务执行前的回调
	 */
	protected void onPreTask() {
	}

	/**
	 * 执行任务
	 */
	public final void execute() {
		if (isRunning())
			return;
		onPreTask();
		new Thread() {
			public void run() {
				mStatus = Status.RUNNING;
				onAfterTask(doInBackground());
				mStatus = Status.FINISHED;
			}
		}.start();
	}

}
