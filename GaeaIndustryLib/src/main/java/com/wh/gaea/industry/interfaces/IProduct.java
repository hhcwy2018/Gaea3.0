package com.wh.gaea.industry.interfaces;

import java.util.Map;

public interface IProduct {
	String materalId();
	Integer count();
	Map<String, Object> extendParams();
}
