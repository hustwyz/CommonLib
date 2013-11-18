package com.secretlisa.lib;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.secretlisa.lib.utils.CommonUtil;
import com.secretlisa.lib.utils.Log;

public class CommonBaseFragment extends Fragment{
	
	protected Log log = CommonUtil.getLog(getClass());

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		log.i("======onActivityCreated======");
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		log.i("======onAttach======");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		log.i("======onCreate======");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		log.i("======onDestroy======");
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		log.i("======onDestroyView======");
	}

	@Override
	public void onPause() {
		super.onPause();
		log.i("======onPause======");
	}

	@Override
	public void onStop() {
		super.onStop();
		log.i("======onStop======");
	}

}
