package com.wh.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonHelp {

	public static final String Default_Charset = "utf8";
	
	public static class NoCacheInstance implements ICacheInstance{
		@Override
		public File open(File file) {
			return file;
		}
		
		@Override
		public void save(File saveFile, byte[] datas, boolean needCopy) throws IOException {
			try (FileOutputStream stream = new FileOutputStream(saveFile);) {
				stream.write(datas, 0, datas.length);
			} catch (Exception e) {
				throw e;
			}
		}
 
	}
	
	static ICacheInstance cacheInstance = new NoCacheInstance();
	
	public static ICacheInstance getCacheInstance() {
		return cacheInstance;
	}
	
	public static void setCacheInstance(ICacheInstance cacheInstance) {
		JsonHelp.cacheInstance = cacheInstance;
	}
	
	public static Object parseJson(String str) throws JSONException{
		str = str.trim();
		if (str.isEmpty())
			return null;
		
		if (str.charAt(0) == '{')
			return new JSONObject(str);
		else
			return new JSONArray(str);
	}
	
	public static Object parseJson(File f, String charset) throws Exception{
		return parseJson(f, charset, false);		
	}
	
	public static Object parseCacheJson(File f, String charset) throws Exception{
		return parseJson(f, charset, true);
	}
	
	public static Object parseJson(File f, String charset, boolean needCopy) throws Exception{
		charset = (charset == null || charset.isEmpty()) ? Default_Charset : charset;
		if (needCopy)
			f = getCacheInstance().open(f);
		
		try(FileInputStream stream = new FileInputStream(f);){
			byte[] datas = new byte[stream.available()];
			int index = 0;
			while (stream.available() > 0){
				int max = Math.min(stream.available(), datas.length - index);
				int readLen = stream.read(datas, 0, max);
				index += readLen;
			}
			String value = new String(datas, charset);
			return parseJson(value);
		}
				
	}

	public static void saveJson(File f, Object value, String charset) throws Exception{
		saveJson(f, value, charset, true);		
	}
	
	public static void saveJson(File f, Object value, String charset, boolean needCopy) throws Exception{
		saveJson(f, value.toString(), charset, needCopy);
	}
	
	public static void saveJson(File f, String value, String charset) throws Exception{
		saveJson(f, value, charset, true);
	}
	
	public static void saveJson(File f, String value, String charset, boolean needCopy) throws Exception{
		charset = (charset == null || charset.isEmpty()) ? Default_Charset : charset;
		byte[] datas = value.getBytes(charset);
		getCacheInstance().save(f, datas, needCopy);
	}
	
	public static boolean isEmpty(JSONObject json, String key){
		if (!json.has(key))
			return true;
		
		Object object = json.get(key);
		if (object == null)
			return true;
		
		if (object instanceof String){
			if (((String)object).isEmpty())
				return true;
		}
		
		return false;
	}
	public static String getString(JSONObject json, String key){
		if (json.has(key)){
			Object value = json.get(key);
			if (value == null)
				return null;
			
			return value.toString();
		}else
			return null;
	}
	
	static final String JSONARRAY_REGEX = "\\[(\\d)*\\]";

	protected static boolean isJsonArray(Object json) {
		return json.toString().matches(JSONARRAY_REGEX);
	}

	protected static Integer getJsonArrayIndex(Object json) throws NumberFormatException {
		String key = json.toString();

		Pattern pattern = Pattern.compile(JSONARRAY_REGEX);
		Matcher matcher = pattern.matcher(key);
		if (matcher.find()) {
			String indexString = matcher.group(1);
			if (indexString == null || indexString.isEmpty()) {
				return null;
			} else {
				return Integer.parseInt(indexString);
			}
		} else {
			throw new NumberFormatException("not jsonarray!");
		}

	}

	protected static Object setSimpleJsonValue(Object parent, String key, Object value, boolean replace) {

		if (isJsonArray(key)) {
			JSONArray jsonArray = (JSONArray) parent;

			Integer index = getJsonArrayIndex(key);
			if (index == null) {
				jsonArray.put(value);
			} else {
				if (!replace)
					if (index < jsonArray.length())
						return jsonArray.get(index);
				jsonArray.put(index, value);
			}
		} else {
			JSONObject jsonObject = (JSONObject) parent;
			if (!replace)
				if (jsonObject.has(key))
					return jsonObject.get(key);

			jsonObject.put(key, value);
		}

		return value;
	}

	/**
	 * 给json的一个节点赋值
	 * @param root json的根对象，必须于keys的第一个值类型相同，如果为null则会根据keys的第一个值建立json对象
	 * @param keys json的键路径，格式如下：
	 * 比如
	 * {
	 * 		root:{
	 * 			item:["a","b"]
	 * 		}
	 * }
	 * 如果给item的第1个值赋值，那么keys的值：["root","item","[0]"]
	 * @param value 设置的值
	 * @return 返回root对象
	 */
	public static Object setJsonPathValue(Object root, String[] keys, Object value) {
		if (root == null)
			root = isJsonArray(keys[0]) ? new JSONArray() : new JSONObject();

		Object result = root;
		if (keys.length > 1) {
			for (int i = 0; i < keys.length - 1; i++) {
				String key = keys[i];
				if (isJsonArray(key)) {
					root = setSimpleJsonValue(root, key, new JSONArray(), false);
				} else {
					root = setSimpleJsonValue(root, key, new JSONObject(), false);
				}
			}
		}

		String key = keys[keys.length - 1];
		setSimpleJsonValue(root, key, value, true);

		return result;
	}

}
