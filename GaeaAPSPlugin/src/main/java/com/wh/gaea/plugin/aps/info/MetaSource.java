package com.wh.gaea.plugin.aps.info;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MetaSource<T,V>{
	public Map<T, V> datas = new ConcurrentHashMap<>();
}