package com.wh.gaea.control;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;

import org.json.JSONArray;
import org.json.JSONObject;

public class MenuItemInfo {
	public String title;
	public Icon icon;
	public String[] parents;

	@Override
	public String toString() {
		return title;
	}

	public JSONObject toJson() {
		JSONObject data = new JSONObject();
		data.put("title", title);
		data.put("parents", parents == null ? null : new JSONArray(Arrays.asList(parents)));
		return data;
	}

	public void fromJson(JSONObject data) {
		if (data.has("title"))
			title = data.getString("title");
		if (data.has("parents")) {
			JSONArray datas = data.getJSONArray("parents");
			parents = datas.toList().toArray(new String[datas.length()]);
		}
	}
	
	public interface IGetTreeKey<T>{
		String getKey(T node);
		T getParent(T node);
		boolean checkRoot(T node);
	}
	
	public static <T> String getKey(T node, IGetTreeKey<T> getTreeKey) {
		T parent = getTreeKey.getParent(node);
		String title = getTreeKey.getKey(node);
		if (parent != null && !getTreeKey.checkRoot(parent))
			return getKey(parent, getTreeKey) + title;
		else {
			return title;
		}
	}

	public static Map<String, MenuItemInfo> jsonarrayToMap(JSONArray data) {
		Map<String, MenuItemInfo> infoMap = new HashMap<>();
		for (Object object : data) {
			MenuItemInfo info = new MenuItemInfo();
			info.fromJson((JSONObject) object);
			String key = "";
			if (info.parents != null) {
				for (String keyString : info.parents) {
					key += keyString;
				}
			}
			infoMap.put(key + info.title, info);
		}

		return infoMap;
	}

}