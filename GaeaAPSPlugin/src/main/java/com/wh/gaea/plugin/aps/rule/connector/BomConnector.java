package com.wh.gaea.plugin.aps.rule.connector;

import org.json.JSONObject;

import com.wh.tools.StringHelper;

public class BomConnector extends RuleConnector {
	public String bom_id;

	public BomConnector(String bom_id) {
		this.bom_id = bom_id;
	}

	protected BomConnector() {
	}

	public BomConnector(String bom_id, String rule_id) {
		this.bom_id = bom_id;
		this.rule_id = rule_id;
	}

	public String getID() {
		return StringHelper.linkString(bom_id, rule_id);
	}

	public String getFilter() {
		return bom_id;
	}

	@Override
	public JSONObject toJson() {
		JSONObject data = super.toJson();
		data.put("bom_id", bom_id);
		return data;
	}

	@Override
	public void fromJson(JSONObject data) {
		super.fromJson(data);
		if (data.has("bom_id"))
			bom_id = data.getString("bom_id");
	}

}