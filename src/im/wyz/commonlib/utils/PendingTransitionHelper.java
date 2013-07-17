package im.wyz.commonlib.utils;

import android.app.Activity;

public class PendingTransitionHelper {

	public PendingTransitionHelper(Activity a, int i, int j) {
		a.overridePendingTransition(i, j);
	}

}
