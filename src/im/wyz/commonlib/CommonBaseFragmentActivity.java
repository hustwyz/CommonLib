package im.wyz.commonlib;

import im.wyz.commonlib.utils.CommonUtils;
import im.wyz.commonlib.utils.Log;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;

public class CommonBaseFragmentActivity extends FragmentActivity {

	Log log = CommonUtils.getLog(getClass());

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		log.i("======onCreate======");
	}

	@Override
	protected void onStart() {
		super.onStart();
		log.i("======onStart======");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		log.i("======onRestart======");
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		log.i("======onNewIntent======");
	}

	@Override
	protected void onResume() {
		super.onResume();
		log.i("======onResume======");
	}

	@Override
	protected void onPause() {
		super.onPause();
		log.i("======onPause======");
	}

	@Override
	protected void onStop() {
		super.onStop();
		log.i("======onStop======");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		log.i("======onDestroy======");
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			CommonUtils.finishActivity(this);
		}
		return super.onKeyDown(keyCode, event);
	}

}
