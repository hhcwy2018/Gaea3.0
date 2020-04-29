package com.wh.gaea.plugin.aps.rule.connector;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.gaea.plugin.aps.rule.Rule;

public class StructConnector {
	String struct_id;

	public Map<String, BomConnector> bomConnectorMap = new ConcurrentHashMap<>();
	public Map<String, GlobalConnector> globalConnectorMap = new ConcurrentHashMap<>();
	public Map<String, BommxConnector> bomnodeConnectorMap = new ConcurrentHashMap<>();

	public String getStructId() {
		return struct_id;
	}

	public StructConnector() {

	}

	@SuppressWarnings("unchecked")
	public <T extends RuleConnector> Map<String, T> getMap(Class<T> c) {
		if (BommxConnector.class.isAssignableFrom(c))
			return (Map<String, T>) bomnodeConnectorMap;
		else if (BomConnector.class.isAssignableFrom(c))
			return (Map<String, T>) bomConnectorMap;
		else {
			return (Map<String, T>) globalConnectorMap;
		}
	}

	public RuleConnector getConnector(Rule rule) {
		switch (rule.scope) {
		case rsBOM:
			return bomConnectorMap.get(rule.id);
		case rsGlobal:
			return globalConnectorMap.get(rule.id);
		case rsNode:
		default:
			return bomnodeConnectorMap.get(rule.id);
		}
	}

	public StructConnector(String struct_id) throws Exception {
		this.struct_id = struct_id;
	}

	protected <T extends RuleConnector> void newConnector(JSONObject data, String mapName, Class<T> c,
			Map<String, T> map) throws Exception {
		if (data.has(mapName)) {
			JSONArray datas = data.getJSONArray(mapName);
			for (Object object : datas) {
				JSONObject row = (JSONObject) object;
				T connector = c.newInstance();
				connector.fromJson(row);
				map.put(connector.rule_id, connector);
			}
		}
	}

	public void clear() {
		bomConnectorMap.clear();
		globalConnectorMap.clear();
		bomnodeConnectorMap.clear();
	}

	public void fromJson(JSONObject data) throws Exception {
		clear();
		newConnector(data, "bomConnectorMap", BomConnector.class, bomConnectorMap);
		newConnector(data, "globalConnectorMap", GlobalConnector.class, globalConnectorMap);
		newConnector(data, "bomnodeConnectorMap", BommxConnector.class, bomnodeConnectorMap);
	}

	public void removeConnector(RuleConnector connector) throws Exception {
		if (connector instanceof BomConnector)
			bomConnectorMap.remove(connector.rule_id);
		else if (connector instanceof BommxConnector)
			bomnodeConnectorMap.remove(connector.rule_id);
		else if (connector instanceof GlobalConnector)
			globalConnectorMap.remove(connector.rule_id);

	}

	public void removeConnector(String rule_id) throws Exception {
		bomConnectorMap.remove(rule_id);
		bomnodeConnectorMap.remove(rule_id);
		globalConnectorMap.remove(rule_id);
	}

	public void saveConnector(RuleConnector connector) {
		if (connector instanceof BommxConnector)
			bomnodeConnectorMap.put(connector.rule_id, (BommxConnector) connector);
		else if (connector instanceof BomConnector)
			bomConnectorMap.put(connector.rule_id, (BomConnector) connector);
		else if (connector instanceof GlobalConnector)
			globalConnectorMap.put(connector.rule_id, (GlobalConnector) connector);

	}

	public void saveConnector(Rule rule, String bomId, String bommxId) {
		RuleConnector connector = null;
		switch (rule.scope) {
		case rsNode:
			connector = new BommxConnector(bomId, bommxId, rule.id);
			break;
		case rsGlobal:
			connector = new GlobalConnector(rule.id);
			break;
		case rsBOM:
			connector = new BomConnector(bomId, rule.id);
			break;
		}

		saveConnector(connector);
	}

	protected <T extends RuleConnector> void saveConnectorMapToJson(Map<String, T> map, String key, JSONObject data) {
		JSONArray values = new JSONArray();
		for (T connector : map.values()) {
			values.put(connector.toJson());
		}
		data.put(key, values);
	}

	public JSONObject toJson() {
		JSONObject data = new JSONObject();
		data.put("struct_id", struct_id);
		saveConnectorMapToJson(bomConnectorMap, "bomConnectorMap", data);
		saveConnectorMapToJson(bomnodeConnectorMap, "bomnodeConnectorMap", data);
		saveConnectorMapToJson(globalConnectorMap, "globalConnectorMap", data);
		return data;
	}
}