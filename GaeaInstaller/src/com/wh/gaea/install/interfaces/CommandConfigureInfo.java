package com.wh.gaea.install.interfaces;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.gaea.install.control.CommandRunner.CommandType;
import com.wh.gaea.install.control.CommandRunner.IIndicate;

public class CommandConfigureInfo {
	CommandType commandType = CommandType.ctConfigure;
	public IIndicate commandIndicate;
	public JSONArray datas;
	
	public boolean checkResult = true;
	public long waitTime = 1;
	
	public CommandType getCommandType() {
		return commandType;
	}
	
	public CommandConfigureInfo(JSONObject data) throws Exception {
		fromJson(data);
	}
	
	public CommandConfigureInfo(CommandType commandType, IIndicate commandIndicate) {
		this.commandIndicate = commandIndicate;
		this.commandType = commandType;
	}
	
	public JSONObject toJson() {
		JSONObject data = new JSONObject();
		data.put("commandType", commandType.name());
		data.put("commandIndicate", ((Enum<?>)commandIndicate).name());
		data.put("commandIndicate_type", commandIndicate.getClass().getName());
		data.put("datas", datas);
		data.put("waitTime", waitTime);
		data.put("datas", datas);
		
		return data;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public void fromJson(JSONObject data) throws Exception {
		if (data.has("commandType"))
			commandType = CommandType.valueOf(data.getString("commandType"));

		if (data.has("commandIndicate_type")) {
			Class<?> c = Class.forName(data.getString("commandIndicate_type"));
			commandIndicate = (IIndicate) Enum.valueOf((Class<Enum>) c, data.getString("commandIndicate"));
		}
		
		if (data.has("datas")) {
			datas = data.getJSONArray("datas");
		}

		if (data.has("checkResult")) {
			checkResult = data.getBoolean("checkResult");
		}
		if (data.has("waitTime")) {
			waitTime = data.getLong("waitTime");
		}
	}

}
