package com.wh.tools;

public class StringHelper {
	public static String emptyString(String str1) {
		return (str1 == null || str1.trim().isEmpty()) ? null : str1;
	}

	public static String linkString(String ...strs) {
		String result = null;
		for (String str: strs) {
			String tmp = emptyString(str);
			if (tmp != null){
				if (result == null)
					result = tmp;
				else
					result += "," + tmp;
			}
		}
		return result;
	}
}
