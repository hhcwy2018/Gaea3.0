package com.wh.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.collections4.map.HashedMap;

public class ProcessHelper {

	public static class ProcessInfo {
		public String name;
		public String pid;

		@Override
		public String toString() {
			return name;
		}

		public ProcessInfo(String name, String pid) {
			this.name = name;
			this.pid = pid;
		}

	}

	protected static List<ProcessInfo> readProcess(String command) throws IOException {
		List<ProcessInfo> list = readProcess("jps -l", 1, 0);
		Map<String, ProcessInfo> map = new HashedMap<>();
		for (ProcessInfo processInfo : list) {
			map.put(processInfo.pid, processInfo);
		}

		list = readProcess(command, 0, 1);
		for (ProcessInfo processInfo : list) {
			ProcessInfo jpsInfo = map.get(processInfo.pid);
			if (jpsInfo != null) {
				processInfo.name = processInfo.name + "[" + jpsInfo.name + "]";
			}
		}
		
		return list;
	}

	protected static List<ProcessInfo> readProcess(String command, int nameIndex, int pidIndex) throws IOException {
		List<String> tasklist = new ArrayList<String>();
		Process process = Runtime.getRuntime().exec(command);
		try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));) {
			String s = "";
			while ((s = br.readLine()) != null) {
				if ("".equals(s.trim())) {
					continue;
				}
				tasklist.add(s.trim());
			}
		}

		// 定义映像名称数组
		Map<String, ProcessInfo> unqNames = new TreeMap<>();
		for (int i = 0; i < tasklist.size(); i++) {
			String data = tasklist.get(i) + "";
			data = data.replaceAll("[ ]+", " ");
			String[] datas = data.split(" ");

			if (datas.length <= nameIndex)
				continue;
			
			String name = datas[nameIndex].trim();
			unqNames.put(name.toLowerCase(), new ProcessInfo(name, datas[pidIndex].toLowerCase().trim()));
		}

		return new ArrayList<>(unqNames.values());
	}

	public static List<ProcessInfo> getProcessList() throws IOException {

		Properties prop = System.getProperties();
		// 获取操作系统名称
		String os = prop.getProperty("os.name");
		if (os != null && os.toLowerCase().indexOf("linux") > -1) {
			return readProcess("ps -ef");
		} else {
			// 2.适应与windows
			return readProcess("tasklist /nh");
		}

	}

	public static int getProcessId(String name) throws IOException {
		List<ProcessInfo> processInfos = getProcessList();
		for (ProcessInfo processInfo : processInfos) {
			if (processInfo.name.equalsIgnoreCase(name)) {
				return Integer.parseInt(processInfo.pid);
			}
		}

		return -1;
	}

}
