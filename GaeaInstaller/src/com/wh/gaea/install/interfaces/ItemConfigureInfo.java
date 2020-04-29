package com.wh.gaea.install.interfaces;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

public class ItemConfigureInfo {
	static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	Class<?> c;

	/**
	 * 显示的录入项目标题
	 */
	public String title;
	/**
	 * 项目的缺省值以及录入值
	 */
	public Object value;
	/**
	 * 检查未通过时候的提示信息，如果为null则由系统生成值不可为null的提示信息
	 */
	public String msg;
	
	/**
	 * 是否允许为null，true允许，其他不允许
	 */
	public boolean allowNull = false;
	
	/**
	 * 值检查的正则表达式
	 */
	public String regular;
	
	public Object tag;
	
	public String saveKey;
	
	protected ItemConfigureInfo(JSONObject data) throws Exception {
		fromJson(data);
	}
	
	public ItemConfigureInfo(Class<?> type) {
		this.c = type;
	}

	public Class<?> getType() {
		return c;
	}
	
	public JSONObject toJson() {
		JSONObject data = new JSONObject();
		data.put("title", title);
		if (Date.class.isAssignableFrom(c)) {
			if (value != null) {
				SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
				data.put("value", format.format(value));
			}
		}else
			data.put("value", value);
		data.put("msg", msg);
		data.put("allowNull", allowNull);
		data.put("regular", regular);
		data.put("saveKey", saveKey);
		data.put("class", c.getName());
		return data;
	}
	
	public void fromJson(JSONObject data) throws Exception {
		if (data.has("title"))
			title = data.getString("title");
		if (data.has("class"))
			c = Class.forName(data.getString("class"));
		if (data.has("value")) {
			if (Date.class.isAssignableFrom(c)) {
				if (value != null) {
					SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
					value = format.parse(data.getString("value"));
				}
			}else
			value = data.getString("value");
		}
		if (data.has("msg"))
			msg = data.getString("msg");
		if (data.has("allowNull"))
			allowNull = data.getBoolean("allowNull");
		if (data.has("regular"))
			regular = data.getString("regular");
		if (data.has("saveKey"))
			saveKey = data.getString("saveKey");
	}
	
	public static class ComboboxConfigureInfo extends ItemConfigureInfo{
		String[] items;
		
		public String[] getItems() {
			return items;
		}

		public void setItems(String[] items) {
			this.items = items == null ? null : Arrays.copyOf(items, items.length);
		}

		public ComboboxConfigureInfo(Class<?> type) {
			super(type);
		}
		
		public JSONObject toJson() {
			JSONObject data = super.toJson();
			data.put("items", items == null ? null : new JSONArray(Arrays.asList(items)));
			return data;
		}
		
		public void fromJson(JSONObject data) throws Exception {
			super.fromJson(data);
			if (data.has("items")) {
				JSONArray datas = data.getJSONArray("items");
				if (datas != null) {
					items = datas.toList().toArray(new String[datas.length()]);
				}
			}
		}
	}
}
