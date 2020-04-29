package com.wh.gaea.plugin.aps.rule.connector;

import org.json.JSONObject;

public abstract class RuleConnector {
	public String rule_id;

	public String getID() {
		return rule_id;
	}

	public String getFilter() {
		return rule_id;
	}
	
	public JSONObject toJson() {
		JSONObject data = new JSONObject();
		data.put("rule_id", rule_id);
		return data;
	}
	
	public void fromJson(JSONObject data) {
		if (data.has("rule_id"))
			rule_id = data.getString("rule_id");
	}
}