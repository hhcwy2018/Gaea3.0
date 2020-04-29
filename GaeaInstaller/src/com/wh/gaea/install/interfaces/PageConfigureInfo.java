package com.wh.gaea.install.interfaces;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.gaea.install.control.YamlHelper;
import com.wh.tools.IEnum;
import com.wh.tools.JsonHelp;
import com.wh.tools.TextStreamHelp;

public class PageConfigureInfo extends ArrayList<ItemConfigureInfo> {
	private static final long serialVersionUID = 1L;

	public enum SaveType implements IEnum {
		stYAML(0, "YAML文件"), stJson(1, "JSON文件"), stKeyValue(2, "键值分割文件"), stXML(3, "XML文件");

		private int code;
		private String msg;

		private SaveType(int code, String msg) {
			this.code = code;
			this.msg = msg;
		}

		@Override
		public String toString() {
			return msg;
		}

		public int getCode() {
			return code;
		}

		public String getMsg() {
			return msg;
		}
	}

	public String id = UUID.randomUUID().toString();
	public String title;
	public String saveFile;
	public SaveType saveType = SaveType.stYAML;
	public String splitChar = "=";
	
	public PageConfigureInfo() {
	}

	public PageConfigureInfo(String title) {
		this.title = title;
	}

	public PageConfigureInfo(JSONObject data) throws Exception {
		fromJson(data);
	}

	public JSONObject toJson() {
		JSONObject data = new JSONObject();
		data.put("id", id);
		data.put("title", title);
		data.put("saveFile", saveFile);
		data.put("saveType", saveType.name());
		JSONArray datas = new JSONArray();
		for (ItemConfigureInfo info : this) {
			datas.put(info.toJson());
		}
		data.put("datas", datas);

		return data;
	}

	public void fromJson(JSONObject data) throws Exception {
		if (data.has("id"))
			id = data.getString("id");

		if (data.has("title"))
			title = data.getString("title");

		if (data.has("saveFile"))
			saveFile = data.getString("saveFile");

		if (data.has("saveType"))
			try {
				saveType = SaveType.valueOf(data.getString("saveType"));
			} catch (Exception e) {
			}

		if (data.has("datas")) {
			JSONArray datas = data.getJSONArray("datas");
			if (datas != null) {
				for (Object object : datas) {
					ItemConfigureInfo info = new ItemConfigureInfo((JSONObject) object);
					add(info);
				}
			}
		}
	}

	public File getSaveFile() throws Exception {
		if (saveFile == null || saveFile.isEmpty())
			throw new NullPointerException("savefile not set!");

		File file = new File(saveFile);
		return file;
	}
	
	@Override
	public String toString() {
		return title;
	}
	
	public void saveToFile(File baseDir) throws Exception {
		String old = saveFile;
		try {
			saveFile = new File(baseDir, saveFile).getAbsolutePath();
			switch (saveType) {
				case stJson:
					Object json = JsonHelp.parseJson(getSaveFile(), null);
					for (ItemConfigureInfo info : this) {
						String[] keys = info.saveKey.split("\\.");
						json = JsonHelp.setJsonPathValue(json, keys, info.value);
					}

					JsonHelp.saveJson(getSaveFile(), json, null);
					break;
				case stYAML:
					YamlHelper yamlHelper = new YamlHelper();
					yamlHelper.loadYaml(saveFile);
					for (ItemConfigureInfo info : this) {
						yamlHelper.setProperty(info.saveKey, info.value == null ? "" : info.value);
					}
					yamlHelper.saveYaml(saveFile);
					break;
				case stKeyValue:
					String text = TextStreamHelp.loadFromFile(getSaveFile(), null);
					String splitChar = "\n";
					if (text.indexOf("\r\n") != -1) {
						splitChar = "\r\n";
					}

					String[] texts = text.split("\r\n|\n");
					Map<String, String> map = new HashMap<>();
					for (String string : texts) {
						string = string.trim();
						if (string.isEmpty())
							continue;

						String[] tmps = string.split(splitChar);
						if (tmps.length != 2)
							continue;

						String key = tmps[0].trim();
						if (key.isEmpty())
							continue;

						map.put(key, tmps[1]);
					}

					for (ItemConfigureInfo info : this) {
						map.put(info.saveKey, info.value == null ? null : info.value.toString());
					}

					String data = null;
					for (Entry<String, String> entry : map.entrySet()) {
						String value = entry.getKey() + splitChar + entry.getValue();
						if (data == null)
							data = value;
						else {
							data += splitChar + value;
						}
					}

					TextStreamHelp.saveToFile(getSaveFile(), data);

					break;
				case stXML:
					SAXReader reader = new SAXReader();
					Document dom = reader.read(new File(saveFile));
					for (ItemConfigureInfo info : this) {
						List<Node> nodes = dom.selectNodes(info.saveKey);
						if (nodes != null && nodes.size() > 0) {
							Pattern pattern = Pattern.compile("\\[([^=]+)\\]");
							Matcher matcher = pattern.matcher(info.saveKey);
							boolean hasAttr = matcher.find();
							String attrName = null;
							if (hasAttr) {
								attrName = matcher.group(1).replace("@", "");
							}
							for (Node node : nodes) {
								if (hasAttr && node instanceof Element) {
									Element element = (Element) node;
									element.addAttribute(attrName, info.value.toString());
								}else {
									node.setText(info.value == null ? "" : info.value.toString());
								}
							}
						}
					}
					
					OutputFormat format = OutputFormat.createPrettyPrint();
					format.setEncoding("UTF-8");
					XMLWriter writer = new XMLWriter(new FileOutputStream(getSaveFile()), format);
					try {
						// 设置是否转义，默认使用转义字符
						writer.setEscapeText(false);
						writer.write(dom);
					} finally {
						writer.close();				
					}
					
					break;
				}
		} finally {
			saveFile = old;
		}
	}

}
