package com.wh.hardware.register;

import org.json.JSONObject;

import com.wh.encrypt.MD5;

public class Core {
	public static String CHARSET = "utf8";
	
	public String getRegisterSignCode(JSONObject data) {
		return MD5.encode(String.valueOf(data.getLong("start")) + String.valueOf(data.getInt("use")) + data.getString("aes") + data.getString("tag"));
	}
	
}
