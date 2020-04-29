package com.wh.gaea.industry.interfaces;

import org.json.JSONObject;

public class DynamicGroupInfo {
	public String id;
	public String code;
	public String name;

	public DynamicGroupInfo() {}
	
	public DynamicGroupInfo(JSONObject data) {
		fromJson(data);
	}
	
	@Override
	public String toString() {
		return name;
	}

	public JSONObject toJson() {
		JSONObject data = new JSONObject();
		data.put("id", id);
		data.put("code", code);
		data.put("name", name);
		return data;
	}
	
	public void fromJson(JSONObject data) {
		if (data.has("id"))
			id = data.getString("id");
		if (data.has("code"))
			code = data.getString("code");
		if (data.has("name"))
			name = data.getString("name");
	}
}
