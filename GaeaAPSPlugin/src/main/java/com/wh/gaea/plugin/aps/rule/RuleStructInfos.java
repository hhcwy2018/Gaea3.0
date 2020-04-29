package com.wh.gaea.plugin.aps.rule;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.gaea.plugin.aps.rule.connector.GlobalConnector;
import com.wh.gaea.plugin.aps.rule.connector.RuleConnector;
import com.wh.gaea.plugin.aps.rule.connector.StructConnector;
import com.wh.tools.JsonHelp;

public class RuleStructInfos {

	Map<String, RuleStructInfo> structMap = new ConcurrentHashMap<>();

	public RuleStructInfos(File path) throws Exception {
		File[] files = path.listFiles();
		if (files != null)
			for (File file : files) {
				if (!file.isFile())
					continue;

				JSONObject data = (JSONObject) JsonHelp.parseJson(file, null);
				RuleStructInfo info = new RuleStructInfo(data);
				structMap.put(info.id, info);
			}
	}

	protected StructConnector getStructConnector(String struct_id) throws Exception {
		RuleStructInfo struct = structMap.get(struct_id);
		if (struct == null)
			throw new Exception("未发现指定的架构【" + struct_id + "】，请检查后重试！");

		if (struct.structConnector == null) {
			struct.structConnector = new StructConnector(struct_id);
		}

		return struct.structConnector;
	}

	public void bindToStruct(String struct_id, RuleConnector connector) throws Exception {
		StructConnector structConnector = getStructConnector(struct_id);

		structConnector.saveConnector(connector);
	}

	public void unbindToStruct(String struct_id, RuleConnector connector) throws Exception {
		StructConnector structConnector = getStructConnector(struct_id);
		structConnector.removeConnector(connector);
	}

	public List<Rule> getGlobalRules(String struct_id) throws Exception {
		RuleStructInfo structInfo = structMap.get(struct_id);
		List<Rule> rules = new ArrayList<>();
		Map<RuleConnector, Rule> connectors = structInfo.getRules(GlobalConnector.class);
		rules.addAll(connectors.values());
		return rules;
	}

	public void removeStructInfo(String id) throws Exception {
		structMap.remove(id);
	}

	public void addStructInfo(RuleStructInfo info) throws Exception {
		structMap.put(info.id, info);
	}

	public JSONArray toJson() {
		JSONArray data = new JSONArray();
		for (RuleStructInfo info : structMap.values()) {
			data.put(info.toJson());
		}
		return data;
	}

	public void fromJson(JSONArray data) throws JSONException, Exception {
		structMap.clear();
		for (Object object : data) {
			RuleStructInfo info = new RuleStructInfo((JSONObject) object);
			structMap.put(info.id, info);
		}
	}

	public RuleStructInfo[] toArray() {
		return structMap.size() == 0 ? null : structMap.values().toArray(new RuleStructInfo[structMap.size()]);
	}
}
