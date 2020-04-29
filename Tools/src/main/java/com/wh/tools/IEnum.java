package com.wh.tools;

import java.lang.reflect.Array;
import java.util.EnumSet;
import java.util.Iterator;

public interface IEnum{
	int getCode();
	String getMsg();
	
	@SuppressWarnings("unchecked")
	public static <T extends IEnum> T[] msg(T[] rules) {
		if (rules == null || rules.length == 0)
			return null;

		T[] datas = (T[]) Array.newInstance(rules[0].getClass(), rules.length);

		for (int i = 0; i < rules.length; i++) {
			datas[i] = rules[i];
		}

		return datas;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> T[] msgs(Class<T> c) {
		EnumSet<T> all = EnumSet.allOf((Class<T>) c);
		Object result = Array.newInstance(c, all.size());
		Iterator<T> iterator = all.iterator();
		for (int i = 0; i < all.size(); i++) {
			Array.set(result, i, iterator.next());
		}

		return (T[]) result;
	}
}
