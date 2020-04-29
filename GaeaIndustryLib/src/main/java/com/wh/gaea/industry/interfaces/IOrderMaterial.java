package com.wh.gaea.industry.interfaces;

import java.util.Date;

public interface IOrderMaterial {
	String id();
	String name();
	String deviceId();
	float workhours();
	Date endTime();
}
