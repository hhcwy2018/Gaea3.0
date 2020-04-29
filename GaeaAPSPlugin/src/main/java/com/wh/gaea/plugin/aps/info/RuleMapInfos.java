package com.wh.gaea.plugin.aps.info;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class RuleMapInfos extends ArrayList<RuleMapInfo>{
	
	private static final long serialVersionUID = 1L;

	public RuleMapInfos() {}
	public RuleMapInfos(JSONArray data) {
		fromJson(data);
	}
	
	public JSONArray toJson() {
		JSONArray data = new JSONArray();
		for (RuleMapInfo info : this) {
			data.put(info.toJson());
		}
		return data;
	}
	
	public void fromJson(JSONArray data) {
		clear();
		for (Object object : data) {
			RuleMapInfo info = new RuleMapInfo();
			info.fromJson((JSONObject) object);
			add(info);
		}
	}
}
