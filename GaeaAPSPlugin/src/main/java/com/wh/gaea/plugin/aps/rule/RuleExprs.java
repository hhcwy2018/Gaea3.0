package com.wh.gaea.plugin.aps.rule;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class RuleExprs extends ArrayList<RuleExpr>{
	private static final long serialVersionUID = 1L;

	public JSONArray toJson() {
		JSONArray datas = new JSONArray();
		for (RuleExpr filter : this) {
			datas.put(filter.toJson());
		}

		return datas;
	}

	public static RuleExprs fromJson(JSONArray json) {
		RuleExprs filters = new RuleExprs();
		for (Object object : json) {
			JSONObject row = (JSONObject) object;
			filters.add(RuleExpr.fromJson(row));
		}
		return filters;
	}

	@Override
	public String toString() {
		return toJson().toString();
	}
}