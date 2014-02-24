package com.secretlisa.lib;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.secretlisa.lib.utils.CommonUtil;
import com.secretlisa.lib.utils.Log;
import com.umeng.analytics.MobclickAgent;

public class CommonBaseFragmentActivity extends FragmentActivity {

	protected Log log = CommonUtil.getLog(getClass());

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
		if(CommonConfig.UMENG)
			MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		log.i("======onPause======");
		if(CommonConfig.UMENG)
			MobclickAgent.onPause(this);
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

//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
//			CommonUtil.finishActivity(this);
//		}
//		return super.onKeyDown(keyCode, event);
//	}

}
