package com.wh.gaea.plugin.aps.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.DefaultTableModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.gaea.plugin.aps.info.RuleMapInfos;
import com.wh.gaea.plugin.aps.rule.connector.RuleConnector;
import com.wh.gaea.plugin.aps.rule.connector.StructConnector;

public class RuleStructInfo {

	public RuleMapInfos ruleMapInfos = new RuleMapInfos();

	public Map<String, Rule> ruleMap = new HashMap<>();

	public OrderTarget orderTarget = OrderTarget.otMinSwitchMould;
	public OrderAllotType orderType = OrderAllotType.otAvg;

	public String id;
	public String desc;
	public Date date;

	public StructConnector structConnector;

	public JSONObject toJson() {
		JSONObject data = new JSONObject();
		data.put("id", id);
		data.put("date", date.getTime());
		data.put("desc", desc);
		data.put("orderTarget", orderTarget.name());
		data.put("orderType", orderType.name());
		JSONArray rules = new JSONArray();
		for (Rule rule : ruleMap.values()) {
			rules.put(rule.toJson());
		}

		data.put("rules", rules);

		data.put("ruleMapInfos", ruleMapInfos.toJson());
		if (structConnector != null)
			data.put("structConnector", structConnector.toJson());
		return data;
	}

	public void fromJson(JSONObject data) throws JSONException, Exception {
		ruleMap.clear();

		if (data.has("id"))
			id = data.getString("id");
		if (data.has("date"))
			date = new Date(data.getLong("date"));
		if (data.has("desc"))
			desc = data.getString("desc");
		if (data.has("orderTarget"))
			orderTarget = OrderTarget.valueOf(data.getString("orderTarget"));
		if (data.has("orderType"))
			orderType = OrderAllotType.valueOf(data.getString("orderType"));
		if (data.has("ruleMapInfos"))
			ruleMapInfos = new RuleMapInfos(data.getJSONArray("ruleMapInfos"));

		if (data.has("rules")) {
			JSONArray rules = data.getJSONArray("rules");
			for (Object tmp : rules) {
				Rule rule = new Rule();
				rule.fromJson((JSONObject) tmp);
				ruleMap.put(rule.id, rule);
			}
		}

		if (data.has("structConnector")) {
			structConnector = new StructConnector();
			structConnector.fromJson(data.getJSONObject("structConnector"));
		} else {
			structConnector = new StructConnector(id);
		}
	}

	public RuleStructInfo() {
	}

	public RuleStructInfo(JSONObject data) throws JSONException, Exception {
		fromJson(data);
	}

	@Override
	public String toString() {
		return id;
	}

	public void saveRule(Rule rule) {
		ruleMap.put(rule.id, rule);
	}

	public static Rule createRule(DefaultTableModel model, int row) {
		if (row < 0 || row >= model.getRowCount())
			return null;

		Rule rule = new Rule();
		for (int i = 0; i < model.getColumnCount(); i++) {
			String colName = model.getColumnName(i);
			rule.setValue(colName, model.getValueAt(row, i));
		}

		return rule;
	}

	public void saveConnector(Rule rule, String bomId, String bommxId) {
		structConnector.saveConnector(rule, bomId, bommxId);
	}

	@SuppressWarnings("unchecked")
	public <T extends RuleConnector> Map<RuleConnector, Rule> getRules(Class<T> c) {

		Map<RuleConnector, Rule> connectorMap = new HashMap<>();

		StructConnector connector = structConnector;
		if (connector == null)
			return connectorMap;

		Collection<RuleConnector> connectors = (Collection<RuleConnector>) connector.getMap(c).values();
		if (connectors != null)
			for (RuleConnector ruleConnector : connectors) {
				if (ruleConnector.getClass().equals(c)) {
					if (ruleMap.containsKey(ruleConnector.rule_id))
						connectorMap.put(ruleConnector, ruleMap.get(ruleConnector.rule_id));
				}
			}

		return connectorMap;
	}

	public void removeRule(Rule rule) throws Exception {
		ruleMap.remove(rule.id);
		structConnector.removeConnector(rule.id);
	}

	public List<Rule> queryRules(RulePart part, RuleScope scope, RuleType type, String... keys) {

		Map<String, String> keyMap = new HashMap<>();

		if (keys != null && keys.length > 0)
			for (String key : keys) {
				if (key == null || key.isEmpty())
					continue;
				
				keyMap.put(key, key);
			}

		List<Rule> rules = new ArrayList<>();
		for (Rule rule : new ArrayList<>(ruleMap.values())) {
			boolean isPart = true;
			if (part != null) {
				isPart = part == rule.part;
			}
			
			boolean isScope = true;
			if (scope != null) {
				isScope = scope == rule.scope;
			}
			
			boolean isType = true;
			if (type != null) {
				isType = type == rule.type;
			}

			if (isPart && isScope && isType) {
				RuleConnector connector = structConnector.getConnector(rule);
				if (connector == null) {
					ruleMap.remove(rule.id);
					continue;
				}
				if (keyMap.size() > 0) {
					if (keyMap.containsKey(connector.getFilter()))
						rules.add(rule);
				} else
					rules.add(rule);
			}
		}

		return rules;
	}

}
