package com.wh.gaea.industry.info;

public class ExtendParamInfo<V>{
	public String key;
	public V value;
	
	public ExtendParamInfo(String key, V value) {
		this.key = key;
		this.value = value;
	}
}