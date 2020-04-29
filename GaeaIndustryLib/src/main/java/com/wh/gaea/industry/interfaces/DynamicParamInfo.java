package com.wh.gaea.industry.interfaces;

import org.json.JSONObject;

public class DynamicParamInfo {
	public String id;
	public String code;
	public String name;
	public String group;
	public int precision = 2;
	public Object max;
	public Object min;
	public String dateType;
	public int size;
	public boolean must = false;
	public String defaultValue;
	public String values;
	public String valueList;

	public DynamicParamInfo() {
	}

	public DynamicParamInfo(JSONObject data) {
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
		data.put("group", group);
		data.put("precision", precision);
		data.put("max", max);
		data.put("min", min);
		data.put("dateType", dateType);
		data.put("size", size);
		data.put("must", must);
		data.put("defaultValue", defaultValue);
		data.put("values", values);
		data.put("valueList", valueList);
		return data;
	}

	public void fromJson(JSONObject data) {
		if (data.has("id"))
			id = data.getString("id");
		if (data.has("code"))
			code = data.getString("code");
		if (data.has("name"))
			name = data.getString("name");
		if (data.has("group"))
			group = data.getString("group");
		if (data.has("precision"))
			precision = data.getInt("precision");
		if (data.has("max"))
			max = data.get("max");
		if (data.has("min"))
			min = data.get("min");
		if (data.has("size"))
			size = data.getInt("size");
		if (data.has("dateType"))
			dateType = data.getString("dateType");
		if (data.has("must"))
			must = data.getBoolean("must");
		if (data.has("defaultValue"))
			defaultValue = data.getString("defaultValue");
		if (data.has("values"))
			values = data.getString("values");
		if (data.has("valueList"))
			valueList = data.getString("valueList");
	}

}