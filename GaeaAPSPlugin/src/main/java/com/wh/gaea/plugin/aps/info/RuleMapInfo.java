package com.wh.gaea.plugin.aps.info;

import org.json.JSONObject;

import com.wh.gaea.industry.interfaces.DynamicGroupInfo;
import com.wh.gaea.industry.interfaces.DynamicParamInfo;
import com.wh.gaea.plugin.aps.rule.RuleMapType;
import com.wh.gaea.plugin.aps.rule.connector.RuleTarget;

public class RuleMapInfo {
	public RuleTarget type = RuleTarget.ftMaterial;
	public DynamicGroupInfo item;
	public DynamicParamInfo attr;
	public RuleTarget dest_type;
	public DynamicGroupInfo dest_item;
	public DynamicParamInfo dest_attr;
	public int level = 0;

	public RuleMapType mapType = RuleMapType.mtEqual;
	
	public JSONObject toJson() {
		JSONObject data = new JSONObject();
		data.put("type", type.name());
		data.put("item", item.toJson());
		data.put("attr", attr.toJson());
		data.put("dest_type", dest_type.name());
		data.put("dest_item", dest_item.toJson());
		data.put("dest_attr", dest_attr.toJson());
		data.put("level", level);
		data.put("mapType", mapType.name());
		return data;
	}
	
	public void fromJson(JSONObject data) {
		try {
			if (data.has("type"))
				type = RuleTarget.valueOf(data.getString("type"));
			if (data.has("item"))
				item = new DynamicGroupInfo(data.getJSONObject("item"));
			if (data.has("attr"))
				attr = new DynamicParamInfo(data.getJSONObject("attr"));
			if (data.has("dest_type"))
				dest_type = RuleTarget.valueOf(data.getString("dest_type"));
			if (data.has("dest_item"))
				dest_item = new DynamicGroupInfo(data.getJSONObject("dest_item"));
			if (data.has("dest_attr"))
				dest_attr = new DynamicParamInfo(data.getJSONObject("dest_attr"));
			if (data.has("level"))
				level = data.getInt("level");
			if (data.has("mapType"))
				mapType = RuleMapType.valueOf(data.getString("mapType"));
		} catch (Exception e) {
		}
	}
}
