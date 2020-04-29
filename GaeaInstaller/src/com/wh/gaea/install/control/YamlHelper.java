package com.wh.gaea.install.control;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class YamlHelper {

	Map<String, Object> map = new LinkedHashMap<>();

	public static boolean isNotEmpty(String str) {
		return str != null && !str.trim().isEmpty();
	}

	public void loadYaml(String fileName) throws IOException {
		try (InputStream in = new FileInputStream(fileName);) {
			map = isNotEmpty(fileName)
					? new Yaml().load(in)
					: new LinkedHashMap<>();
		}
	}

	public void saveYaml(String fileName) throws IOException {
		if (isNotEmpty(fileName)) {

			try (FileWriter fileWriter = new FileWriter(
					fileName);) {
				DumperOptions options = new DumperOptions();
				options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
				Yaml yaml = new Yaml(options);
				yaml.dump(map, fileWriter);
			}
		}
	}

	public Object getProperty(Object qualifiedKey) {
		return getProperty(map, qualifiedKey);
	}

	@SuppressWarnings("unchecked")
	protected Object getProperty(Map<String, Object> map, Object qualifiedKey) {
		if (map != null && !map.isEmpty() && qualifiedKey != null) {
			String input = String.valueOf(qualifiedKey).trim();
			if (!input.isEmpty()) {
				if (input.contains(".")) {
					int index = input.indexOf(".");
					String left = input.substring(0, index);
					String right = input.substring(index + 1, input.length());
					return getProperty((Map<String, Object>) map.get(left),
							right);
				} else if (map.containsKey(input)) {
					return map.get(input);
				} else {
					return null;
				}
			}
		}
		return null;
	}

	public void setProperty(String qualifiedKey, Object value) {
		setProperty(map, qualifiedKey, value);
	}

	@SuppressWarnings("unchecked")
	protected void setProperty(Map<String, Object> map, String qualifiedKey,
			Object value) {
		if (map != null && !map.isEmpty() && qualifiedKey != null) {
			String input = String.valueOf(qualifiedKey).trim();
			if (!input.isEmpty()) {
				if (input.contains(".")) {
					int index = input.indexOf(".");
					String left = input.substring(0, index);
					String right = input.substring(index + 1, input.length());
					setProperty((Map<String, Object>) map.get(left), right,
							value);
				} else {
					map.put(qualifiedKey, value);
				}
			}
		}
	}
}