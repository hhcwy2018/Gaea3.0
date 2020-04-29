package com.wh.gaea.install.interfaces;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class InstallConfigureInfo extends ArrayList<PageConfigureInfo>{
	private static final long serialVersionUID = 1L;

	public String name;
	public String iconName = null;
	public ArrayList<CommandConfigureInfo> commands = new ArrayList<>();
	
	public File installDir;
	public File publishDir;
	
	public boolean includeGaea = false;
	public boolean includeGaeaRuntime = false;
	public String gaeaProjectName;
	public String gaeaDocRootPath = "C:\\xampp\\htdocs\\project";
	public File gaeaRuntimePath = new File("c:\\");
	
	public PageConfigureInfo get(String pageId) {
		for (PageConfigureInfo pageInfo : this) {
			if (pageInfo.id.equalsIgnoreCase(pageId)) {
				return pageInfo;
			}
		}
		
		return null;
	}
	
	public JSONObject toJson() {
		JSONObject data = new JSONObject();

		data.put("name", name);
		
		JSONArray datas = new JSONArray();
		for (PageConfigureInfo info : this) {
			datas.put(info.toJson());
		}
		data.put("datas", datas);
		
		datas = new JSONArray();		
		for (CommandConfigureInfo info : this.commands) {
			datas.put(info.toJson());
		}
		data.put("commands", datas);
		
		data.put("iconName", iconName);
		
		data.put("includeGaea", includeGaea);
		
		data.put("gaeaRuntimePath", gaeaRuntimePath == null ? null : gaeaRuntimePath.getAbsolutePath());

		data.put("includeGaeaRuntime", includeGaeaRuntime);
		data.put("gaeaProjectName", gaeaProjectName);
		data.put("gaeaDocRootPath", gaeaDocRootPath);

		data.put("installDir", installDir == null ? null : installDir.getAbsolutePath());
		data.put("publishDir", publishDir == null ? null : publishDir.getAbsolutePath());
		return data;
	}
	
	public void fromJson(JSONObject data) throws Exception {
		if (data.has("name"))
			name = data.getString("name");
		
		if (data.has("iconName"))
			iconName = data.getString("iconName");
		
		if (data.has("includeGaea"))
			includeGaea = data.getBoolean("includeGaea");
		
		if (data.has("gaeaRuntimePath"))
			gaeaRuntimePath = new File(data.getString("gaeaRuntimePath"));
		
		if (data.has("includeGaeaRuntime"))
			includeGaeaRuntime = data.getBoolean("includeGaeaRuntime");
		
		if (data.has("gaeaProjectName"))
			gaeaProjectName = data.getString("gaeaProjectName");
		
		if (data.has("gaeaDocRootPath"))
			gaeaDocRootPath = data.getString("gaeaDocRootPath");
		
		if (data.has("installDir"))
			installDir = new File(data.getString("installDir"));
		
		if (data.has("publishDir"))
			publishDir = new File(data.getString("publishDir"));
		
		clear();
		commands.clear();
		if (data.has("datas")) {
			JSONArray datas = data.getJSONArray("datas");
			if (datas != null) {
				for (Object object : datas) {
					PageConfigureInfo info = new PageConfigureInfo((JSONObject) object);
					add(info);
				}
			}
		}

		if (data.has("commands")) {
			JSONArray datas = data.getJSONArray("commands");
			if (datas != null) {
				for (Object object : datas) {
					CommandConfigureInfo info = new CommandConfigureInfo((JSONObject) object);
					commands.add(info);
				}
			}
		}
	}

}
