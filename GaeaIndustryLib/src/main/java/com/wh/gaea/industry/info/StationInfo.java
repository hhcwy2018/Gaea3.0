package com.wh.gaea.industry.info;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StationInfo {
	public String id;
	public String code;
	public String name;
	
	public List<String> deviceIds = Collections.synchronizedList(new ArrayList<>());
}
