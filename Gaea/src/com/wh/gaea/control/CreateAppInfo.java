package com.wh.gaea.control;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.tools.JsonHelp;

public class CreateAppInfo {
	public String title;
	public boolean includeUI = true;
	public String publishDir;

	public JSONArray plugins = new JSONArray();
	public JSONArray menus = new JSONArray();

	public File saveFile;

	public CreateAppInfo(File file) {
		this.saveFile = file;
	}

	public File getPublishDir() {
		return new File(publishDir);
	}
	
	public void reset() {
		title = null;
		includeUI = true;
		publishDir = null;
		plugins = new JSONArray();
		menus = new JSONArray();
	}

	public void fromJson(JSONObject data) {
		if (data.has("title"))
			title = data.getString("title");
		if (data.has("includeUI"))
			includeUI = data.getBoolean("includeUI");
		if (data.has("publishDir"))
			publishDir = data.getString("publishDir");

		if (data.has("plugins")) {
			plugins = data.getJSONArray("plugins");
		}

		if (data.has("menus")) {
			menus = data.getJSONArray("menus");
		}
	}

	public JSONObject toJson() {
		JSONObject data = new JSONObject();
		data.put("title", title);
		data.put("includeUI", includeUI);
		data.put("publishDir", publishDir);
		data.put("plugins", plugins);
		data.put("menus", menus);
		return data;
	}

	public void load() throws Exception {
		if (!saveFile.exists())
			return;

		JSONObject data = (JSONObject) JsonHelp.parseJson(saveFile, null);
		fromJson(data);
	}

	public void save() throws Exception {
		if (!saveFile.getParentFile().exists())
			if (!saveFile.mkdirs())
				throw new Exception("建立目录【" + saveFile.getAbsolutePath() + "】失败！");

		JsonHelp.saveJson(saveFile, toJson(), null);
	}
}