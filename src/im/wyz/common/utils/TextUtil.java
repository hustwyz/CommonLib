package im.wyz.common.utils;

public class TextUtil {

	/**
	 * join string array with string delimiter
	 * 
	 * @param delimiter
	 * @param tokens
	 * @return
	 */
	public static String implode(CharSequence delimiter, CharSequence[] tokens) {
		StringBuilder sb = new StringBuilder();
		boolean firstTime = true;
		for (CharSequence token : tokens) {
			if (firstTime) {
				firstTime = false;
			} else {
				sb.append(delimiter);
			}
			sb.append(token);
		}
		return sb.toString();
	}

	/**
	 * return true if the str is null or the length of str is 0
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(CharSequence str) {
		if (str == null || str.length() == 0)
			return true;
		else
			return false;
	}

}
