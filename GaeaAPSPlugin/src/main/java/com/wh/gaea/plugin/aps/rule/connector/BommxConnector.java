package com.wh.gaea.plugin.aps.rule.connector;

import org.json.JSONObject;

import com.wh.tools.StringHelper;

public class BommxConnector extends BomConnector {
	public String bommx_id;

	protected BommxConnector() {
		
	}
	
	public BommxConnector(String bom_id, String bommx_id) {
		super(bom_id);
		this.bommx_id = bommx_id;
	}

	public BommxConnector(String bom_id, String bommx_id, String rule_id) {
		super(bom_id, rule_id);
		this.bommx_id = bommx_id;
	}

	public String getID() {
		return StringHelper.linkString(StringHelper.linkString(bom_id, rule_id), bommx_id);
	}

	public String getFilter() {
		return getFilter(bom_id, bommx_id);
	}
	
	public static String getFilter(String bom_id, String bommx_id) {
		return StringHelper.linkString(bom_id, bommx_id);
	}
	
	@Override
	public JSONObject toJson() {
		JSONObject data = super.toJson();
		data.put("bommx_id", bommx_id);
		return data;
	}
	
	@Override
	public void fromJson(JSONObject data) {
		super.fromJson(data);
		if (data.has("bommx_id"))
			bommx_id = data.getString("bommx_id");
	}

}