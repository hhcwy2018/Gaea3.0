package com.wh.gaea.industry.info;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessInfo {
	public String id;
	public String code;
	public String name;
	public String desc;
	public String type;

	/**
	 * 此工序包含的设备列表，key/value为设备编号
	 */
	public Map<String, String> deviceMap = new ConcurrentHashMap<>();

	/**
	 * 此工序包含的工位列表，key/value为工位编号
	 */
	public Map<String, String> stationMap = new ConcurrentHashMap<>();

	/**
	 * 此工位包含的工位与设备映射表，key为工位编号，value为设备编号
	 */
	public Map<String, String> station_deviceMap = new ConcurrentHashMap<>();

}
