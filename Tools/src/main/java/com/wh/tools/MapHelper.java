package com.wh.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapHelper {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T extends Map<String, E>, E> Collection<E> queryMap(T sourceMap, Collection<Object> ids,
			boolean include) throws Exception {
		T map = (T) new HashMap(sourceMap);
		if (!include) {
			for (Object obj : ids) {
				String id = obj.toString();
				if (!map.containsKey(id))
					map.remove(id);
			}
			return map.values();
		} else {
			List<E> result = new ArrayList<>();
			for (Object obj : ids) {
				String id = obj.toString();
				if (map.containsKey(id))
					result.add(map.get(id));
			}
			return result;
		}
	}

	public interface ICreateMapValue<V>{
		V instance();
	}

	public static <T, V> V getMapValue(Map<T, V> map, T key, ICreateMapValue<V> createMapValue){
		V v = map.get(key);
		if (v == null){
			synchronized (map){
				v = map.get(key);
				if (v == null){
					v = createMapValue.instance();
					map.put(key, v);
				}
			}
		}
		return v;
	}
}
