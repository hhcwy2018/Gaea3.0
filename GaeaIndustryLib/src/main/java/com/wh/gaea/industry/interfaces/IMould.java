package com.wh.gaea.industry.interfaces;

public interface IMould extends IDevice {
	
	/**
	 * 调模时间，单位秒
	 * @return
	 */
	float adjustTimes();
	
	/**
	 * 换模时间，单位秒
	 * @return
	 */
	float switchTimes();
}
