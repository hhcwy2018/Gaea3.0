package com.wh.gaea.plugin.aps.configure;

import java.io.File;
import java.io.IOException;

import org.json.JSONObject;

import com.wh.gaea.plugin.aps.info.EngineInfo;
import com.wh.gaea.plugin.aps.rule.OrderAllotType;
import com.wh.gaea.plugin.aps.rule.OrderTarget;
import com.wh.gaea.plugin.aps.rule.RuleStructInfo;
import com.wh.gaea.plugin.aps.rule.RuleStructInfos;
import com.wh.swing.tools.MsgHelper;
import com.wh.tools.FileHelp;
import com.wh.tools.JsonHelp;

public class APSConfigure {
	static final File STRUCT_PATH = FileHelp.GetPath("struct");
	static File file = FileHelp.GetFile("config.json", "config");
	static final String CONFIG_DB_KEY = "db";
	static final String CONFIG_STRUCT_KEY = "struct";
	static final String CONFIG_STRUCT_ALLOT_KEY = "struct_allot";
	static final String CONFIG_STRUCT_TARGET_KEY = "struct_target";
	static final String CONFIG_RULEMAP_KEY = "struct_rulemap";
	static final String CONFIG_RULE_KEY = "rule";

	public static JSONObject getConfigData() throws Exception {
		if (file.exists()) {
			JSONObject json = (JSONObject) JsonHelp.parseJson(file, null);
			return json;
		}
		return new JSONObject();
	}

	public static void save(JSONObject json) throws Exception {
		JsonHelp.saveJson(file, json, null);
	}

	public static String getConfigDBName() throws Exception {
		JSONObject json = getConfigData();
		if (json.has(CONFIG_DB_KEY))
			return json.getString(CONFIG_DB_KEY);
		else {
			return null;
		}
	}

	public static void setDBConfigDBName(String dbName) throws Exception {
		JSONObject json = getConfigData();
		json.put(CONFIG_DB_KEY, dbName);
		save(json);
	}

	public static void saveCurrentRuleStruct(RuleStructInfo structInfo) throws Exception {

		if (structInfo == null)
			throw new Exception("排程架构未选择！");

		JSONObject data = getConfigData();
		data.put(CONFIG_STRUCT_KEY, structInfo.id);
		save(data);

	}

	public static void saveCurrentOrderAllotType(OrderAllotType orderAllotType) throws Exception {

		JSONObject data = getConfigData();
		data.put(CONFIG_STRUCT_ALLOT_KEY, orderAllotType.name());
		save(data);

	}

	public static void saveCurrentOrderTarget(OrderTarget orderTarget) throws Exception {

		JSONObject data = getConfigData();
		data.put(CONFIG_STRUCT_TARGET_KEY, orderTarget.name());
		save(data);

	}

	public static OrderAllotType loadCurrentOrderAllotType() throws Exception {

		JSONObject data = getConfigData();
		if (data.has(CONFIG_STRUCT_ALLOT_KEY))
			return OrderAllotType.valueOf(data.getString(CONFIG_STRUCT_ALLOT_KEY));
		else {
			return OrderAllotType.otAvg;
		}

	}

	public static OrderTarget loadCurrentOrderTarget() throws Exception {

		JSONObject data = getConfigData();
		if (data.has(CONFIG_STRUCT_TARGET_KEY))
			return OrderTarget.valueOf(data.getString(CONFIG_STRUCT_TARGET_KEY));
		else {
			return OrderTarget.otMinSwitchMould;
		}

	}

	public static File getStructFile(String id) {
		return FileHelp.GetFile(id, STRUCT_PATH);
	}

	public static RuleStructInfo loadCurrentRuleStruct() throws Exception {
		JSONObject data = getConfigData();
		if (data.has(CONFIG_STRUCT_KEY)) {
			String id = data.getString(CONFIG_STRUCT_KEY);
			return loadRuleStruct(id);
		} else {
			return null;
		}
	}

	public static RuleStructInfo loadRuleStruct(String id) throws Exception {
		File file = getStructFile(id);
		if (file.exists())
			return new RuleStructInfo((JSONObject) JsonHelp.parseJson(file, null));
		else {
			return new RuleStructInfo();
		}
	}

	public static void removeRuleStruct(RuleStructInfo structInfo) throws Exception {
		File file = getStructFile(structInfo.id);
		if (file.exists())
			if (!file.delete())
				throw new IOException("删除文件【" + file.getAbsolutePath() + "】失败！");
	}

	public static void saveRuleStruct(RuleStructInfo structInfo) throws Exception {
		JsonHelp.saveJson(getStructFile(structInfo.id), structInfo.toJson(), null);
	}

	public static void saveEngine(EngineInfo engineInfo) throws Exception {

		if (engineInfo == null)
			throw new Exception("排程引擎未选择！");

		JSONObject data = getConfigData();
		data.put(CONFIG_RULE_KEY, engineInfo.name);
		save(data);

	}

	public static EngineInfo loadEngine() throws Exception {

		JSONObject data = getConfigData();
		if (data.has(CONFIG_RULE_KEY))
			return new EngineInfo(data.getString(CONFIG_RULE_KEY));
		else {
			return null;
		}
	}

	public static RuleStructInfos getRuleStructInfos() {
		try {
			return new RuleStructInfos(STRUCT_PATH);
		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showException(e);
			return null;
		}
	}
}
