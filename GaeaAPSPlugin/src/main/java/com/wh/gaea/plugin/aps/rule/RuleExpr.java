package com.wh.gaea.plugin.aps.rule;

import org.json.JSONObject;

import com.wh.gaea.industry.interfaces.DynamicGroupInfo;
import com.wh.gaea.industry.interfaces.DynamicParamInfo;
import com.wh.gaea.plugin.aps.rule.connector.RuleTarget;

public class RuleExpr {
	public RuleExprLogicType leftLogic = RuleExprLogicType.ltNone;
	public RuleExprLogicType rightLogic = RuleExprLogicType.ltNone;
	public RuleTarget type = RuleTarget.ftDevice;
	public DynamicGroupInfo item;
	public DynamicParamInfo attr;
	public RuleExprOperation operation;
	public Object value;
	public int level = 0;

	public JSONObject toJson() {
		JSONObject data = new JSONObject();
		data.put("leftLogic", leftLogic == null ? null : leftLogic.name());
		data.put("rightLogic", rightLogic == null ? null : rightLogic.name());
		data.put("type", type.name());
		data.put("operation", operation.name());
		data.put("item", item.toJson());
		data.put("attr", attr.toJson());
		data.put("value", value);
		data.put("level", level);
		return data;
	}

	@Override
	public String toString() {
		return toJson().toString();
	}

	public static RuleExpr fromJson(JSONObject json) {
		RuleExpr filter = new RuleExpr();

		if (json.has("leftLogic"))
			filter.leftLogic = RuleExprLogicType.valueOf(json.getString("leftLogic"));
		if (json.has("rightLogic"))
			filter.rightLogic = RuleExprLogicType.valueOf(json.getString("rightLogic"));
		filter.type = RuleTarget.valueOf(json.getString("type"));
		filter.operation = RuleExprOperation.valueOf(json.getString("operation"));
		if (json.has("item"))
			filter.item = new DynamicGroupInfo(json.getJSONObject("item"));
		if (json.has("attr"))
			filter.attr = new DynamicParamInfo(json.getJSONObject("attr"));
		if (json.has("value"))
			filter.value = json.get("value");
		if (json.has("level"))
			filter.level = json.getInt("level");
		return filter;
	}

}